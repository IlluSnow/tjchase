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

package illusnow.tjchase.entity.dataentity;

import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
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

import java.util.ArrayList;
import java.util.List;
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
    private int life;

    public BlueprintManager(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean isEntityInsideBlueprint(@Nullable Entity entity) {
        return !getAllBlueprintsOf(entity).isEmpty();
    }

    public static boolean isEntityInsideOwnedBlueprint(@Nullable Entity entity) {
        return getBlueprintOf(entity, blueprintManager -> blueprintManager.getOwner() == entity) != null;
    }

    public static List<BlueprintManager> getAllBlueprintsOf(@Nullable Entity entity) {
        if (entity == null || !hasAnyBlueprintsIn(entity.level())) {
            return List.of();
        }
        return getBlueprintsOf(entity, Predicates.alwaysTrue());
    }

    public static List<BlueprintManager> getBlueprintsOf(@Nullable Entity entity, Predicate<? super BlueprintManager> condition) {
        List<BlueprintManager> blueprintManagers = new ArrayList<>();
        if (entity != null) {
            for (BlueprintManager manager : getNearbyBlueprints(entity.level(), entity.getBoundingBox().inflate(BlueprintManager.MAX_LENGTH_ALLOWED))) {
                if (manager.isEntityInside(entity) && manager.isValid() && condition.test(manager)) {
                    blueprintManagers.add(manager);
                }
            }
        }
        return blueprintManagers;
    }

    @Nullable
    public static BlueprintManager getBlueprintOf(@Nullable Entity entity, Predicate<? super BlueprintManager> condition) {
        List<BlueprintManager> blueprints = getBlueprintsOf(entity, condition);
        return blueprints.isEmpty() ? null : blueprints.getFirst();
    }

    public static List<BlueprintManager> getNearbyBlueprints(Level level, AABB range) {
        if (!hasAnyBlueprintsIn(level)) {
            return List.of();
        }
        return level.getEntitiesOfClass(BlueprintManager.class, range);
    }

    public static boolean hasAnyBlueprintsIn(Level level) {
        return level.getData(ModAttachments.BLUEPRINT_LIVE_COUNT).get() > 0;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        level().getData(ModAttachments.BLUEPRINT_LIVE_COUNT).incrementAndGet();
    }

    @Override
    public void onRemovedFromLevel() {
        super.onRemovedFromLevel();
        level().getData(ModAttachments.BLUEPRINT_LIVE_COUNT).decrementAndGet();
    }

    public static void updateBlueprintData(LivingEntity livingEntity) {
        List<BlueprintManager> blueprints = getAllBlueprintsOf(livingEntity);
        boolean insideOwnedBlueprint = blueprints.stream().anyMatch(blueprintManager -> blueprintManager.getOwner() == livingEntity);
        boolean hadNegativeEffect = livingEntity.getData(ModAttachments.NEGATIVE_EFFECT_BLUEPRINT);
        boolean hadPositiveEffect = livingEntity.getData(ModAttachments.POSITIVE_EFFECT_BLUEPRINT);
        boolean shouldHavePositiveEffect = insideOwnedBlueprint;
        boolean shouldHaveNegativeEffect = !blueprints.isEmpty() && blueprints.stream().anyMatch(blueprintManager -> blueprintManager.getOwner() != livingEntity);
        livingEntity.setData(ModAttachments.NEGATIVE_EFFECT_BLUEPRINT, shouldHaveNegativeEffect);
        livingEntity.setData(ModAttachments.POSITIVE_EFFECT_BLUEPRINT, shouldHavePositiveEffect);
        if (hadNegativeEffect && !shouldHaveNegativeEffect) {
            mobLeaves(livingEntity, false, true);
        }
        if (hadPositiveEffect && !shouldHavePositiveEffect) {
            mobLeaves(livingEntity, true, false);
        }
        if (!hadNegativeEffect && shouldHaveNegativeEffect) {
            mobEnters(livingEntity, false, true);
        }
        if (!hadPositiveEffect && shouldHavePositiveEffect) {
            mobEnters(livingEntity, true, false);
        }
        if (!blueprints.isEmpty()) {
            mobInsideBlueprint(livingEntity, shouldHavePositiveEffect);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            if (getConstructTime() > CONSTRUCT_TIME_LINE + CONSTRUCT_TIME_MAIN) {
                if (life > 0) {
                    life--;
                }
                if (life <= 0) {
                    if (life == 0) {
                        resetDisappearTimestamp();
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

    public static void mobInsideBlueprint(LivingEntity entity, boolean positiveEffects) {
        if (positiveEffects) {
            Utils.sendTJChaseBuffParticles(entity, 0.6F, 0.8F, 1);
            if (entity.tickCount % HEAL_INTERVAL == 0) {
                entity.heal(1);
            }
        }
    }

    public static void mobLeaves(LivingEntity entity, boolean removePositiveEffects, boolean removeNegativeEffects) {
        if (removePositiveEffects) {
            AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && speed.hasModifier(SPEED_MODIFIER_OWNER_INSIDE_ID)) {
                speed.removeModifier(SPEED_MODIFIER_OWNER_INSIDE);
            }
        }
        if (removeNegativeEffects) {
            // Currently unused
        }
    }

    public static void mobEnters(LivingEntity entity, boolean addPositiveEffects, boolean addNegativeEffects) {
        if (addPositiveEffects) {
            AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null && !speed.hasModifier(SPEED_MODIFIER_OWNER_INSIDE_ID)) {
                speed.addTransientModifier(SPEED_MODIFIER_OWNER_INSIDE);
            }
        }
        if (addNegativeEffects) {
            // Currently unused
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
