package illusnow.tjchase.item;

import com.mojang.datafixers.util.Pair;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.projectile.OrbitingBlockEntity;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.particle.ModParticleTypes;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.tag.ModItemTags;
import illusnow.tjchase.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HarpItem extends Item {
    private static final int INFINITY = 10000000;
    private final int baseMaxOrbitingBlockCount;
    private final float baseDamageBonus;

    public HarpItem(Properties properties, int baseMaxOrbitingBlockCount, float baseDamageBonus) {
        super(properties);
        this.baseMaxOrbitingBlockCount = baseMaxOrbitingBlockCount;
        this.baseDamageBonus = baseDamageBonus;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
            return super.use(level, player, hand);
        }

        int durability = itemInHand.getMaxDamage() - itemInHand.getDamageValue();
        int orbitingBlockCount = getOrbitingBlockCount(player.getRandom(), itemInHand);
        OrbitingBlockHolder holder = player.getData(ModAttachments.ORBITING_BLOCKS);
        if (player.isShiftKeyDown() && orbitingBlockCount > 0) {
            holder.getOrbitingBlocks().stream()
                    .filter(block -> !block.isAutoCreated())
                    .forEach(block -> player.getInventory().placeItemBackInInventory(new ItemStack(block.getBlockState().getBlock().asItem())));
            holder.clear();
            player.syncData(ModAttachments.ORBITING_BLOCKS);
            addHarpCooldown(player, HarpConstants.BASE_COOLDOWN - getUseCooldownTicksDecrease(player.getRandom(), itemInHand));
            player.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.CONSUME;
        }

        if (holder.isEmpty() &&
                (!player.getAbilities().instabuild && durability <= orbitingBlockCount || pickAvailableBlocks(itemInHand, (orbitingBlockCount), player.getRandom(), player.getInventory()).isEmpty())) {
            return super.use(level, player, hand);
        } else if (holder.isEmpty()) {
            player.startUsingItem(hand);
        } else {
            if (level instanceof ServerLevel serverlevel) {
                OrbitingBlock block = holder.removeLookingBlock(player.yHeadRot);
                OrbitingBlockEntity projectile = new OrbitingBlockEntity(player, block);

                float damage = calculateBaseDamage(block.getBlockState()) + baseDamageBonus;
                double seekingRange = getSeekingRange(serverlevel, itemInHand);
                double seekPower = getSeekPower(serverlevel, itemInHand);
                float aoeDamage = getAOEDamage(serverlevel, itemInHand);
                double aoeRadius = getAOERadius(serverlevel, itemInHand);
                OrbitingBlockEntity.Properties properties = new OrbitingBlockEntity.Properties(damage,
                        HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE,
                        EnchantmentHelper.has(itemInHand, ModEnchantmentEffectComponents.NO_GRAVITY.get()),
                        seekingRange,
                        seekPower,
                        aoeDamage,
                        aoeRadius,
                        OrbitingBlockEntity.BlockType.getTypeFor(block.getBlockState()),
                        itemInHand.copy());
                projectile.setProperties(properties);

                float velocity = getShootVelocity(serverlevel, itemInHand) * (projectile.canSeek() ? (float) HarpConstants.SEEK_VELOCITY_MULTIPLIER : 1);
                Projectile.spawnProjectile(projectile, serverlevel, itemInHand,
                        entity -> entity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, velocity, HarpConstants.ORBITING_BLOCK_SHOOT_INACCURACY));
                player.syncData(ModAttachments.ORBITING_BLOCKS);
                level.playSound(null, player.blockPosition(), getPlayingSound(itemInHand).value(), SoundSource.PLAYERS);
                itemInHand.hurtAndBreak(1, player, hand);
                player.awardStat(Stats.ITEM_USED.get(this));
            }

            if (holder.isEmpty()) {
                addHarpCooldown(player, HarpConstants.BASE_COOLDOWN - getUseCooldownTicksDecrease(player.getRandom(), itemInHand));
            } else {
                addHarpCooldown(player, HarpConstants.BASE_PLAY_COOLDOWN - getPlayCooldownTicksDecrease(player.getRandom(), itemInHand));
            }
            player.swing(InteractionHand.OFF_HAND);
        }
        return InteractionResult.CONSUME;
    }

    private int getOrbitingBlockCount(RandomSource random, ItemStack stack) {
        return Utils.getFixedEnchantmentIntValue(random, stack, baseMaxOrbitingBlockCount, ModEnchantmentEffectComponents.ADDITIONAL_ORBITING_BLOCK_COUNT.get());
    }

    public static int getUseCooldownTicksDecrease(RandomSource random, ItemStack stack) {
        return Utils.getFixedEnchantmentIntValue(random, stack, 0, ModEnchantmentEffectComponents.USE_COOLDOWN_TICKS_DECREASE.get());
    }

    private static int getPlayCooldownTicksDecrease(RandomSource random, ItemStack stack) {
        return Utils.getFixedEnchantmentIntValue(random, stack, 0, ModEnchantmentEffectComponents.PLAY_COOLDOWN_TICKS_DECREASE.get());
    }

    private static int getPlayTicksDecrease(RandomSource random, ItemStack stack) {
        return Utils.getFixedEnchantmentIntValue(random, stack, 0, ModEnchantmentEffectComponents.PLAY_TICKS_DECREASE.get());
    }

    private static float getShootVelocity(ServerLevel level, ItemStack stack) {
        return Utils.getItemEnchantmentValue(level, stack, HarpConstants.ORBITING_BLOCK_SHOOT_VELOCITY, ModEnchantmentEffectComponents.SHOOT_VELOCITY.get());
    }

    private static float getSeekingRange(ServerLevel level, ItemStack stack) {
        return Utils.getItemEnchantmentValue(level, stack, 0, ModEnchantmentEffectComponents.SEEKING_RANGE.get());
    }

    private static float getSeekPower(ServerLevel level, ItemStack stack) {
        return Utils.getItemEnchantmentValue(level, stack, 0, ModEnchantmentEffectComponents.SEEK_POWER.get());
    }

    private static float getAOEDamage(ServerLevel level, ItemStack stack) {
        return Utils.getItemEnchantmentValue(level, stack, 0, ModEnchantmentEffectComponents.AOE_DAMAGE.get());
    }

    private static double getAOERadius(ServerLevel level, ItemStack stack) {
        return Utils.getItemEnchantmentValue(level, stack, 0, ModEnchantmentEffectComponents.AOE_RADIUS.get());
    }

    // The wrapped method is actually nullable
    @SuppressWarnings("OptionalOfNullableMisuse")
    private static Optional<WeightedBlockSelector> getBlockSelector(ItemStack stack) {
        return Optional.ofNullable(EnchantmentHelper.getHighestLevel(stack, ModEnchantmentEffectComponents.ANGEL_TOM_WEAPON2_BLOCK_SELECTOR.get())).map(Pair::getFirst);
    }

    private static float calculateBaseDamage(BlockState state) {
        double hardness = state.getBlock().defaultDestroyTime();
        if (hardness < 0) {
            throw new IllegalArgumentException("Cannot calculate damage for unbreakable block");
        }
        if (Math.abs(hardness) < 1E-5) {
            return HarpConstants.BASE_ORBITING_BLOCK_DAMAGE;
        }
        return HarpConstants.calculateBaseDamageFromHardness(hardness) + HarpConstants.BASE_ORBITING_BLOCK_DAMAGE_CONSTANT;
    }

    private static List<BlockItemToBeUsed> pickAvailableBlocks(ItemStack stack, int maxCount, RandomSource random, Inventory inventory) {
        List<BlockItemToBeUsed> blocksMayBeUsed = findBlocksMayBeUsed(inventory);
        int totalCount = blocksMayBeUsed.stream().mapToInt(BlockItemToBeUsed::useCount).sum();
        if (totalCount <= maxCount) {
            int delta = maxCount - totalCount;
            getBlockSelector(stack).ifPresent(selector -> {
                for (int i = 0; i < delta; i++) {
                    BlockState blockState = selector.getRandomMaterial(random);
                    BlockItemToBeUsed blockItemToBeUsed = new BlockItemToBeUsed(-1, ItemStack.EMPTY, blockState, 1);
                    blocksMayBeUsed.add(blockItemToBeUsed);
                }
            });
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
            if (canBeUsed(stack)) {
                int useCount = instabuild ? INFINITY : stack.getCount();
                BlockItemToBeUsed blockItemToBeUsed = new BlockItemToBeUsed(i, stack, ((BlockItem) stack.getItem()).getBlock().defaultBlockState(), useCount);
                blocksMayBeUsed.add(blockItemToBeUsed);
            }
        }
        return blocksMayBeUsed;
    }

    private static boolean canBeUsed(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        BlockState blockState = block.defaultBlockState();
        float hardness = blockState.getBlock().defaultDestroyTime();
        if (hardness < 0) {
            return false;
        }
        if (blockState.hasBlockEntity()) {
            return false;
        }
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return false;
        }
        if (blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) || blockItem instanceof DoubleHighBlockItem) {
            return false;
        }
        return !(blockState.getBlock() instanceof PistonHeadBlock);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide()) {
            return;
        }
        RandomSource random = livingEntity.getRandom();
        int currentUseDuration = stack.getUseDuration(livingEntity) - remainingUseDuration;
        int playDuration = getPlayDuration(stack, livingEntity);
        if (currentUseDuration == Mth.floor(playDuration * HarpConstants.PLAY_SOUND_PROGRESS)) {
            level.playSound(null, livingEntity.blockPosition(), getAttractingSound(stack).value(), livingEntity.getSoundSource());
        }
        if (currentUseDuration >= Mth.floor(playDuration * HarpConstants.SHOW_NOTE_PROGRESS) && currentUseDuration < playDuration) {
            Vec3 direction = new Vec3(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
            direction = direction.multiply(1, livingEntity.getBbHeight() / livingEntity.getBbWidth(), 1).normalize();
            ((ServerLevel) level).sendParticles(ModParticleTypes.HARP_PLAYED_NOTE.get(), livingEntity.getX(), livingEntity.getY(0.5), livingEntity.getZ(), 0, direction.x, direction.y, direction.z, 1);
        }
        if (livingEntity instanceof Player player && currentUseDuration == playDuration) {
            List<BlockItemToBeUsed> blocksToBeUsed = pickAvailableBlocks(stack, getOrbitingBlockCount(player.getRandom(), stack), player.getRandom(), player.getInventory());
            List<Pair<BlockState, Boolean>> orbitingBlocks = new ArrayList<>();
            for (BlockItemToBeUsed item : blocksToBeUsed) {
                for (int i = 0; i < item.useCount(); i++) {
                    orbitingBlocks.add(Pair.of(item.blockState(), player.getAbilities().instabuild || item.index() < 0));
                }
            }
            Collections.shuffle(orbitingBlocks);
            OrbitingBlockHolder data = player.getData(ModAttachments.ORBITING_BLOCKS);
            data.setOrbitRadius(player.getBbWidth() * HarpConstants.DEFAULT_ORBIT_RADIUS);
            data.reset(orbitingBlocks, player.yHeadRot);
            player.syncData(ModAttachments.ORBITING_BLOCKS);
            if (!player.getAbilities().instabuild) {
                for (BlockItemToBeUsed item : blocksToBeUsed) {
                    if (item.index() < 0) {
                        continue;
                    }
                    item.stack().shrink(item.useCount());
                }
                stack.hurtAndBreak(blocksToBeUsed.stream().mapToInt(BlockItemToBeUsed::useCount).sum(), player, player.getUsedItemHand());
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            addHarpCooldown(player,HarpConstants.BASE_PLAY_COOLDOWN - getPlayCooldownTicksDecrease(player.getRandom(), stack));
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
        return HarpConstants.BASE_PLAY_DURATION - getPlayTicksDecrease(user.getRandom(), stack);
    }

    public static Holder<SoundEvent> getAttractingSound(ItemStack stack) {
        return EnchantmentHelper.pickHighestLevel(stack, ModEnchantmentEffectComponents.HARP_ATTRACT_SOUND.get()).orElse(ModSoundEvents.HARP_ATTRACT_BLOCKS);
    }

    public static Holder<SoundEvent> getPlayingSound(ItemStack stack) {
        return EnchantmentHelper.pickHighestLevel(stack, ModEnchantmentEffectComponents.HARP_PLAY_SOUND.get()).orElse(ModSoundEvents.HARP_THROW_BLOCK);
    }

    public static void addHarpCooldown(Player player, int cooldown) {
        BuiltInRegistries.ITEM.getTagOrEmpty(ModItemTags.HARPS).forEach(item -> player.getCooldowns().addCooldown(new ItemStack(item), cooldown));
    }

    public record BlockItemToBeUsed(int index, ItemStack stack, BlockState blockState, int useCount) {}
}
