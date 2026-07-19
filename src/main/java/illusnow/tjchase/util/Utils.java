package illusnow.tjchase.util;

import illusnow.tjchase.particle.ModParticleTypes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ConditionalEffect;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Utils {
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

    public static void setTarget(Mob attacker, @Nullable LivingEntity target) {
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
}
