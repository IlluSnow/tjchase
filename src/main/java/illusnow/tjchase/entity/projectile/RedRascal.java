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

package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class RedRascal extends AbstractRascal {
    private static final Identifier KNOCKBACK_RESISTANCE_MODIFIER_ID = TJChase.prefix("red_rascal_knockback_resistance_modifier");
    private static final AttributeModifier KNOCKBACK_RESISTANCE_MODIFIER = new AttributeModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final double KNOCKBACK_STRENGTH = 2;
    private static final int MAX_LIFE = 400;
    private static final int SLOWNESS_DURATION = 20 * 4;
    private static final int SLOWNESS_AMPLIFIER = 3;

    public RedRascal(EntityType<? extends RedRascal> type, Level level) {
        super(type, level);
    }

    private RedRascal(Entity owner, Level level) {
        super(ModEntities.RED_RASCAL.get(), owner, level);
    }

    private RedRascal(Level level, double x, double y, double z) {
        super(ModEntities.RED_RASCAL.get(), level, x, y, z);
    }

    public static RedRascal fromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return new RedRascal(owner, level);
    }

    public static RedRascal fromPos(Level level, double x, double y, double z, ItemStack stack) {
        return new RedRascal(level, x, y, z);
    }

    @Override
    protected void updateDamage(@Nullable Entity owner) {
        setDamage(8);
    }

    @Override
    protected void postHurt(Entity entity) {
        if (entity instanceof LivingEntity living) {
            Utils.checkAndGetAttribute(living, Attributes.KNOCKBACK_RESISTANCE).addTransientModifier(KNOCKBACK_RESISTANCE_MODIFIER);
            living.knockback(KNOCKBACK_STRENGTH, -getDeltaMovement().x, -getDeltaMovement().z);
            Utils.checkAndGetAttribute(living, Attributes.KNOCKBACK_RESISTANCE).removeModifier(KNOCKBACK_RESISTANCE_MODIFIER_ID);
            living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, EntityEvent.DEATH);
            playHitSound(0.5F);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == EntityEvent.DEATH) {
            for (int i = 0; i < 8; i++) {
                level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, ModItems.RED_RASCAL.get().getDefaultInstance()),
                        getBoundingBox().getCenter().x,
                        getBoundingBox().getCenter().y,
                        getBoundingBox().getCenter().z,
                        (random.nextFloat() - 0.5) * 0.08,
                        (random.nextFloat() - 0.5) * 0.08,
                        (random.nextFloat() - 0.5) * 0.08
                );
            }
        }
    }

    @Override
    protected int getMaxLife() {
        return MAX_LIFE;
    }

    @Override
    protected double getInertia() {
        return 1;
    }

    @Override
    protected double getWaterInertia() {
        return 1;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }
}
