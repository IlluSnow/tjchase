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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

public class ChainHarvesting {
    private final AdjacencyType adjacencyType;
    private final Level level;
    private final BlockPos start;
    private final int maxHarvestCount;
    private final Predicate<? super BlockState> blockPredicate;
    private final Set<BlockPos> result = new HashSet<>();

    public ChainHarvesting(AdjacencyType adjacencyType, Level level, BlockPos start, Predicate<? super BlockState> blockPredicate) {
        this(adjacencyType, level, start, 200, blockPredicate);
    }

    public ChainHarvesting(AdjacencyType adjacencyType, Level level, BlockPos start, int maxHarvestCount, Predicate<? super BlockState> blockPredicate) {
        this.adjacencyType = adjacencyType;
        this.level = level;
        this.start = start.immutable();
        this.maxHarvestCount = maxHarvestCount;
        this.blockPredicate = blockPredicate;
    }

    public List<BlockPos> getResult(Comparator<BlockPos> comparator) {
        if (maxHarvestCount <= 0) {
            return List.of();
        }
        if (result.isEmpty()) {
            start();
        }
        return result.stream().sorted(comparator).toList();
    }

    private void start() {
        result.add(start);
        if (maxHarvestCount == 1) {
            return;
        }
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && result.size() < maxHarvestCount) {
            BlockPos current = queue.poll();
            for (BlockPos neighbor : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                neighbor = neighbor.immutable();
                if (result.contains(neighbor)) {
                    continue;
                }
                Vec3i delta = neighbor.subtract(current);
                if (Math.abs(delta.getX()) + Math.abs(delta.getY()) + Math.abs(delta.getZ()) <= adjacencyType.getMaxDistManhattan() && blockPredicate.test(level.getBlockState(neighbor))) {
                    result.add(neighbor);
                    if (result.size() >= maxHarvestCount) {
                        return;
                    }
                    queue.add(neighbor);
                }
            }
        }
    }

    public enum AdjacencyType {
        PLANE(1),
        EDGE(2),
        VERTEX(3);

        private final int maxDistManhattan;

        AdjacencyType(int maxDistManhattan) {
            this.maxDistManhattan = maxDistManhattan;
        }

        public int getMaxDistManhattan() {
            return maxDistManhattan;
        }
    }
}
