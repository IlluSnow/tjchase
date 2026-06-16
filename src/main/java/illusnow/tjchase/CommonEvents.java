package illusnow.tjchase;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.HarpTester;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.item.HarpItem;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EventBusSubscriber(modid = TJChase.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().getBlockState(event.getEntity().getOnPos()).is(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES)) {
            event.setDamageMultiplier(0);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        // Does not need to sync here because the event is fired on both sides
        player.getData(ModAttachments.ORBITING_BLOCKS.get()).update();
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity && !entity.level().isClientSide()) {
            AngelTomPassive2Owner.mayUpdate(livingEntity);
            if (healthLocked((ServerLevel) livingEntity.level(), livingEntity, livingEntity.getMainHandItem(), null)) {
                Utils.sendTJChaseBuffParticles(livingEntity, 0.6F, 1, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        OrbitingBlockHolder orbitingBlocks = entity.getData(ModAttachments.ORBITING_BLOCKS);
        boolean invalidDamageSource = event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)
                || event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || event.getSource().is(DamageTypeTags.BYPASSES_ARMOR);
        if (!invalidDamageSource && !orbitingBlocks.isEmpty()) {
            OrbitingBlock block = orbitingBlocks.removeLookingBlock(entity.yHeadRot);
            entity.syncData(ModAttachments.ORBITING_BLOCKS);
            ServerLevel level = (ServerLevel) entity.level();
            Vec3 centerPos = block.calculateCenterWorldPos(entity, block.getYRot());
            if (OrbitingBlockEntity.BlockType.getTypeFor(block.getBlockState()) == OrbitingBlockEntity.BlockType.TNT) {
                OrbitingBlockEntity.applyExplosion(level, null, centerPos.x, centerPos.y, centerPos.z, false, HarpConstants.TNT_EXPLOSION_POWER_NEARBY, false, Level.ExplosionInteraction.NONE);
            } else {
                event.setAmount(Math.max(event.getAmount() - HarpConstants.ORBITING_BLOCK_DAMAGE_REDUCTION, event.getAmount() * HarpConstants.ORBITING_BLOCK_MIN_DAMAGE_TAKEN));
                OrbitingBlockEntity.addBreakEffects(level,
                        block.getBlockState(),
                        BlockPos.containing(centerPos),
                        null,
                        5 + entity.getRandom().nextDouble() * 2,
                        centerPos.x,
                        centerPos.y,
                        centerPos.z);
                if (orbitingBlocks.isEmpty() && entity instanceof Player player) {
                    int playCooldownTicksDecrease = player.getMainHandItem().is(ModItemTags.HARPS) ? HarpItem.getUseCooldownTicksDecrease(player.getRandom(), player.getMainHandItem()) : 0;
                    HarpItem.addHarpCooldown(player, HarpConstants.BASE_COOLDOWN - playCooldownTicksDecrease);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePreLowest(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = entity.getMainHandItem();
        ServerLevel level = (ServerLevel) entity.level();
        float minimum = getMinimumHealth(level, entity, stack);
        if (healthLocked(level, entity, stack, event.getSource())) {
            if (entity.getHealth() <= minimum) {
                event.setNewDamage(0);
            } else {
                float delta = entity.getHealth() - minimum;
                event.setNewDamage(Math.min(event.getNewDamage(), delta));
            }
        }
    }

    private static boolean healthLocked(ServerLevel level, LivingEntity entity, ItemStack holdingItem, @Nullable DamageSource source) {
        return (source == null || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) && getMinimumHealth(level, entity, holdingItem) > 0;
    }

    private static float getMinimumHealth(ServerLevel level, LivingEntity entity, ItemStack stack) {
        if (!entity.getData(ModAttachments.ORBITING_BLOCKS).isEmpty()) {
            return Utils.getItemEnchantmentValue(level, stack, 0, ModEnchantmentEffectComponents.MINIMUM_HEALTH_WHEN_BLOCKS_ORBITING.get());
        }
        return 0;
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (AngelTomPassive2Owner.hasBuff(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeAttackTarget(LivingChangeTargetEvent event) {
        handleHarpTester(event);
    }

    private static void handleHarpTester(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        @Nullable
        LivingEntity target = event.getNewAboutToBeSetTarget();
        Optional<EntityReference<HarpTester>> ref1 = attacker.getData(ModAttachments.HARP_TESTER);
        Optional<EntityReference<HarpTester>> ref2 = target == null ? Optional.empty() : target.getData(ModAttachments.HARP_TESTER);
        Optional<EntityReference<HarpTester>> ref3 = Optional.empty();
        if (target instanceof TraceableEntity traceable && traceable.getOwner() != null) {
            ref3 = traceable.getOwner().getData(ModAttachments.HARP_TESTER);
        }
        if (ref1.isEmpty() && ref2.isEmpty() && ref3.isEmpty()) {
            return;
        }
        HarpTester harpTester1 = ref1.map(ref -> EntityReference.get(ref, attacker.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        HarpTester harpTester2 = ref2.map(ref -> EntityReference.get(ref, target.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        HarpTester harpTester3 = ref3.map(ref -> EntityReference.get(ref, target.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        if (harpTester1 == harpTester2 || harpTester1 == harpTester3) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void checkCanAffect(MobEffectEvent.Applicable event) {
        if (AngelTomPassive2Owner.hasBuff(event.getEntity()) && event.getEffectInstance().getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
