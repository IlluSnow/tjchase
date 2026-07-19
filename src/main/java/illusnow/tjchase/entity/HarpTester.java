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

package illusnow.tjchase.entity;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class HarpTester extends DataEntity implements Targeting {
    public static final String NAME = TJChase.prefixMsg("monster_tsunami");
    public static final String NAME_REMAINING = TJChase.prefixMsg("monster_tsunami.remaining");
    private static final int WAVE_INTERVAL = 100;
    private static final String WAVE_MEMBERS_ALIVE_TAG = "WaveMembersAlive";
    private static final String WAVE_MEMBER_TAG = "WaveMember";
    private static final String TARGET_TAG = "Target";
    private static final String CURRENT_WAVE_TAG = "CurrentWave";
    private static final String MAX_HEALTH_TAG = "MaxHealth";
    private static final String BETWEEN_WAVES_TAG = "BetweenWaves";
    private static final String FINAL_WAVE_TAG = "FinalWave";
    private static final String BETWEEN_WAVE_TIME_TAG = "BetweenWaveTime";
    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.translatable(NAME), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private final List<EntityReference<Mob>> waveMembersAlive = new ArrayList<>();
    private final int maxWave = Wave.WAVES.size();
    @Nullable
    private EntityReference<Player> target;
    private int currentWave;
    private float maxHealth;
    private boolean betweenWaves = true;
    private boolean finalWave;
    private int betweenWaveTime;
    private int finalWaveTicks;

    public HarpTester(EntityType<?> entityType, Level level) {
        super(entityType, level);
        bossEvent.setProgress(0);
    }

    public static List<HarpTester> getHarpTestersNearby(Level level, Entity center) {
        return level.getEntitiesOfClass(HarpTester.class, center.getBoundingBox().inflate(120));
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            Player target = getTarget();
            if (level().getDifficulty() == Difficulty.PEACEFUL || target == null || !target.isAlive()) {
                for (Mob mob : seeWaveMembersAlive()) {
                    mob.discard();
                }
                waveMembersAlive.clear();
                maxHealth = 0;
                discard();
                return;
            }
            serverTick((ServerLevel) level(), target);
        }
    }

    protected void serverTick(ServerLevel level, Player target) {
        if (tickCount % 20 == 0) {
            setPos(target.position());
        }
        if (finalWave) {
            finalWaveTicks++;
        }
        seeWaveMembersAlive().forEach(mob -> Utils.setTarget(mob, target));
        if (betweenWaves) {
            betweenWaveTime++;
            bossEvent.setName(Component.translatable(NAME));
            bossEvent.setProgress((float) betweenWaveTime / WAVE_INTERVAL);
            if (betweenWaveTime >= WAVE_INTERVAL) {
                betweenWaveTime = 0;
                betweenWaves = false;
                currentWave++;
                if (currentWave == maxWave) {
                    finalWave = true;
                }
                setPos(target.position());
                spawn(level, target);
            }
            return;
        }
        waveMembersAlive.removeIf(this::nonAlive);
        float health = (float) seeWaveMembersAlive().stream().mapToDouble(LivingEntity::getHealth).sum();
        bossEvent.setProgress(health / maxHealth);
        target.displayClientMessage(Component.literal("%.1f".formatted(health))
                .withColor(getColor(health / maxHealth))
                .append(Component.literal(" / %.1f".formatted(maxHealth)).withColor(16777215)), true);
        if (finalWaveTicks >= 20) {
            bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
        }
        if (!finalWave && waveMembersAlive.size() <= 20) {
            bossEvent.setName(Component.translatable(NAME_REMAINING, waveMembersAlive.size()));
            seeWaveMembersAlive().forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false)));
        } else {
            bossEvent.setName(Component.translatable(NAME));
        }
        if (finalWave) {
            seeWaveMembersAlive().forEach(mob -> mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0, false, false)));
        }
        if (waveMembersAlive.isEmpty()) {
            if (finalWave) {
                discard();
            } else {
                betweenWaves = true;
                betweenWaveTime = 0;
            }
        }
    }

    private int getColor(float progress) {
        return Mth.hsvToRgb(Mth.clamp(progress / 3, 0, 1F / 3), 0.2F, 1);
    }

    protected boolean nonAlive(EntityReference<Mob> ref) {
        Mob mob = EntityReference.get(ref, level(), Mob.class);
        return mob == null || !mob.isAlive();
    }

    protected void spawn(ServerLevel level, Player target) {
        maxHealth = 0;
        Wave wave = Wave.WAVES.get(currentWave - 1);
        for (WaveMember<?> waveMember : wave.waveMembers()) {
            for (int i = 0; i < waveMember.count(); i++) {
                Mob mob = waveMember.spawn(this, level, findSpawnPos(waveMember.type()), target);
                if (mob != null && mob.isAlive()) {
                    addWaveMemberAlive(mob);
                    mob.setHealth(mob.getMaxHealth());
                    maxHealth += mob.getMaxHealth();
                }
            }
        }
    }

    protected BlockPos findSpawnPos(EntityType<?> entityType) {
        final double minDistance = 20;
        final double maxDistance = 40;
        final int attempts = 10;
        for (int i = 0; i < attempts; i++) {
            double distance = random.nextDouble() * (maxDistance - minDistance) + minDistance;
            float angle = random.nextFloat() * 2 * Mth.PI;
            double x = getX() + Mth.cos(angle) * distance;
            double z = getZ() + Mth.sin(angle) * distance;
            BlockPos spawnPos = new BlockPos((int) Math.rint(x), 0, (int) Math.rint(z));
            int y = level().getHeight(Heightmap.Types.WORLD_SURFACE, spawnPos);
            spawnPos = spawnPos.above(y);
            if (SpawnPlacements.getPlacementType(entityType).isSpawnPositionOk(level(), spawnPos, entityType)) {
                return spawnPos;
            }
        }
        return blockPosition();
    }

    public List<Mob> seeWaveMembersAlive() {
        return waveMembersAlive.stream().map(mob -> EntityReference.get(mob, level(), Mob.class)).filter(Objects::nonNull).toList();
    }

    protected void addWaveMemberAlive(Mob mob) {
        EntityReference<Mob> ref = EntityReference.of(mob);
        waveMembersAlive.add(ref);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        var waveMembersAliveInputList = input.childrenListOrEmpty(WAVE_MEMBERS_ALIVE_TAG);
        for (var waveMemberAliveInput : waveMembersAliveInputList) {
            waveMemberAliveInput.read(WAVE_MEMBER_TAG, EntityReference.<Mob>codec()).ifPresent(waveMembersAlive::add);
        }
        input.read(TARGET_TAG, EntityReference.<Player>codec()).ifPresent(ref -> target = ref);
        currentWave = input.getIntOr(CURRENT_WAVE_TAG, 0);
        maxHealth = input.getFloatOr(MAX_HEALTH_TAG, 0);
        betweenWaves = input.getBooleanOr(BETWEEN_WAVES_TAG, true);
        finalWave = input.getBooleanOr(FINAL_WAVE_TAG, false);
        betweenWaveTime = input.getIntOr(BETWEEN_WAVE_TIME_TAG, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        ValueOutput.ValueOutputList outputList = output.childrenList(WAVE_MEMBERS_ALIVE_TAG);
        for (EntityReference<Mob> ref : waveMembersAlive) {
            ValueOutput child = outputList.addChild();
            child.store(WAVE_MEMBER_TAG, EntityReference.codec(), ref);
        }
        if (target != null) {
            output.store(TARGET_TAG, EntityReference.codec(), target);
        }
        output.putInt(CURRENT_WAVE_TAG, currentWave);
        output.putFloat(MAX_HEALTH_TAG, maxHealth);
        output.putBoolean(BETWEEN_WAVES_TAG, betweenWaves);
        output.putBoolean(FINAL_WAVE_TAG, finalWave);
        output.putInt(BETWEEN_WAVE_TIME_TAG, betweenWaveTime);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        bossEvent.removePlayer(serverPlayer);
    }

    @Nullable
    @Override
    public Player getTarget() {
        return EntityReference.getPlayer(target, level());
    }

    public void setTarget(Player target) {
        this.target = EntityReference.of(target);
        if (target instanceof ServerPlayer serverPlayer) {
            bossEvent.addPlayer(serverPlayer);
        }
    }

    public record Wave(List<WaveMember<?>> waveMembers) {
        public static final List<Wave> WAVES = List.of(
                new Wave(List.of(
                        new WaveMember<> (
                                EntityType.ZOMBIE,
                                100,
                                zombie -> {
                                    if (zombie.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
                                        ItemStack stack = new ItemStack(Items.LEATHER_HELMET);
                                        DyedItemColor.applyDyes(stack, List.of(DyeItem.byColor(DyeColor.byId(zombie.getRandom().nextInt(16)))));
                                        zombie.setItemSlot(EquipmentSlot.HEAD, stack);
                                    }
                                }
                        )
                )),
                new Wave(List.of(
                        new WaveMember<>(
                                EntityType.RAVAGER,
                                10,
                                ravager -> {}
                        ),
                        new WaveMember<>(
                                EntityType.VINDICATOR,
                                15,
                                vindicator -> {}
                        ),
                        new WaveMember<> (
                                EntityType.CAVE_SPIDER,
                                30,
                                caveSpider -> {}
                        )
                )),
                new Wave(List.of(
                        new WaveMember<>(
                                EntityType.WARDEN,
                                1,
                                15,
                                25,
                                warden -> {}
                        )
                ))
        );
    }

    public record WaveMember<T extends Mob>(EntityType<T> type, int count, double minDistance, double maxDistance, Consumer<? super T> modifier) {
        public WaveMember(EntityType<T> type, int count, Consumer<? super T> modifier) {
            this(type, count, 20, 40, modifier);
        }

        @Nullable
        private T spawn(HarpTester harpTester, ServerLevel level, BlockPos pos, LivingEntity target) {
            @Nullable
            T entity = type.create(level, EntitySpawnReason.EVENT);
            if (entity != null) {
                entity.snapTo(pos.getBottomCenter());
                entity.setData(ModAttachments.HARP_TESTER, Optional.of(EntityReference.of(harpTester)));
                EventHooks.finalizeMobSpawn(entity, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.EVENT, null);
                level.addFreshEntityWithPassengers(entity);
                Utils.setTarget(entity, target);
                entity.spawnAnim();
                entity.lookAt(target, 360, 360);
                entity.setYBodyRot(entity.getYRot());
                entity.setYHeadRot(entity.getYRot());
                modifier.accept(entity);
            }
            return entity;
        }
    }
}
