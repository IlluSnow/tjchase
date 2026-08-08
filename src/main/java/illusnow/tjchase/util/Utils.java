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

package illusnow.tjchase.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illusnow.tjchase.particle.ModParticleTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Utils {
    public static final AABB ZERO_AABB = new AABB(0, 0, 0, 0, 0, 0);
    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("minX").forGetter(aabb -> aabb.minX),
                Codec.DOUBLE.fieldOf("minY").forGetter(aabb -> aabb.minY),
                Codec.DOUBLE.fieldOf("minZ").forGetter(aabb -> aabb.minZ),
                Codec.DOUBLE.fieldOf("maxX").forGetter(aabb -> aabb.maxX),
                Codec.DOUBLE.fieldOf("maxY").forGetter(aabb -> aabb.maxY),
                Codec.DOUBLE.fieldOf("maxZ").forGetter(aabb -> aabb.maxZ)
            ).apply(instance, AABB::new)
    );
    public static final StreamCodec<ByteBuf, AABB> AABB_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, aabb -> aabb.minX,
            ByteBufCodecs.DOUBLE, aabb -> aabb.minY,
            ByteBufCodecs.DOUBLE, aabb -> aabb.minZ,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxX,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxY,
            ByteBufCodecs.DOUBLE, aabb -> aabb.maxZ,
            AABB::new
    );

    private Utils() {}

    public static int getFixedEnchantmentIntValue(RandomSource random, ItemStack stack, int initialValue, DataComponentType<EnchantmentValueEffect> type) {
        MutableFloat value = new MutableFloat(initialValue);
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, enchantmentLevel) ->
                enchantment.value().modifyUnfilteredValue(type, random, enchantmentLevel, value));
        return value.intValue();
    }

    public static float getItemEnchantmentValue(ServerLevel level, ItemStack stack, float initialValue, DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> type) {
        MutableFloat value = new MutableFloat(initialValue);
        EnchantmentHelper.runIterationOnItem(stack, (enchantment, enchantmentLevel) ->
                enchantment.value().modifyItemFilteredCount(type, level, enchantmentLevel, stack, value));
        return value.floatValue();
    }

    public static void sendTJChaseBuffParticles(Entity entity, float r, float g, float b) {
        sendTJChaseBuffParticles(entity, r, g, b, 0.4);
    }

    public static void sendTJChaseBuffParticles(Entity entity, float r, float g, float b, double frequency) {
        sendTJChaseBuffParticles(entity, r, g, b, true, frequency);
    }

    public static void sendTJChaseBuffParticles(Entity entity, float r, float g, float b, boolean alwaysShow, double frequency) {
        if (!EntitySelector.ENTITY_STILL_ALIVE.test(entity) || !EntitySelector.NO_SPECTATORS.test(entity)) {
            return;
        }
        RandomSource random = entity.getRandom();
        if (random.nextDouble() >= frequency) {
            return;
        }
        ServerLevel level = (ServerLevel) entity.level();
        float angle = random.nextFloat() * 2 * Mth.PI;
        level.sendParticles(
                ColorParticleOption.create(ModParticleTypes.TJCHASE_BUFF.get(), r, g, b),
                alwaysShow,
                alwaysShow,
                entity.getX() + Mth.cos(angle) * entity.getBbWidth() * (1 + random.nextDouble() * 0.2),
                entity.getY(0.1 + random.nextDouble() * 0.55),
                entity.getZ() + Mth.sin(angle) * entity.getBbWidth() * (1 + random.nextDouble() * 0.2),
                0,
                0,
                0.075,
                0,
                1
        );
    }

    public static void setTarget(@Nullable Mob attacker, @Nullable LivingEntity target) {
        if (attacker == null) {
            return;
        }
        if (attacker.getTarget() == target) {
            return;
        }
        if (attacker.getBrain().isBrainDead()) {
            attacker.setTarget(target);
        } else {
            attacker.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, Optional.ofNullable(target));
        }
    }

    public static void clearNegativeEffectsAndFire(LivingEntity entity) {
        new ArrayList<>(entity.getActiveEffects()).stream()
                .map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                .forEach(entity::removeEffect);
        entity.clearFire();
    }

    public static void saveAABB(AABB aabb, ValueOutput output, String name) {
        output.store(name, AABB_CODEC, aabb);
    }

    public static Optional<AABB> loadAABB(ValueInput input, String name) {
        return input.read(name, AABB_CODEC);
    }

    public static boolean noPhysics(LivingEntity entity) {
        return entity.isNoGravity() || entity.noPhysics;
    }

    public static boolean canFly(LivingEntity entity) {
        if (entity instanceof FlyingAnimal) {
            return true;
        }
        if (entity.getAttribute(Attributes.FLYING_SPEED) != null) {
            return true;
        }
        if (entity instanceof Mob mob && mob.getMoveControl() instanceof FlyingMoveControl) {
            return true;
        }
        if (entity instanceof Phantom || entity instanceof Vex) {
            return true;
        }
        return noPhysics(entity);
    }

    public static boolean isAquatic(Entity entity) {
        return entity.getType().is(EntityTypeTags.AQUATIC);
    }

    @SuppressWarnings("deprecation")
    public static Vec3 tryMoveDownToGround(Level level, Vec3 pos, int maxTries) {
        BlockPos.MutableBlockPos blockPos = BlockPos.containing(pos).mutable();
        int tries = 0;
        while (!level.getBlockState(blockPos.below()).isSolid()) {
            blockPos.move(Direction.DOWN);
            tries++;
            if (tries > maxTries) {
                return pos;
            }
        }
        return new Vec3(pos.x, blockPos.getY(), pos.z);
    }
}
