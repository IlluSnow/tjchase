package illusnow.tjchase.util;

import illusnow.tjchase.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;

public class VineGenerator {
    private static final int DOOR_HEIGHT = 3;
    private static final double CYCLE_COUNT_10 = 1;
    private static final double THICKNESS_MULTIPLIER = 0.9;
    private final BlockPos bottomCenter;
    private final Direction direction;
    private final WeightedBlockSelector decorativeSelector;
    private final WeightedBlockSelector platformSelector;
    private final Map<Integer, Set<BlockPos>> vineBlocks = new HashMap<>();

    public VineGenerator(BlockPos bottomCenter, Direction direction, WeightedBlockSelector selector, WeightedBlockSelector platformSelector) {
        this.bottomCenter = bottomCenter;
        this.direction = direction;
        this.decorativeSelector = selector;
        this.platformSelector = platformSelector;
    }

    public void placeVineOfHeight(Level level, int height, int maxHeight, double radius) {
        double radiusOfHeight = radius * calculateRadiusMultiplier(height, maxHeight);

        double angle1 = Math.PI * 2 * CYCLE_COUNT_10 * (maxHeight / 10.0) * ((double) height / maxHeight);
        double angle2 = angle1 + Math.PI;

        double xOffset1 = Math.cos(angle1) * radiusOfHeight;
        double zOffset1 = Math.sin(angle1) * radiusOfHeight;
        double xOffset2 = Math.cos(angle2) * radiusOfHeight;
        double zOffset2 = Math.sin(angle2) * radiusOfHeight;

        double x1 = bottomCenter.getX() + 0.5 + xOffset1;
        double z1 = bottomCenter.getZ() + 0.5 + zOffset1;
        double x2 = bottomCenter.getX() + 0.5 + xOffset2;
        double z2 = bottomCenter.getZ() + 0.5 + zOffset2;

        double exactThickness = radiusOfHeight * THICKNESS_MULTIPLIER;
        int currentY = bottomCenter.getY() + height;

        drawSolidCircle(level, x1, currentY, z1, exactThickness, decorativeSelector);
        drawSolidCircle(level, x2, currentY, z2, exactThickness, decorativeSelector);

        BlockPos centerPos = new BlockPos(bottomCenter.getX(), currentY, bottomCenter.getZ());
        placeBlock(level, centerPos, platformSelector.getRandomMaterial(level.getRandom()));
        placeBlock(level, centerPos.relative(direction),
                ModBlocks.TEMPORARY_VINE.get().defaultBlockState().setValue(LadderBlock.FACING, direction));
    }

    private static double calculateRadiusMultiplier(double height, double maxHeight) {
        double ratio = height / maxHeight;
        return 2 * (ratio - 0.7) * (ratio - 0.7) + 1;
    }

    public void placeTop(Level level, int maxHeight, double radius, Direction direction) {
        int topHeight = 2;
        for (int i = 0; i < topHeight; i++) {
            int y = bottomCenter.getY() + maxHeight - topHeight + i;
            BlockPos centerPos = new BlockPos(bottomCenter.getX(), y, bottomCenter.getZ());
            BlockPos ladderPos = centerPos.relative(direction);
            double topRadius = radius * calculateRadiusMultiplier(1, 1) * (2 + i);

            for (int x = (int) Math.round(bottomCenter.getX() - topRadius); x <= bottomCenter.getX() + topRadius; x++) {
                for (int z = (int) Math.round(bottomCenter.getZ() - topRadius); z <= bottomCenter.getZ() + topRadius; z++) {
                    double distanceSqr = Math.pow(x - bottomCenter.getX(), 2) + Math.pow(z - bottomCenter.getZ(), 2);
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!pos.equals(centerPos) && !pos.equals(ladderPos) && distanceSqr <= topRadius * topRadius) {
                        placeBlock(level, pos, platformSelector.getRandomMaterial(level.getRandom()));
                    }
                }
            }
        }
    }

    private void drawSolidCircle(Level level, double cx, int cy, double cz, double radius, WeightedBlockSelector selector) {
        if (radius <= 0) {
            return;
        }

        int rCeil = (int) Math.ceil(radius);
        int startX = (int) Math.floor(cx) - rCeil;
        int endX = (int) Math.floor(cx) + rCeil;
        int startZ = (int) Math.floor(cz) - rCeil;
        int endZ = (int) Math.floor(cz) + rCeil;

        double radiusSqr = radius * radius;
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                double dx = (x + 0.5) - cx;
                double dz = (z + 0.5) - cz;

                BlockPos pos = new BlockPos(x, cy, z);
                BlockPos delta = pos.subtract(bottomCenter);
                int dot = delta.getX() * direction.getStepX() + delta.getZ() * direction.getStepZ();
                if (dx * dx + dz * dz <= radiusSqr && !(cy - bottomCenter.getY() <= DOOR_HEIGHT && delta.getX() * delta.getZ() == 0 && dot > 0)) {
                    placeBlock(level, pos, selector.getRandomMaterial(level.getRandom()));
                }
            }
        }
    }

    private void placeBlock(Level level, BlockPos pos, BlockState state) {
        BlockState currentState = level.getBlockState(pos);
        boolean replaceable = currentState.isAir() || currentState.canBeReplaced();
        if (replaceable || vineBlocks.getOrDefault(pos.getY(), Set.of()).contains(pos)) {
            if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
                state = state.setValue(BlockStateProperties.PERSISTENT, true);
            }
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        vineBlocks.putIfAbsent(pos.getY(), new HashSet<>());
        vineBlocks.get(pos.getY()).add(pos);
    }

    public BlockPos getBottomCenter() {
        return bottomCenter;
    }

    public Direction getDirection() {
        return direction;
    }

    public Map<Integer, Set<BlockPos>> getVineBlocks() {
        return Collections.unmodifiableMap(vineBlocks);
    }
}