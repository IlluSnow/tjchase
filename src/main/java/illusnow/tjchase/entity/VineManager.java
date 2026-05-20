package illusnow.tjchase.entity;

import com.google.common.collect.ImmutableSortedMap;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.mixin.EntityAccessor;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.tag.ModBlockTags;
import illusnow.tjchase.util.VineGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Predicate;

public class VineManager extends DataEntity {
    public static final int VINE_CLIMBING_SPEED_MULTIPLIER = 10;
    public static final int VINE_CLIMBING_GRAVITY_MULTIPLIER = 3;
    public static final double MAX_CLIMBING_SPEED = 0.9;
    public static final Predicate<Entity> AFFECTED_BY_VINES = entity -> entity instanceof LivingEntity && entity.isAlive() && !(entity instanceof Enemy);
    private static final int MAX_LIFETIME = 20 * 20;
    private static final int POSITIVE_EFFECTS_COOLDOWN = 20 * 10;
    private static final int HEALING_AMOUNT = 6;
    private static final int EFFECTS_DURATION = 40;
    private static final int REGENERATION_AMPLIFIER = 2;
    private static final int MOVEMENT_SLOWDOWN_AMPLIFIER = 1;
    private static final int JUMP_AMPLIFIER = 4;
    private static final String BOTTOM_CENTER_TAG = "BottomCenter";
    private static final String DIRECTION_TAG = "Direction";
    private static final String VINE_BLOCKS_TAG = "VineBlocks";
    private static final String VINE_BLOCKS_HEIGHT_TAG = "Height";
    private static final String VINE_BLOCKS_BLOCKS_TAG = "Blocks";
    private static final String VINE_BLOCKS_BLOCK_TAG = "Block";
    private static final String LIFETIME_TAG = "Lifetime";
    private BlockPos bottomCenter = BlockPos.ZERO;
    private Direction direction = Direction.NORTH;
    private SortedMap<Integer, Set<BlockPos>> vineBlocks = new TreeMap<>();
    private Set<BlockPos> actualTopBlocks = new HashSet<>();
    private int maxY;
    private int minY;
    private int lifetime;
    private int height;

    public VineManager(EntityType<?> type, Level level) {
        super(type, level);
    }

    public VineManager(EntityType<?> type, Level level, VineGenerator generator) {
        this(type, level, generator.getBottomCenter(), generator.getDirection(), generator.getVineBlocks());
    }

    public VineManager(EntityType<?> type, Level level, BlockPos bottomCenter, Direction direction, Map<Integer, Set<BlockPos>> vineBlocks) {
        super(type, level);
        this.bottomCenter = bottomCenter;
        this.direction = direction;
        this.vineBlocks = ImmutableSortedMap.copyOf(vineBlocks);
        if (direction == Direction.UP || direction == Direction.DOWN) {
            throw new IllegalArgumentException("Direction cannot be UP or DOWN");
        }
        loadData(bottomCenter, direction, vineBlocks);
    }

    private void loadData(BlockPos bottomCenter, Direction direction, Map<Integer, Set<BlockPos>> vineBlocks) {
        this.actualTopBlocks = extractTopBlocks(bottomCenter, direction, vineBlocks);
        this.maxY = extractMaxHeight(vineBlocks);
        this.minY = extractMinHeight(vineBlocks);
        this.height = maxY - minY + 1;
    }

