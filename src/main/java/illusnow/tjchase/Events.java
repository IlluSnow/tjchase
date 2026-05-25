package illusnow.tjchase;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.util.OrbitingBlock;
import illusnow.tjchase.util.OrbitingBlockHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public class Events {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().getBlockState(event.getEntity().getOnPos()).is(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES)) {
            event.setDamageMultiplier(0);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Does not need to sync here because the event is fired on both sides
        event.getEntity().getData(ModAttachments.ORBITING_BLOCKS.get()).update();
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
            event.setAmount(Math.max(event.getAmount() - HarpConstants.ORBITING_BLOCK_DAMAGE_REDUCTION, event.getAmount() * HarpConstants.ORBITING_BLOCK_MIN_DAMAGE_TAKEN));
            ServerLevel level = (ServerLevel) entity.level();
            Vec3 particlePos = block.calculateWorldPos(entity, block.getYRot()).add(0, -HarpConstants.ORBITING_BLOCK_HEIGHT_MUL / 2, 0);
            OrbitingBlockEntity.addBreakEffects(level,
                    block.getBlockState(),
                    BlockPos.containing(particlePos),
                    null,
                    5 + entity.getRandom().nextDouble() * 2,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z);
        }
    }
}
