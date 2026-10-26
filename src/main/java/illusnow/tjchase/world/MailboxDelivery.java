/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package illusnow.tjchase.world;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MailboxDelivery {
    private static final int OUTBOUND_DELAY = 20;
    private static final int OUTBOUND_FLIGHT = 40;
    private static final int RETURN_DELAY = 40;
    private static final double FLIGHT_DISTANCE = 12;
    private static final double FORCE_SENT_DISTANCE = 50;
    private static final Logger LOGGER = LogUtils.getLogger();

    private MailboxDelivery() {}

    public static boolean start(ServerLevel level, BlockPos mailboxPos, ServerPlayer player) {
        Manager manager = level.getData(ModAttachments.MAILBOX_DELIVERY);
        if (manager.deliveries.getOrDefault(player.getUUID(), new State()).active) {
            return false;
        }
        Vec3 origin = Vec3.atCenterOf(mailboxPos).add(0, 0.5, 0);
        double angle = player.getRandom().nextDouble() * 2 * Math.PI;
        Vec3 destination = origin.add(Math.cos(angle) * FLIGHT_DISTANCE, 2, Math.sin(angle) * FLIGHT_DISTANCE);
        level.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y, origin.z, 12, 0.25, 0.2, 0.25, 0.03);
        State delivery = manager.deliveries.computeIfAbsent(player.getUUID(), uuid -> new State());
        delivery.begin(level.dimension(), origin, destination);
        level.setData(ModAttachments.MAILBOX_DELIVERY, manager);
        return true;
    }

    public static void tryPause(ServerPlayer player) {
        for (ServerLevel level : player.level().getServer().getAllLevels()) {
            Manager manager = level.getData(ModAttachments.MAILBOX_DELIVERY);
            State delivery = manager.deliveries.get(player.getUUID());
            if (delivery == null || !delivery.active) {
                continue;
            }
            pauseActive(level, delivery);
        }
    }

    private static void pauseActive(ServerLevel level, State delivery) {
        if (delivery.allayId != null && level.getEntity(delivery.allayId) instanceof Allay allay) {
            delivery.allayPosition = allay.position();
            allay.discard();
            delivery.allayId = null;
        }
        delivery.setPaused(true);
    }

    public static boolean isAllayDelivering(Allay allay) {
        if (allay.level().isClientSide()) {
            throw new IllegalStateException("Wrong side!");
        }
        ServerLevel level = (ServerLevel) allay.level();
        Manager manager = level.getData(ModAttachments.MAILBOX_DELIVERY);
        return manager.deliveries.values().stream().anyMatch(state -> Objects.equals(allay.getUUID(), state.allayId));
    }

    public static void tickAll(ServerLevel level) {
        Manager manager = level.getData(ModAttachments.MAILBOX_DELIVERY);
        boolean changed = false;
        for (var iterator = manager.deliveries.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            State delivery = entry.getValue();
            if (!delivery.active || !delivery.dimension.equals(level.dimension())) {
                continue;
            }
            if (player == null || !player.isAlive() || delivery.tooFarToReach(player)) {
                pauseActive(level, delivery);
            } else {
                delivery.setPaused(false);
            }
            if (player != null) {
                delivery.tick(level, player);
            }
            changed = true;
            if (!delivery.active) {
                iterator.remove();
            }
        }
        if (changed) {
            level.setData(ModAttachments.MAILBOX_DELIVERY, manager);
        }
    }

    public static final class Manager {
        private final Map<UUID, State> deliveries = new HashMap<>();

        public Manager() {}

        public static final class Serializer implements IAttachmentSerializer<Manager> {
            public static final Serializer INSTANCE = new Serializer();

            private Serializer() {}

            @Override
            public Manager read(IAttachmentHolder holder, ValueInput input) {
                Manager manager = new Manager();
                for (ValueInput child : input.childrenListOrEmpty("deliveries")) {
                    String playerId = child.getStringOr("player", "");
                    try {
                        manager.deliveries.put(UUID.fromString(playerId), State.Serializer.INSTANCE.read(holder, child));
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Invalid player UUID {}, delivery state not serialized", playerId.isBlank() ? "(blank)" : playerId);
                    }
                }
                return manager;
            }

            @Override
            public boolean write(Manager manager, ValueOutput output) {
                ValueOutput.ValueOutputList children = output.childrenList("deliveries");
                manager.deliveries.forEach((playerId, state) -> {
                    ValueOutput child = children.addChild();
                    child.putString("player", playerId.toString());
                    State.Serializer.INSTANCE.write(state, child);
                });
                return true;
            }
        }
    }

    public static final class State {
        private boolean active;
        private ResourceKey<Level> dimension = Level.OVERWORLD;
        private Vec3 origin = Vec3.ZERO;
        private Vec3 destination = Vec3.ZERO;
        private Vec3 allayPosition = Vec3.ZERO;
        @Nullable
        private UUID allayId;
        private int ticks;
        private Phase phase = Phase.WAITING_TO_LEAVE;
        private boolean paused;

        public State() {}

        private void begin(ResourceKey<Level> dimension, Vec3 origin, Vec3 destination) {
            this.active = true;
            this.dimension = dimension;
            this.origin = origin;
            this.destination = destination;
            this.allayPosition = origin;
            this.allayId = null;
            this.ticks = 0;
            this.phase = Phase.WAITING_TO_LEAVE;
        }

        private void tick(ServerLevel level, ServerPlayer player) {
            if (paused) {
                return;
            }
            ticks++;
            switch (phase) {
                case WAITING_TO_LEAVE -> {
                    if (ticks >= OUTBOUND_DELAY) {
                        Allay allay = spawnAllay(level, origin, new ItemStack(ModItems.SENT_ENVELOPE.get()));
                        if (allay == null) {
                            giveEnvelope(player);
                        } else {
                            phase = Phase.FLYING_AWAY;
                            ticks = 0;
                        }
                    }
                }
                case FLYING_AWAY -> {
                    Allay allay = findOrRestoreAllay(level);
                    if (allay == null || !allay.isAlive()) {
                        finish();
                        return;
                    }
                    moveAllay(allay, origin.lerp(destination, Math.min(1, ticks / (double) OUTBOUND_FLIGHT)));
                    if (ticks >= OUTBOUND_FLIGHT) {
                        allay.spawnAnim();
                        allay.discard();
                        allayId = null;
                        allayPosition = destination;
                        phase = Phase.WAITING_TO_RETURN;
                        ticks = 0;
                    }
                }
                case WAITING_TO_RETURN -> {
                    if (ticks >= RETURN_DELAY) {
                        boolean goldenFeather = LuckinessHelper.checkWhetherToGiveGoldenFeather(player);
                        Allay allay = spawnAllay(level, destination, new ItemStack(goldenFeather ? ModItems.MYSTERIOUS_ENVELOPE.get() : ModItems.ENVELOPE.get()));
                        if (allay == null) {
                            giveEnvelope(player);
                        } else {
                            phase = Phase.FLYING_HOME;
                            ticks = 0;
                        }
                    }
                }
                case FLYING_HOME -> {
                    Allay allay = findOrRestoreAllay(level);
                    if (allay == null || !allay.isAlive()) {
                        finish();
                        return;
                    }
                    Vec3 playerPosition = getPlayerPosition(player);
                    Vec3 delta = playerPosition.subtract(allay.position());
                    Vec3 nextPosition = allay.position();
                    if (delta.lengthSqr() > 1.0E-6) {
                        nextPosition = nextPosition.add(delta.normalize().scale(Math.min(0.3, delta.length())));
                    }
                    moveAllay(allay, nextPosition);
                    if (allay.distanceToSqr(playerPosition) <= 1.44) {
                        allay.spawnAtLocation(level, allay.getItemInHand(InteractionHand.MAIN_HAND).copy(), 0.25F);
                        allay.spawnAnim();
                        allay.discard();
                        finish();
                    } else if (tooFarToReach(playerPosition)) {
                        pauseActive(level, this);
                    }
                }
            }
        }

        private static Vec3 getPlayerPosition(ServerPlayer player) {
            return player.position().add(0, 1.2, 0);
        }

        private boolean tooFarToReach(ServerPlayer player) {
            return tooFarToReach(getPlayerPosition(player));
        }

        private boolean tooFarToReach(Vec3 playerPosition) {
            return allayPosition.distanceToSqr(playerPosition) >= FORCE_SENT_DISTANCE * FORCE_SENT_DISTANCE;
        }

        @Nullable
        private Allay spawnAllay(ServerLevel level, Vec3 position, ItemStack heldItem) {
            Allay allay = EntityType.ALLAY.create(level, EntitySpawnReason.EVENT);
            if (allay == null) {
                return null;
            }
            allay.setPos(position);
            allay.setNoAi(true);
            allay.setNoGravity(true);
            allay.setItemInHand(InteractionHand.MAIN_HAND, heldItem);
            level.addFreshEntity(allay);
            allayId = allay.getUUID();
            allayPosition = position;
            allay.spawnAnim();
            return allay;
        }

        @Nullable
        private Allay findOrRestoreAllay(ServerLevel level) {
            if (allayId != null) {
                Allay allay = searchAllayGlobally(level.getServer());
                if (allay != null) {
                    return allay;
                }
            }
            ItemStack held = new ItemStack(phase == Phase.FLYING_AWAY ? ModItems.STAMP.get() : ModItems.ENVELOPE.get());
            return spawnAllay(level, allayPosition, held);
        }

        private void moveAllay(Allay allay, Vec3 position) {
            Vec3 movement = position.subtract(allay.position());
            if (movement.lengthSqr() > 1.0E-8) {
                float yaw = (float) (Math.atan2(-movement.x, movement.z) * (180 / Math.PI));
                float pitch = (float) (Math.atan2(-movement.y, Math.sqrt(movement.x * movement.x + movement.z * movement.z)) * (180 / Math.PI));
                allay.setYRot(yaw);
                allay.setYBodyRot(yaw);
                allay.setYHeadRot(yaw);
                allay.setXRot(pitch);
            }
            allay.setPos(position);
            allay.setDeltaMovement(Vec3.ZERO);
            allayPosition = position;
        }

        @Nullable
        private Allay searchAllayGlobally(MinecraftServer server) {
            if (allayId == null) {
                return null;
            }
            for (ServerLevel level : server.getAllLevels()) {
                if (level.getEntity(allayId) instanceof Allay allay) {
                    return allay;
                }
            }
            return null;
        }

        private void giveEnvelope(ServerPlayer player) {
            boolean goldenFeather = LuckinessHelper.checkWhetherToGiveGoldenFeather(player);
            ItemStack envelope = new ItemStack(goldenFeather ? ModItems.MYSTERIOUS_ENVELOPE.get() : ModItems.ENVELOPE.get());
            Utils.giveItem(player, envelope);
            finish();
        }

        private void finish() {
            active = false;
            allayId = null;
            ticks = 0;
        }

        private void setPaused(boolean paused) {
            this.paused = paused;
        }

        public static final class Serializer implements IAttachmentSerializer<State> {
            public static final Serializer INSTANCE = new Serializer();

            private Serializer() {}

            @Override
            public boolean write(State state, ValueOutput output) {
                output.putBoolean("active", state.active);
                output.putString("dimension", state.dimension.identifier().toString());
                writeVec(output, "origin", state.origin);
                writeVec(output, "destination", state.destination);
                writeVec(output, "allay_position", state.allayPosition);
                output.putString("allay", state.allayId == null ? "null" : state.allayId.toString());
                output.putInt("ticks", state.ticks);
                output.putInt("phase", state.phase.id);
                output.putBoolean("paused", state.paused);
                return true;
            }

            @Override
            public State read(IAttachmentHolder holder, ValueInput input) {
                State state = new State();
                state.active = input.getBooleanOr("active", false);
                state.dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(input.getStringOr("dimension", Level.OVERWORLD.identifier().toString())));
                state.origin = readVec(input, "origin");
                state.destination = readVec(input, "destination");
                state.allayPosition = readVec(input, "allay_position");
                String uuid = input.getStringOr("allay", "null");
                try {
                    state.allayId = "null".equals(uuid) ? null : UUID.fromString(uuid);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Invalid allay UUID {}", uuid.isBlank() ? "(blank)" : uuid);
                    state.allayId = null;
                }
                state.ticks = Math.max(0, input.getIntOr("ticks", 0));
                state.phase = Phase.byId(input.getIntOr("phase", 0));
                state.paused = input.getBooleanOr("paused", false);
                return state;
            }

            private static Vec3 readVec(ValueInput input, String key) {
                return new Vec3(input.getDoubleOr(key + "_x", 0), input.getDoubleOr(key + "_y", 0), input.getDoubleOr(key + "_z", 0));
            }

            private static void writeVec(ValueOutput output, String key, Vec3 vector) {
                output.putDouble(key + "_x", vector.x);
                output.putDouble(key + "_y", vector.y);
                output.putDouble(key + "_z", vector.z);
            }
        }
    }

    private enum Phase {
        WAITING_TO_LEAVE(0),
        FLYING_AWAY(1),
        WAITING_TO_RETURN(2),
        FLYING_HOME(3);

        private final int id;

        Phase(int id) {
            this.id = id;
        }

        private static Phase byId(int id) {
            return switch (id) {
                case 1 -> FLYING_AWAY;
                case 2 -> WAITING_TO_RETURN;
                case 3 -> FLYING_HOME;
                default -> WAITING_TO_LEAVE;
            };
        }
    }
}
