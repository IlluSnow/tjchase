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
import illusnow.tjchase.entity.HealthLockable;
import illusnow.tjchase.entity.Linia;
import illusnow.tjchase.entity.TJChaseCharacter;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.dataentity.BlueprintManager;
import illusnow.tjchase.entity.dataentity.HarpTester;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.entity.projectile.YogaBall;
import illusnow.tjchase.item.HarpItem;
import illusnow.tjchase.item.ModDataComponents;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.*;
import illusnow.tjchase.world.gameplay.WeakState;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import illusnow.tjchase.world.gameplay.action.ModActions;
import illusnow.tjchase.world.gameplay.action.TieAction;
import illusnow.tjchase.world.gameplay.struggle.StruggleInstance;
import illusnow.tjchase.world.gameplay.struggle.StruggleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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
        ActionHolder.updateBidirectionally(player);
        if (!player.level().isClientSide()) {
            Controllable.checkCanContinueToControl(player);
        }
        WeakState weak = player.getData(ModAttachments.WEAK_STATE);
        if (player.isAlive()) {
            if (!player.level().isClientSide() && weak.serverTick(player)) {
                player.syncData(ModAttachments.WEAK_STATE);
            }
        }
        StruggleInstance.updateBidirectionally(player);
    }

    @SubscribeEvent
    public static void onCalculatingEntitySize(EntityEvent.Size event) {
        if (event.getEntity() instanceof Player player && player.hasData(ModAttachments.WEAK_STATE) && player.getData(ModAttachments.WEAK_STATE).isWeak()) {
            event.setNewSize(WeakState.WEAK_DIMENSIONS);
        }
    }

    @SubscribeEvent
    public static void onClickingWeakPlayer(PlayerInteractEvent.EntityInteractSpecific event) {
        handleHugAndTie(event);
    }

    private static void handleHugAndTie(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getTarget() instanceof ServerPlayer player && player.getData(ModAttachments.WEAK_STATE).isWeak() && TyingHelper.getTiedTo(player) == null) {
            TyingHelper.tie(player, event.getEntity());
            ActionHolder.setAction(player, ModActions.STRUGGLE.get());
            ActionHolder.setAction(event.getEntity(), ModActions.HUG.get());
            StruggleInstance.setStruggle(player, StruggleInstance.createNew(StruggleTypes.PLAYER.get()));
            event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
        }

        Player tying = TyingHelper.getTying(event.getEntity());
        if (event.getTarget() instanceof Rocket rocket && !rocket.level().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND && tying != null) {
            if (ActionHolder.setAction(event.getEntity(), ModActions.TIE.get(), new TieAction.Data(EntityReference.of(rocket), Rocket.DEFAULT_TIE_DURATION))) {
                event.setCancellationResult(InteractionResult.SUCCESS_SERVER);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        untieIfDisconnected(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        tieAgainIfReconnected(event);
    }

    private static void untieIfDisconnected(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        Freezable.Situation currentSituation = Freezable.findCurrentSituation((ServerPlayer) player);
        Entity tiedTo = TyingHelper.getTiedTo(player);
        if (tiedTo != null) {
            if (tiedTo instanceof Rocket rocket && currentSituation.playerMaybeOfflineAfterLoad()) {
                rocket.freezeOnDisconnect((ServerPlayer) player);
            }
            if (tiedTo instanceof Player playerTyingSelf) {
                TyingHelper.tie(player, null);
                TyingHelper.clearStruggleOrPrayAction(player);
                TyingHelper.clearHugOrTieAction(playerTyingSelf);
            }
        }
        Player selfTying = TyingHelper.getTying(player);
        if (selfTying != null) {
            TyingHelper.tie(selfTying, null);
            TyingHelper.clearStruggleOrPrayAction(selfTying);
            TyingHelper.clearHugOrTieAction(player);
        }
    }

    private static void tieAgainIfReconnected(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        Entity tiedTo = TyingHelper.getTiedTo(player);
        if (tiedTo instanceof Rocket rocket) {
            rocket.unfreezeOnConnect((ServerPlayer) player);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity && !entity.level().isClientSide()) {
            AngelTomPassive2Owner.mayUpdate(livingEntity);
            if (getLockedHealthFromAngelTomWeapon3((ServerLevel) livingEntity.level(), livingEntity, livingEntity.getMainHandItem()) > 0) {
                Utils.sendTJChaseBuffParticles(livingEntity, 0.6F, 1, 1);
            }
            BlueprintManager.updateBlueprintData(livingEntity);
        }
        updateTie(entity);
    }

    private static void updateTie(Entity entity) {
        if (!entity.level().isClientSide() && (TyingHelper.canTiePlayer(entity) || entity instanceof Player)) {
            TyingHelper.clearIfInvalid(entity);
        }
        if (TyingHelper.canTiePlayer(entity)) {
            Player tying = TyingHelper.getTying(entity);
            if (tying != null && TyingHelper.isMovementRestricted(tying)) {
                if (entity instanceof Player controller) {
                    updateTieForPlayers(controller, tying);
                } else {
                    TyingHelper.restrictTiedPlayerMovement(tying, entity, false);
                }
            }
        }
    }

    private static void updateTieForPlayers(Player controller, Player controlling) {
        if (controlling.isLocalPlayer()) {
            TyingHelper.restrictTiedPlayerMovement(controlling, controller, false);
        } else {
            if (!controller.level().isClientSide()) {
                TyingHelper.restrictTiedPlayerMovement(controlling, controller, false);
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
        handlePassivePlayer(event);
    }

    private static void handlePassivePlayer(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && Utils.isPassive(player) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
        }
    }

    private static void handleBlueprintDamageReduction(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (BlueprintManager.isEntityInsideOwnedBlueprint(player)) {
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
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && player.getData(ModAttachments.WEAK_STATE).isWeak()) {
            event.setCanceled(true);
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
                if (entity instanceof HealthLockable healthLockable) {
                    healthLockable.tjChase$onHealthLockingHasEffect();
                }
                event.setNewDamage(0);
            } else {
                float delta = entity.getHealth() - minimum;
                float newDamage = event.getNewDamage();
                if (newDamage > delta) {
                    event.setNewDamage(delta);
                    if (entity instanceof HealthLockable healthLockable) {
                        healthLockable.tjChase$onHealthLockingHasEffect();
                    }
                }
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
        if (event.getEntity() instanceof Player) {
            event.setCanceled(true);
            return;
        }
        if (event.getSource().getEntity() instanceof TJChaseCharacter tjc) {
            tjc.awardKillProficiencyPoints(event.getEntity(), event.getEntity().getMaxHealth());
        }
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            Controllable.control(player, null);
            Entity tiedTo = TyingHelper.getTiedTo(player);
            TyingHelper.clearTying(player, tiedTo);
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
        if (entity instanceof HealthLockable healthLockable && healthLockable.tjChase$processDamageInEventListeners()) {
            return healthLockable.tjChase$getLockedHealth();
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
        handleWeak(event);
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

    private static void handleWeak(LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof TJChaseCharacter tjc && tjc.isWeak()) {
            easeTarget(event);
        }
        if (event.getNewAboutToBeSetTarget() instanceof Player player && Utils.isPassive(player)) {
            easeTarget(event);
        }
    }

    private static void easeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && mob.getTarget() == event.getNewAboutToBeSetTarget()) {
            event.setNewAboutToBeSetTarget(null);
        } else {
            event.setCanceled(true);
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

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        TooltipDisplay tooltipDisplay = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        stack.addToTooltip(ModDataComponents.CARRYING_TEMPLATE, event.getContext(), tooltipDisplay, event.getToolTip()::add, event.getFlags());
        if (!stack.has(ModDataComponents.CARRYING_TEMPLATE)) {
            stack.addToTooltip(ModDataComponents.PLACE_TEMPLATE, event.getContext(), tooltipDisplay, event.getToolTip()::add, event.getFlags());
        }
    }
}
