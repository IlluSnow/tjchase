package illusnow.tjchase.item;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.HarpConstants;
import illusnow.tjchase.util.OrbitingBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class HarpItem extends Item {
    private static final int INFINITY = 10000000;

    public HarpItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return super.use(level, player, hand);
        }
        int durability = itemInHand.getMaxDamage() - itemInHand.getDamageValue();
        int pickCount = HarpConstants.BASE_ORBITING_BLOCK_COUNT;
        if (player.getData(ModAttachments.ORBITING_BLOCKS).isEmpty() && (durability <= pickCount || pickAvailableBlocks(pickCount, player.getRandom(), player.getInventory()).isEmpty())) {
            return super.use(level, player, hand);
        } else if (player.getData(ModAttachments.ORBITING_BLOCKS).isEmpty()) {
            player.startUsingItem(hand);
        } else {
            if (level instanceof ServerLevel serverlevel) {
                OrbitingBlock block = player.getData(ModAttachments.ORBITING_BLOCKS).removeLookingBlock(player.yHeadRot);
                OrbitingBlockEntity projectile = new OrbitingBlockEntity(player, block);
                Projectile.spawnProjectile(projectile, serverlevel, itemInHand,
                        entity -> entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, HarpConstants.ORBITING_BLOCK_SHOOT_VELOCITY, HarpConstants.ORBITING_BLOCK_SHOOT_INACCURACY));
                player.syncData(ModAttachments.ORBITING_BLOCKS);
            }
            level.playSound(player, player.blockPosition(), ModSoundEvents.HARP_THROW_BLOCK.get(), SoundSource.PLAYERS);
            itemInHand.hurtAndBreak(1, player, hand);
            player.awardStat(Stats.ITEM_USED.get(this));

            if (player.getData(ModAttachments.ORBITING_BLOCKS).isEmpty()) {
                addHarpCooldown(player, HarpConstants.BASE_COOLDOWN);
            } else {
                addHarpCooldown(player, HarpConstants.BASE_PLAY_COOLDOWN);
            }
            player.swing(InteractionHand.OFF_HAND);
        }
        return InteractionResult.CONSUME;
    }

    private static List<BlockItemToBeUsed> pickAvailableBlocks(int maxCount, RandomSource random, Inventory inventory) {
        List<BlockItemToBeUsed> blocksMayBeUsed = findBlocksMayBeUsed(inventory);
        int totalCount = blocksMayBeUsed.stream().mapToInt(BlockItemToBeUsed::useCount).sum();
        if (totalCount <= maxCount) {
            return blocksMayBeUsed;
        }

        int[] oldCountArray = new int[Inventory.getSelectionSize()];
        for (BlockItemToBeUsed item : blocksMayBeUsed) {
            oldCountArray[item.index()] = item.useCount();
        }

        List<BlockItemToBeUsed> blocksRemained = new ArrayList<>(blocksMayBeUsed);
        int[] countArray = new int[Inventory.getSelectionSize()];
        int remainingCount = maxCount;
        while (remainingCount > 0) {
            if (blocksRemained.isEmpty()) {
                throw new AssertionError();
            }
            int i = random.nextInt(blocksRemained.size());
            BlockItemToBeUsed blockMayBeUsed = blocksRemained.get(i);
            countArray[blockMayBeUsed.index()]++;
            oldCountArray[blockMayBeUsed.index()]--;
            if (oldCountArray[blockMayBeUsed.index()] == 0) {
                blocksRemained.remove(i);
            }
            remainingCount--;
        }

        List<BlockItemToBeUsed> blocksToBeUsed = new ArrayList<>();
        for (BlockItemToBeUsed item : blocksMayBeUsed) {
            blocksToBeUsed.add(new BlockItemToBeUsed(item.index(), item.stack(), item.blockState(), countArray[item.index()]));
        }
        return blocksToBeUsed;
    }

    private static List<BlockItemToBeUsed> findBlocksMayBeUsed(Inventory inventory) {
        var nonEquipmentItems = inventory.getNonEquipmentItems();
        var hotbarItems = nonEquipmentItems.subList(0, Inventory.getSelectionSize());
        boolean instabuild = inventory.player.getAbilities().instabuild;
        List<BlockItemToBeUsed> blocksMayBeUsed = new ArrayList<>();
        for (int i = 0; i < hotbarItems.size(); i++) {
            ItemStack stack = hotbarItems.get(i);
            if (stack.getItem() instanceof BlockItem) {
                int useCount = instabuild ? INFINITY : stack.getCount();
                BlockItemToBeUsed blockItemToBeUsed = new BlockItemToBeUsed(i, stack, ((BlockItem) stack.getItem()).getBlock().defaultBlockState(), useCount);
                blocksMayBeUsed.add(blockItemToBeUsed);
            }
        }
        return blocksMayBeUsed;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        int currentUseDuration = stack.getUseDuration(livingEntity) - remainingUseDuration;
        int playDuration = getPlayDuration(stack, livingEntity);
        if (currentUseDuration == Mth.floor(playDuration * HarpConstants.PLAY_SOUND_PROGRESS)) {
            level.playSound(livingEntity, livingEntity.blockPosition(), ModSoundEvents.HARP_ATTRACT_BLOCKS.get(), livingEntity.getSoundSource());
        }
        if (livingEntity instanceof Player player && currentUseDuration == playDuration) {
            if (!player.level().isClientSide()) {
                List<BlockItemToBeUsed> blocksToBeUsed = pickAvailableBlocks(HarpConstants.BASE_ORBITING_BLOCK_COUNT, player.getRandom(), player.getInventory());
                List<BlockState> orbitingBlocks = new ArrayList<>();
                for (BlockItemToBeUsed item : blocksToBeUsed) {
                    for (int i = 0; i < item.useCount(); i++) {
                        orbitingBlocks.add(item.blockState());
                    }
                }
                livingEntity.getData(ModAttachments.ORBITING_BLOCKS).reset(
                        orbitingBlocks,
                        livingEntity.getBbWidth() * HarpConstants.DEFAULT_ORBIT_RADIUS,
                        livingEntity.yHeadRot
                );
                livingEntity.syncData(ModAttachments.ORBITING_BLOCKS);
                if (!player.getAbilities().instabuild) {
                    for (BlockItemToBeUsed item : blocksToBeUsed) {
                        item.stack().shrink(item.useCount());
                    }
                    stack.hurtAndBreak(blocksToBeUsed.stream().mapToInt(BlockItemToBeUsed::useCount).sum(), player, player.getUsedItemHand());
                }
                player.awardStat(Stats.ITEM_USED.get(this));
                addHarpCooldown(player, HarpConstants.BASE_PLAY_COOLDOWN);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ModItemUseAnimations.HARP_PLAY.getValue();
    }

    public static int getPlayDuration(ItemStack stack, LivingEntity user) {
        return HarpConstants.BASE_PLAY_DURATION;
    }

    public static void addHarpCooldown(Player player, int cooldown) {
        BuiltInRegistries.ITEM.getTagOrEmpty(ModItemTags.HARPS).forEach(item -> player.getCooldowns().addCooldown(new ItemStack(item), cooldown));
    }

    public record BlockItemToBeUsed(int index, ItemStack stack, BlockState blockState, int useCount) {}
}
