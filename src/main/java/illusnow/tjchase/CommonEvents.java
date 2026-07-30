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

package illusnow.tjchase;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.command.TJChaseCommand;
import illusnow.tjchase.entity.*;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.entity.projectile.YogaBall;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@EventBusSubscriber(modid = TJChase.MODID)
public class CommonEvents {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        TJChaseCommand.register(event.getDispatcher());
    }

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
            if (getLockedHealthFromAngelTomWeapon3((ServerLevel) livingEntity.level(), livingEntity, livingEntity.getMainHandItem()) > 0) {
                Utils.sendTJChaseBuffParticles(livingEntity, 0.6F, 1, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof YogaBall && event.getRayTraceResult().getType() == HitResult.Type.ENTITY) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        handleOrbitingBlocksDamageReduction(event);
        handleTJChaseCharacterDamageReductionStart(event);
        handleBlueprintDamageReduction(event);
    }

    private static void handleBlueprintDamageReduction(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (BlueprintManager.getBlueprintOf(player, blueprintManager -> blueprintManager.getOwner() == player) != null) {
                if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                        && !event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                        && !event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)){
                    event.setAmount(event.getAmount() * (1 - BlueprintManager.INSIDE_DAMAGE_REDUCTION));
                }
            }
        }
    }

    private static void handleTJChaseCharacterDamageReductionStart(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof TJChaseCharacter tj) {
            event.setAmount(tj.getReducedStartDamage(event.getSource(), event.getAmount(), event.getOriginalAmount()));
        }
    }

    private static void handleOrbitingBlocksDamageReduction(LivingIncomingDamageEvent event) {
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

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        handleTJChaseCharacterDamageReductionFinal(event);
        if (event.getSource().getEntity() instanceof Linia) {
            event.setNewDamage(Math.min(event.getNewDamage(), Linia.MAX_ATTACK_DAMAGE));
        }
    }

    private static void handleTJChaseCharacterDamageReductionFinal(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof TJChaseCharacter tj) {
            event.setNewDamage(tj.getReducedFinalDamage(event.getSource(), event.getNewDamage(), event.getOriginalDamage()));
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

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof TJChaseCharacter tjc) {
            tjc.awardDamageProficiencyPoints(event.getEntity(), event.getNewDamage());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamagePostLowest(LivingDamageEvent.Post event) {
        handleDamageInBlueprint(event);
    }

    private static void handleDamageInBlueprint(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && BlueprintManager.isEntityInsideBlueprint(mob) && event.getNewDamage() > 0 && Linia.isConvertible(mob)) {
            Linia.convert(mob);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof TJChaseCharacter tjc) {
            tjc.awardKillProficiencyPoints(event.getEntity(), event.getEntity().getMaxHealth());
        }
    }

    private static boolean healthLocked(ServerLevel level, LivingEntity entity, ItemStack holdingItem, @Nullable DamageSource source) {
        return (source == null || !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) && getMinimumHealth(level, entity, holdingItem) > 0;
    }

    private static float getMinimumHealth(ServerLevel level, LivingEntity entity, ItemStack stack) {
        float lockedHealthFromAngelTomWeapon3 = getLockedHealthFromAngelTomWeapon3(level, entity, stack);
        if (lockedHealthFromAngelTomWeapon3 > 0) {
            return lockedHealthFromAngelTomWeapon3;
        }
        if (entity instanceof HealthLockable healthLockable && healthLockable.processDamageInEventListeners()) {
            return healthLockable.getLockedHealth();
        }
        return 0;
    }

    private static float getLockedHealthFromAngelTomWeapon3(ServerLevel level, LivingEntity entity, ItemStack stack) {
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
        if (event.getEntity() instanceof TJChaseCharacter tj && tj.hasSuperArmor()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeAttackTarget(LivingChangeTargetEvent event) {
        handleHarpTester(event);
        if (event.getNewAboutToBeSetTarget() instanceof TJChaseCharacter tjc && tjc.isWeak()) {
            if (event.getEntity() instanceof Mob mob && mob.getTarget() == event.getNewAboutToBeSetTarget()) {
                event.setNewAboutToBeSetTarget(null);
            } else {
                event.setCanceled(true);
            }
        }
        if (event.getEntity() instanceof Mob mob && DancingHelper.isTargetingAffectedByZuri(mob) && event.getNewAboutToBeSetTarget() != null) {
            LivingEntity oldTarget = mob.getTarget();
            Zuri zuri = DancingHelper.getZuriDancingWithDirectly(mob);
            boolean cannotAttack = !zuri.canActivelyAttack(event.getNewAboutToBeSetTarget());
            boolean ally = zuri.isAlliedTo(event.getNewAboutToBeSetTarget());
            if (ally || cannotAttack && event.getNewAboutToBeSetTarget() == oldTarget) {
                event.setNewAboutToBeSetTarget(null);
            } else if (cannotAttack) {
                event.setCanceled(true);
            }
        }
    }

    private static void handleHarpTester(LivingChangeTargetEvent event) {
        LivingEntity attacker = event.getEntity();
        @Nullable
        LivingEntity target = event.getNewAboutToBeSetTarget();
        Optional<EntityReference<HarpTester>> ref1 = attacker.getData(ModAttachments.HARP_TESTER);
        Optional<EntityReference<HarpTester>> ref2 = target == null ? Optional.empty() : target.getData(ModAttachments.HARP_TESTER);
        Optional<EntityReference<HarpTester>> ref3 = Optional.empty();
        if (target instanceof OwnableEntity ownable && ownable.getOwner() != null) {
            ref3 = ownable.getOwner().getData(ModAttachments.HARP_TESTER);
        }
        if (ref1.isEmpty() && ref2.isEmpty() && ref3.isEmpty()) {
            return;
        }
        HarpTester harpTester1 = ref1.map(ref -> EntityReference.get(ref, attacker.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        HarpTester harpTester2 = ref2.map(ref -> EntityReference.get(ref, target.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        HarpTester harpTester3 = ref3.map(ref -> EntityReference.get(ref, target.level(), HarpTester.class)).filter(Entity::isAlive).orElse(null);
        if (ref1.isPresent() && (harpTester1 == harpTester2 || harpTester1 == harpTester3)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void checkCanAffect(MobEffectEvent.Applicable event) {
        handleAngelTomPassive2(event);
        handleTJChaseCharacterSuperArmor(event);
    }

    private static void handleAngelTomPassive2(MobEffectEvent.Applicable event) {
        if (AngelTomPassive2Owner.hasBuff(event.getEntity()) && event.getEffectInstance().getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    private static void handleTJChaseCharacterSuperArmor(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof TJChaseCharacter tj && tj.hasSuperArmor() && event.getEffectInstance().getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
