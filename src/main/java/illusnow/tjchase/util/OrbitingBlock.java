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

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class OrbitingBlock {
    private static final String BLOCK_STATE_TAG = "BlockState";
    private static final String BLOCK_SIZE_TAG = "BlockSize";
    private static final String ORBIT_RADIUS_TAG = "OrbitRadius";
    private static final String ORBIT_HEIGHT_MULTIPLIER_TAG = "OrbitHeightMultiplier";
    private static final String AUTO_CREATED_TAG = "AutoCreated";
    private static final String Y_ROT_O_TAG = "YRotO";
    private static final String Y_ROT_TAG = "YRot";
    public static final StreamCodec<RegistryFriendlyByteBuf, OrbitingBlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), OrbitingBlock::getBlockState,
            ByteBufCodecs.FLOAT, OrbitingBlock::getBlockSize,
            ByteBufCodecs.DOUBLE, OrbitingBlock::getOrbitRadius,
            ByteBufCodecs.DOUBLE, OrbitingBlock::getOrbitHeightMultiplier,
            ByteBufCodecs.BOOL, OrbitingBlock::isAutoCreated,
            ByteBufCodecs.FLOAT, OrbitingBlock::getYRotO,
            ByteBufCodecs.FLOAT, OrbitingBlock::getYRot,
            OrbitingBlock::new
    );

    private final BlockState blockState;
    private final float blockSize;
    private final double orbitRadius; // Blocks
    private final double orbitHeightMultiplier;
    private final boolean autoCreated;
    private float yRotO; // Degrees (-180 ~ 180)
    private float yRot; // Degrees (-180 ~ 180)

    public OrbitingBlock(BlockState blockState, float blockSize, double orbitRadius, double orbitHeightMultiplier, boolean autoCreated, float yRot) {
        this(blockState, blockSize, orbitRadius, orbitHeightMultiplier, autoCreated, yRot, yRot);
    }

    public OrbitingBlock(BlockState blockState, float blockSize, double orbitRadius, double orbitHeightMultiplier, boolean autoCreated, float yRotO, float yRot) {
        this.blockState = blockState;
        this.blockSize = blockSize;
        this.orbitRadius = orbitRadius;
        this.orbitHeightMultiplier = orbitHeightMultiplier;
        this.autoCreated = autoCreated;
        this.yRotO = Mth.wrapDegrees(yRotO);
        this.yRot = Mth.wrapDegrees(yRot);
    }

    public static OrbitingBlock load(ValueInput input) {
        BlockState blockState = input.read(BLOCK_STATE_TAG, BlockState.CODEC).orElseThrow();
        float blockSize = input.getFloatOr(BLOCK_SIZE_TAG, HarpConstants.DEFAULT_ORBITING_BLOCK_SIZE);
        double orbitRadius = input.getDoubleOr(ORBIT_RADIUS_TAG, -1);
        if (orbitRadius < 0) {
            throw new IllegalStateException("Invalid orbit radius: " + orbitRadius);
        }
        double orbitHeightMultiplier = input.getDoubleOr(ORBIT_HEIGHT_MULTIPLIER_TAG, HarpConstants.DEFAULT_ORBITING_BLOCK_HEIGHT_MUL);
        boolean autoCreated = input.getBooleanOr(AUTO_CREATED_TAG, false);
        float yRotO = input.getFloatOr(Y_ROT_O_TAG, 0);
        float yRot = input.getFloatOr(Y_ROT_TAG, 0);
        return new OrbitingBlock(blockState, blockSize, orbitRadius, orbitHeightMultiplier, autoCreated, yRotO, yRot);
    }

    public static Vec3 calculateCenterWorldPos(Entity entity, Vec3 position, float yRot, double orbitRadius, double orbitHeightMultiplier) {
        Vec3 centerPos = position.add(0, entity.getBbHeight() * orbitHeightMultiplier, 0);
        Vec3 lookVec = entity.calculateViewVector(0, yRot);
        Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();
        return centerPos.add(horizontalLookVec.scale(orbitRadius));
    }

    public Vec3 calculateCenterWorldPos(Entity entity, float yRot) {
        return calculateCenterWorldPos(entity, entity.position(), yRot, orbitRadius, orbitHeightMultiplier);
    }

    public Vec3 calculateBottomCenterWorldPos(Entity entity, float yRot) {
        Vec3 center = calculateCenterWorldPos(entity, yRot);
        AABB bounds = getBlockState().getShape(entity.level(), BlockPos.containing(center)).bounds();
        float height = (float) (bounds.maxY - bounds.minY);
        return center.add(0, -getBlockSize() * height / 2, 0);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public float getBlockSize() {
        return blockSize;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public double getOrbitHeightMultiplier() {
        return orbitHeightMultiplier;
    }

    public boolean isAutoCreated() {
        return autoCreated;
    }

    public float getYRotO() {
        return yRotO;
    }

    public float getYRot() {
        return yRot;
    }

    public float getYRot(float partialTicks) {
        return Mth.rotLerp(partialTicks, yRotO, yRot);
    }

    public void addYRot(float delta) {
        this.yRotO = this.yRot;
        this.yRot += delta;
        yRot = Mth.wrapDegrees(yRot);
    }

    public void store(ValueOutput output) {
        output.store(BLOCK_STATE_TAG, BlockState.CODEC, blockState);
        output.putFloat(BLOCK_SIZE_TAG, blockSize);
        output.putDouble(ORBIT_RADIUS_TAG, orbitRadius);
        output.putDouble(ORBIT_HEIGHT_MULTIPLIER_TAG, orbitHeightMultiplier);
        output.putBoolean(AUTO_CREATED_TAG, autoCreated);
        output.putFloat(Y_ROT_O_TAG, yRotO);
        output.putFloat(Y_ROT_TAG, yRot);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("blockState", blockState)
                .add("blockSize", blockSize)
                .add("orbitRadius", orbitRadius)
                .add("orbitHeightMultiplier", orbitHeightMultiplier)
                .add("autoCreated", autoCreated)
                .add("yRotO", yRotO)
                .add("yRot", yRot)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrbitingBlock block = (OrbitingBlock) o;
        return Double.compare(getOrbitRadius(), block.getOrbitRadius()) == 0
                && Float.compare(getBlockSize(), block.getBlockSize()) == 0
                && Double.compare(getOrbitHeightMultiplier(), block.getOrbitHeightMultiplier()) == 0
                && Boolean.compare(autoCreated, block.autoCreated) == 0
                && Float.compare(yRotO, block.yRotO) == 0
                && Float.compare(yRot, block.yRot) == 0
                && Objects.equals(getBlockState(), block.getBlockState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockState(), getBlockSize(), getOrbitRadius(), getOrbitHeightMultiplier(), autoCreated, yRotO, yRot);
    }
}
