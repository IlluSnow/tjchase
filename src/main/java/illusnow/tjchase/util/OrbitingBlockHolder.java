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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrbitingBlockHolder {
    public static final StreamCodec<RegistryFriendlyByteBuf, OrbitingBlockHolder> STREAM_CODEC = StreamCodec.composite(
            OrbitingBlock.STREAM_CODEC.apply(ByteBufCodecs.list()), OrbitingBlockHolder::getOrbitingBlocks,
            ByteBufCodecs.FLOAT, OrbitingBlockHolder::getBlockSize,
            ByteBufCodecs.FLOAT, OrbitingBlockHolder::getRotatingSpeed,
            ByteBufCodecs.DOUBLE, OrbitingBlockHolder::getOrbitRadius,
            ByteBufCodecs.DOUBLE, OrbitingBlockHolder::getOrbitHeightMultiplier,
            OrbitingBlockHolder::new
    );
    private final List<OrbitingBlock> orbitingBlocks = new ArrayList<>();
    private float blockSize;
    private float rotatingSpeed;
    private double orbitRadius;
    private double orbitHeightMultiplier;

    public OrbitingBlockHolder(List<OrbitingBlock> orbitingBlocks, float blockSize, float rotatingSpeed, double orbitRadius, double orbitHeightMultiplier) {
        this.blockSize = blockSize;
        this.rotatingSpeed = rotatingSpeed;
        this.orbitRadius = orbitRadius;
        this.orbitHeightMultiplier = orbitHeightMultiplier;
        if (!orbitingBlocks.isEmpty()) {
            for (OrbitingBlock block : orbitingBlocks) {
                if (block.getOrbitRadius() != orbitRadius) {
                    throw new IllegalArgumentException("All orbiting blocks must have the same orbit radius");
                }
            }
            this.orbitingBlocks.addAll(orbitingBlocks);
        }
    }
    
    public static OrbitingBlockHolder defaultHolder(List<OrbitingBlock> orbitingBlocks) {
        return new OrbitingBlockHolder(orbitingBlocks, HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE, HarpConstants.DEFAULT_ORBITING_BLOCK_ROTATING_SPEED, HarpConstants.DEFAULT_ORBIT_RADIUS, HarpConstants.DEFAULT_ORBITING_BLOCK_HEIGHT_MUL);
    }

    public static OrbitingBlockHolder emptyHolder() {
        return defaultHolder(List.of());
    }

    public OrbitingBlock removeLookingBlock(float yHeadRot) {
        yHeadRot = Mth.wrapDegrees(yHeadRot);
        if (orbitingBlocks.isEmpty()) {
            throw new IllegalStateException("There are no orbiting blocks!");
        }
        int index = getLookingBlockIndex(yHeadRot);
        return remove(index);
    }

    @Nullable
    public OrbitingBlock getLookingBlock(LivingEntity entity) {
        if (orbitingBlocks.isEmpty()) {
            return null;
        }
        return orbitingBlocks.get(getLookingBlockIndex(entity.yHeadRot));
    }

    private int getLookingBlockIndex(float yHeadRot) {
        int index = 0;
        float min = Mth.abs(Mth.wrapDegrees(orbitingBlocks.getFirst().getYRot() - yHeadRot));
        for (int i = 1; i < orbitingBlocks.size(); i++) {
            if (Mth.abs(Mth.wrapDegrees(orbitingBlocks.get(i).getYRot() - yHeadRot)) < min) {
                min = Mth.abs(Mth.wrapDegrees(orbitingBlocks.get(i).getYRot() - yHeadRot));
                index = i;
            }
        }
        return index;
    }

    public OrbitingBlock remove(int index) {
        List<OrbitingBlock> blocks = new ArrayList<>(orbitingBlocks);
        if (index < 0 || index >= blocks.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + blocks.size());
        }
        if (blocks.size() == 1) {
            OrbitingBlock removed = blocks.getFirst();
            clear();
            return removed;
        }
        float initialYRot = blocks.get((index + 1) % blocks.size()).getYRot();
        OrbitingBlock removed = blocks.remove(index);
        reset(blocks.stream().map(block -> Pair.of(block.getBlockState(), block.isAutoCreated())).toList(), initialYRot);
        return removed;
    }

    public void reset(List<Pair<BlockState, Boolean>> blocks, float initialYRot) {
        orbitingBlocks.clear();
        ImmutableList.Builder<OrbitingBlock> builder = ImmutableList.builder();
        int count = blocks.size();
        for (int i = 0; i < count; i++) {
            float yRot = Mth.wrapDegrees(initialYRot + 360F * i / count);
            builder.add(new OrbitingBlock(blocks.get(i).getFirst(), getBlockSize(), getOrbitRadius(), getOrbitHeightMultiplier(), blocks.get(i).getSecond(), yRot));
        }
        orbitingBlocks.addAll(builder.build());
    }

    public boolean isEmpty() {
        return orbitingBlocks.isEmpty();
    }

    public void clear() {
        orbitingBlocks.clear();
    }

    public void update() {
        for (OrbitingBlock block : orbitingBlocks) {
            block.addYRot(getRotatingSpeed());
        }
    }

    public List<OrbitingBlock> getOrbitingBlocks() {
        return Collections.unmodifiableList(orbitingBlocks);
    }

    public float getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(float blockSize) {
        this.blockSize = blockSize;
    }

    public float getRotatingSpeed() {
        return rotatingSpeed;
    }

    public void setRotatingSpeed(float rotatingSpeed) {
        this.rotatingSpeed = rotatingSpeed;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public void setOrbitRadius(double orbitRadius) {
        this.orbitRadius = orbitRadius;
    }

    public double getOrbitHeightMultiplier() {
        return orbitHeightMultiplier;
    }

    public void setOrbitHeightMultiplier(double orbitHeightMultiplier) {
        this.orbitHeightMultiplier = orbitHeightMultiplier;
    }

    public enum Serializer implements IAttachmentSerializer<OrbitingBlockHolder> {
        INSTANCE;

        private static final String ORBIT_BLOCKS_TAG = "OrbitingBlocks";
        private static final String BLOCK_SIZE_TAG = "BlockSize";
        private static final String ROTATING_SPEED_TAG = "RotatingSpeed";
        private static final String ORBIT_RADIUS_TAG = "OrbitRadius";
        private static final String ORBIT_HEIGHT_MULTIPLIER_TAG = "OrbitHeightMultiplier";

        @Override
        public OrbitingBlockHolder read(IAttachmentHolder holder, ValueInput input) {
            ValueInput.ValueInputList orbitBlocksInputList = input.childrenListOrEmpty(ORBIT_BLOCKS_TAG);
            List<OrbitingBlock> orbitingBlocks = new ArrayList<>();
            for (ValueInput blockInput : orbitBlocksInputList) {
                orbitingBlocks.add(OrbitingBlock.load(blockInput));
            }
            float blockSize = input.getFloatOr(BLOCK_SIZE_TAG, HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE);
            float rotatingSpeed = input.getFloatOr(ROTATING_SPEED_TAG, HarpConstants.DEFAULT_ORBITING_BLOCK_ROTATING_SPEED);
            double orbitRadius = input.getDoubleOr(ORBIT_RADIUS_TAG, HarpConstants.DEFAULT_ORBIT_RADIUS);
            double orbitHeightMultiplier = input.getDoubleOr(ORBIT_HEIGHT_MULTIPLIER_TAG, HarpConstants.DEFAULT_ORBITING_BLOCK_HEIGHT_MUL);
            return new OrbitingBlockHolder(orbitingBlocks, blockSize, rotatingSpeed, orbitRadius, orbitHeightMultiplier);
        }

        @Override
        public boolean write(OrbitingBlockHolder holder, ValueOutput output) {
            ValueOutput.ValueOutputList orbitBlocksInputList = output.childrenList(ORBIT_BLOCKS_TAG);
            for (OrbitingBlock block : holder.getOrbitingBlocks()) {
                block.store(orbitBlocksInputList.addChild());
            }
            output.putFloat(BLOCK_SIZE_TAG, holder.getBlockSize());
            output.putFloat(ROTATING_SPEED_TAG, holder.getRotatingSpeed());
            output.putDouble(ORBIT_RADIUS_TAG, holder.getOrbitRadius());
            output.putDouble(ORBIT_HEIGHT_MULTIPLIER_TAG, holder.getOrbitHeightMultiplier());
            return true;
        }
    }
}
