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

import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class BlueprintManager extends DataEntity implements TraceableEntity {
    private static final String BLUEPRINT_AABB_TAG = "BlueprintAABB";
    private static final String LIFE_TAG = "Life";
    private static final String CONSTRUCT_TIME_TAG = "ConstructTime";
    private static final String DISAPPEAR_TIME_TAG = "DisappearTime";
    private static final String OWNER_TAG = "Owner";
    private static final EntityDataAccessor<AABB> BLUEPRINT_AABB = SynchedEntityData.defineId(BlueprintManager.class, ModEntityDataSerializers.AABB.get());
    private static final EntityDataAccessor<Long> CONSTRUCT_TIMESTAMP = SynchedEntityData.defineId(BlueprintManager.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Long> DISAPPEAR_TIMESTAMP = SynchedEntityData.defineId(BlueprintManager.class, EntityDataSerializers.LONG);
    private static final Identifier SPEED_MODIFIER_OWNER_INSIDE_ID = TJChase.prefix("owner_inside");
    private static final AttributeModifier SPEED_MODIFIER_OWNER_INSIDE = new AttributeModifier(
            SPEED_MODIFIER_OWNER_INSIDE_ID, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    );
    public static final int CONSUME_TICKS_DIVISOR = 3; // 200% faster
    public static final int CONSTRUCT_TIME_LINE = 8;
    public static final int CONSTRUCT_TIME_MAIN = 8;
    public static final float INSIDE_DAMAGE_REDUCTION = 0.2F;
    private static final int MAX_LIFE = 20 * 30;
    private static final int HEAL_INTERVAL = 30;
    public static final double MAX_LENGTH_ALLOWED = 30;
    @Nullable
    private EntityReference<Player> owner;
    private final Set<Player> playersInside = new HashSet<>();
    private int life;

    public BlueprintManager(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean isEntityInsideBlueprint(@Nullable Entity entity) {
        return getBlueprintOf(entity) != null;
    }

    @Nullable
    public static BlueprintManager getBlueprintOf(@Nullable Entity entity) {
        return getBlueprintOf(entity, Predicates.alwaysTrue());
    }

    @Nullable
    public static BlueprintManager getBlueprintOf(@Nullable Entity entity, Predicate<? super BlueprintManager> condition) {
        if (entity != null) {
            for (BlueprintManager manager : getNearbyBlueprints(entity.level(), entity.getBoundingBox().inflate(BlueprintManager.MAX_LENGTH_ALLOWED))) {
                if (manager.isEntityInside(entity) && manager.isValid() && condition.test(manager)) {
                    return manager;
                }
            }
        }
        return null;
    }

    public static List<BlueprintManager> getNearbyBlueprints(Level level, AABB range) {
        return level.getEntitiesOfClass(BlueprintManager.class, range);
    }

    public <T extends Entity> List<T> getEntitiesInsideOfClass(Class<T> type) {
        return level().getEntitiesOfClass(type, getBlueprintAABB());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (getConstructTime() > CONSTRUCT_TIME_LINE + CONSTRUCT_TIME_MAIN) {
                if (life > 0) {
                    Set<Player> actualPlayersInside = new HashSet<>(getEntitiesInsideOfClass(Player.class));
                    updatePlayersInside(actualPlayersInside);
                    actualPlayersInside.forEach(this::playerInside);
                    life--;
                }
                if (life <= 0) {
                    if (life == 0) {
                        resetDisappearTimestamp();
                        playersInside.forEach(this::playerLeaves);
                        playersInside.clear();
                        playSound(ModSoundEvents.BLUEPRINT_FOLD.get());
                        life = -1;
                    }
                    if (getDisappearTime() >= CONSTRUCT_TIME_LINE + CONSTRUCT_TIME_MAIN) {
                        discard();
                    }
                }
            } else if (getConstructTime() == CONSTRUCT_TIME_LINE + CONSTRUCT_TIME_MAIN) {
                life = MAX_LIFE;
            }
        }
    }

    private void updatePlayersInside(Set<Player> actualPlayersInside) {
        for (Iterator<Player> iterator = playersInside.iterator(); iterator.hasNext(); ) {
            Player originalPlayer = iterator.next();
            if (!actualPlayersInside.contains(originalPlayer)) {
                iterator.remove();
                playerLeaves(originalPlayer);
            }
        }
        for (Player newPlayer : actualPlayersInside) {
            if (!playersInside.contains(newPlayer)) {
                playersInside.add(newPlayer);
                playerEnters(newPlayer);
            }
        }
    }

    private void playerInside(Player player) {
        if (player == getOwner()) {
            Utils.sendTJChaseBuffParticles(player, 0.6F, 0.8F, 1);
            if (player.tickCount % HEAL_INTERVAL == 0) {
                player.heal(1);
            }
        }
    }

    private void playerLeaves(Player player) {
        if (player == getOwner()) {
            if (getNearbyBlueprints(player.level(), player.getBoundingBox().inflate(BlueprintManager.MAX_LENGTH_ALLOWED))
                    .stream()
                    .filter(BlueprintManager::isValid)
                    .filter(blueprintManager -> blueprintManager != this && blueprintManager.isEntityInside(player))
                    .noneMatch(blueprintManager -> blueprintManager.getOwner() == player)) {
                AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                if (speed != null && speed.hasModifier(SPEED_MODIFIER_OWNER_INSIDE_ID)) {
                    speed.removeModifier(SPEED_MODIFIER_OWNER_INSIDE);
                }
            }
        }
    }

    private void playerEnters(Player player) {
        if (player == getOwner()) {
            AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(SPEED_MODIFIER_OWNER_INSIDE_ID)) {
                speed.addTransientModifier(SPEED_MODIFIER_OWNER_INSIDE);
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLUEPRINT_AABB, Utils.ZERO_AABB);
        builder.define(CONSTRUCT_TIMESTAMP, 0L);
        builder.define(DISAPPEAR_TIMESTAMP, Long.MIN_VALUE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        Utils.saveAABB(getBlueprintAABB(), output, BLUEPRINT_AABB_TAG);
        output.putInt(LIFE_TAG, life);
        output.putLong(CONSTRUCT_TIME_TAG, entityData.get(CONSTRUCT_TIMESTAMP));
        output.putLong(DISAPPEAR_TIME_TAG, entityData.get(DISAPPEAR_TIMESTAMP));
        EntityReference.store(owner, output, OWNER_TAG);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setBlueprintAABB(Utils.loadAABB(input, BLUEPRINT_AABB_TAG).orElse(Utils.ZERO_AABB));
        life = input.getIntOr(LIFE_TAG, 0);
        setConstructTimestamp(input.getLongOr(CONSTRUCT_TIME_TAG, 0));
        setDisappearTimestamp(input.getLongOr(DISAPPEAR_TIME_TAG, Long.MIN_VALUE));
        owner = EntityReference.read(input, OWNER_TAG);
    }

    @Override
    protected double getRenderDistance() {
        return 128;
    }

    public boolean isEntityInside(Entity entity) {
        return entity.getBoundingBox().intersects(getBlueprintAABB());
    }

    public boolean isPositionInside(Vec3 pos) {
        return getBlueprintAABB().contains(pos);
    }

    public boolean isValid() {
        if (level().isClientSide()) {
            return getConstructTime() >= CONSTRUCT_TIME_LINE + CONSTRUCT_TIME_MAIN && getDisappearTime() <= 0;
        }
        return life > 0;
    }

    public int getConstructTime() {
        return Ints.saturatedCast(level().getGameTime() - entityData.get(CONSTRUCT_TIMESTAMP));
    }

    private void setConstructTimestamp(long constructTimestamp) {
        entityData.set(CONSTRUCT_TIMESTAMP, constructTimestamp);
    }

    public void resetConstructTimestamp() {
        setConstructTimestamp(level().getGameTime());
    }

    public int getDisappearTime() {
        long disappearTimestamp = entityData.get(DISAPPEAR_TIMESTAMP);
        if (disappearTimestamp == Long.MIN_VALUE) {
            return 0;
        }
        return Ints.saturatedCast(level().getGameTime() - disappearTimestamp);
    }

    private void setDisappearTimestamp(long disappearTimestamp) {
        entityData.set(DISAPPEAR_TIMESTAMP, disappearTimestamp);
    }

    private void resetDisappearTimestamp() {
        setDisappearTimestamp(level().getGameTime());
    }

    public AABB getBlueprintAABB() {
        return entityData.get(BLUEPRINT_AABB);
    }

    private void setBlueprintAABB(AABB blueprintAABB) {
        entityData.set(BLUEPRINT_AABB, blueprintAABB);
        snapTo(blueprintAABB.getCenter());
    }

    public void setBlueprintAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (Math.abs(maxX - minX) > MAX_LENGTH_ALLOWED || Math.abs(maxY - minY) > MAX_LENGTH_ALLOWED || Math.abs(maxZ - minZ) > MAX_LENGTH_ALLOWED) {
            throw new IllegalArgumentException("Too large size for BlueprintAABB, x: %.2f, y: %.2f, z: %.2f".formatted(Math.abs(maxX - minX), Math.abs(maxY - minY), Math.abs(maxZ - minZ)));
        }
        setBlueprintAABB(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Nullable
    @Override
    public Player getOwner() {
        return EntityReference.getPlayer(owner, level());
    }

    public void setOwner(Player owner) {
        this.owner = EntityReference.of(owner);
    }
}