    private static Integer extractMinHeight(Map<Integer, Set<BlockPos>> vineBlocks) {
        return vineBlocks.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getKey)).map(Map.Entry::getKey).orElseThrow();
    }

    private static Integer extractMaxHeight(Map<Integer, Set<BlockPos>> vineBlocks) {
        return vineBlocks.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getKey)).map(Map.Entry::getKey).orElseThrow();
    }

    private static Set<BlockPos> extractTopBlocks(BlockPos bottomCenter, Direction direction, Map<Integer, Set<BlockPos>> vineBlocks) {
        return vineBlocks.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .map(list -> list.stream()
                        .filter(pos -> !invalidTopPos(bottomCenter, direction, pos))
                        .toList())
                .map(Set::copyOf)
                .orElse(Set.of());
    }

    private static boolean invalidTopPos(BlockPos bottomCenter, Direction direction, BlockPos pos) {
        return pos.getX() == bottomCenter.relative(direction).getX() && pos.getZ() == bottomCenter.relative(direction).getZ();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                int cleanTicks = lifetime - MAX_LIFETIME;
                if (cleanTicks == 0) {
                    level().playSound(null, bottomCenter, ModSoundEvents.VINE_VANISH.get(), SoundSource.AMBIENT, VineSeed.GROW_SOUND_VOLUME, 1.0F);
                }
                var iterator = vineBlocks.reversed().entrySet().iterator();
                for (int i = 0; i < cleanTicks; i++) {
                    iterator.next();
                }
                if (iterator.hasNext()) {
                    var toRemove = iterator.next();
                    toRemove.getValue().stream().filter(pos -> level().getBlockState(pos).is(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES)).forEach(pos -> level().removeBlock(pos, false));
                } else if (cleanTicks < height) {
                    throw new IllegalStateException("Clean Error: cleanTicks = %s, lifetime = %s, height = %s".formatted(cleanTicks, lifetime, height));
                } else {
                    remove(RemovalReason.DISCARDED);
                }
            }
            if (!actualTopBlocks.isEmpty()) {
                int minX = actualTopBlocks.stream().mapToInt(Vec3i::getX).min().orElseThrow();
                int maxX = actualTopBlocks.stream().mapToInt(Vec3i::getX).max().orElseThrow();
                int minZ = actualTopBlocks.stream().mapToInt(Vec3i::getZ).min().orElseThrow();
                int maxZ = actualTopBlocks.stream().mapToInt(Vec3i::getZ).max().orElseThrow();
                int minY = this.maxY - 1;
                int maxY = this.maxY + 3;
                AABB checkingBoundingBox = new AABB(minX, minY, minZ, maxX, maxY, maxZ).inflate(2);
                for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, checkingBoundingBox, EntitySelector.NO_SPECTATORS.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE))) {
                    if (((EntityAccessor) entity).callIsAffectedByBlocks() && entity.onGround()) {
                        BlockPos onPos = entity.getOnPos();
                        BlockState onState = level().getBlockState(onPos);
                        if (actualTopBlocks.contains(onPos) && onState.is(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES)) {
                            if (AFFECTED_BY_VINES.test(entity) && entity.getData(ModAttachments.VINE_CD) < level().getGameTime()) {
                                entity.heal(HEALING_AMOUNT);
                                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECTS_DURATION, REGENERATION_AMPLIFIER, false, false));
                                entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, EFFECTS_DURATION, JUMP_AMPLIFIER, false, false));
                                // Passed null to prevent the player being healed from not hearing the sound
                                level().playSound(null, entity.blockPosition(), ModSoundEvents.VINE_HEAL.get(), entity.getSoundSource(), 1.0F, 1.0F);
                                entity.setData(ModAttachments.VINE_CD, level().getGameTime() + POSITIVE_EFFECTS_COOLDOWN);
                                // cd: 10s
                            } else if (entity instanceof Enemy) {
                                entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EFFECTS_DURATION, MOVEMENT_SLOWDOWN_AMPLIFIER, false, false));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putLong(BOTTOM_CENTER_TAG, bottomCenter.asLong());
        output.putInt(DIRECTION_TAG, direction.get2DDataValue());

        ValueOutput.ValueOutputList allBlocks = output.childrenList(VINE_BLOCKS_TAG);
        for (var entry : vineBlocks.entrySet()) {
            ValueOutput levelBlocks = allBlocks.addChild();
            levelBlocks.putInt(VINE_BLOCKS_HEIGHT_TAG, entry.getKey());
            ValueOutput.ValueOutputList blocks = levelBlocks.childrenList(VINE_BLOCKS_BLOCKS_TAG);
            for (BlockPos pos : entry.getValue()) {
                blocks.addChild().putLong(VINE_BLOCKS_BLOCK_TAG, pos.asLong());
            }
        }

        output.putInt(LIFETIME_TAG, lifetime);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.bottomCenter = input.getLong(BOTTOM_CENTER_TAG).map(BlockPos::of).orElse(BlockPos.ZERO);
        this.direction = input.getInt(DIRECTION_TAG).map(Direction::from2DDataValue).orElse(Direction.NORTH);

        input.childrenListOrEmpty(VINE_BLOCKS_TAG).forEach(levelBlocks -> {
            int height = levelBlocks.getIntOr(VINE_BLOCKS_HEIGHT_TAG, 0);
            ValueInput.ValueInputList blocksStorage = levelBlocks.childrenListOrEmpty(VINE_BLOCKS_BLOCKS_TAG);
            Set<BlockPos> blocks = new HashSet<>();
            for (var posStorage : blocksStorage) {
                posStorage.getLong(VINE_BLOCKS_BLOCK_TAG).map(BlockPos::of).ifPresent(blocks::add);
            }
            vineBlocks.put(height, blocks);
        });

        loadData(bottomCenter, direction, vineBlocks);
        this.lifetime = input.getIntOr(LIFETIME_TAG, 0);
    }
}
