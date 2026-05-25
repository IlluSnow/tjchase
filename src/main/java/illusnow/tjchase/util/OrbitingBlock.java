package illusnow.tjchase.util;

import com.google.common.base.MoreObjects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class OrbitingBlock {
    private static final String BLOCK_STATE_TAG = "BlockState";
    private static final String ORBIT_RADIUS_TAG = "OrbitRadius";
    private static final String Y_ROT_O_TAG = "YRotO";
    private static final String Y_ROT_TAG = "YRot";
    public static final StreamCodec<RegistryFriendlyByteBuf, OrbitingBlock> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), OrbitingBlock::getBlockState,
            ByteBufCodecs.DOUBLE, OrbitingBlock::getOrbitRadius,
            ByteBufCodecs.FLOAT, OrbitingBlock::getYRotO,
            ByteBufCodecs.FLOAT, OrbitingBlock::getYRot,
            OrbitingBlock::new
    );

    private final BlockState blockState;
    private final double orbitRadius; // Blocks
    private float yRotO; // Degrees (-180 ~ 180)
    private float yRot; // Degrees (-180 ~ 180)

    public OrbitingBlock(BlockState blockState, double orbitRadius, float yRot) {
        this(blockState, orbitRadius, yRot, yRot);
    }

    public OrbitingBlock(BlockState blockState, double orbitRadius, float yRotO, float yRot) {
        this.blockState = blockState;
        this.orbitRadius = orbitRadius;
        this.yRotO = Mth.wrapDegrees(yRotO);
        this.yRot = Mth.wrapDegrees(yRot);
    }

    public static OrbitingBlock load(ValueInput input) {
        BlockState blockState = input.read(BLOCK_STATE_TAG, BlockState.CODEC).orElseThrow();
        double orbitRadius = input.getDoubleOr(ORBIT_RADIUS_TAG, -1);
        if (orbitRadius < 0) {
            throw new IllegalStateException("Invalid orbit radius: " + orbitRadius);
        }
        float yRotO = input.getFloatOr(Y_ROT_O_TAG, 0);
        float yRot = input.getFloatOr(Y_ROT_TAG, 0);
        return new OrbitingBlock(blockState, orbitRadius, yRotO, yRot);
    }

    public static Vec3 calculateWorldPos(Entity entity, Vec3 position, float yRot, double orbitRadius) {
        Vec3 eyePos = position.add(0, entity.getBbHeight() * HarpConstants.ORBITING_BLOCK_HEIGHT_MUL, 0);
        Vec3 lookVec = entity.calculateViewVector(0, yRot);
        Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize();
        return eyePos.add(horizontalLookVec.scale(orbitRadius));
    }

    public Vec3 calculateWorldPos(Entity entity, float yRot) {
        return calculateWorldPos(entity, entity.position(), yRot, orbitRadius);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public double getOrbitRadius() {
        return orbitRadius;
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
        output.putDouble(ORBIT_RADIUS_TAG, orbitRadius);
        output.putFloat(Y_ROT_O_TAG, yRotO);
        output.putFloat(Y_ROT_TAG, yRot);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("blockState", blockState)
                .add("orbitRadius", orbitRadius)
                .add("yRotO", yRotO)
                .add("yRot", yRot)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OrbitingBlock block = (OrbitingBlock) o;
        return Double.compare(getOrbitRadius(), block.getOrbitRadius()) == 0 && Float.compare(yRotO, block.yRotO) == 0 && Float.compare(yRot, block.yRot) == 0 && Objects.equals(getBlockState(), block.getBlockState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBlockState(), getOrbitRadius(), yRotO, yRot);
    }
}
