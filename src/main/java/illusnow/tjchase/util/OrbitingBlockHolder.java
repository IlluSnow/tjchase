package illusnow.tjchase.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrbitingBlockHolder {
    public static final StreamCodec<RegistryFriendlyByteBuf, OrbitingBlockHolder> STREAM_CODEC = StreamCodec.composite(
            OrbitingBlock.STREAM_CODEC.apply(ByteBufCodecs.list()), OrbitingBlockHolder::getOrbitingBlocks,
            OrbitingBlockHolder::new
    );
    private final List<OrbitingBlock> orbitingBlocks = new ArrayList<>();

    public OrbitingBlockHolder(List<OrbitingBlock> orbitingBlocks) {
        if (!orbitingBlocks.isEmpty()) {
            double orbitRadius = orbitingBlocks.getFirst().getOrbitRadius();
            for (OrbitingBlock block : orbitingBlocks) {
                if (block.getOrbitRadius() != orbitRadius) {
                    throw new IllegalArgumentException("All orbiting blocks must have the same orbit radius");
                }

            }
            this.orbitingBlocks.addAll(orbitingBlocks);
        }
    }

    public static OrbitingBlockHolder emptyHolder() {
        return new OrbitingBlockHolder(List.of());
    }

    public OrbitingBlock removeLookingBlock(float yHeadRot) {
        yHeadRot = Mth.wrapDegrees(yHeadRot);
        if (orbitingBlocks.isEmpty()) {
            throw new IllegalStateException("There are no orbiting blocks!");
        }
        int index = 0;
        float min = Mth.abs(Mth.wrapDegrees(orbitingBlocks.getFirst().getYRot() - yHeadRot));
        for (int i = 1; i < orbitingBlocks.size(); i++) {
            if (Mth.abs(Mth.wrapDegrees(orbitingBlocks.get(i).getYRot() - yHeadRot)) < min) {
                min = Mth.abs(Mth.wrapDegrees(orbitingBlocks.get(i).getYRot() - yHeadRot));
                index = i;
            }
        }
        return remove(index);
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
        reset(blocks.stream().map(OrbitingBlock::getBlockState).toList(), blocks.getFirst().getOrbitRadius(), initialYRot);
        return removed;
    }

    public void reset(List<BlockState> blocks, double orbitRadius, float initialYRot) {
        orbitingBlocks.clear();
        ImmutableList.Builder<OrbitingBlock> builder = ImmutableList.builder();
        int count = blocks.size();
        for (int i = 0; i < count; i++) {
            float yRot = Mth.wrapDegrees(initialYRot + 360F * i / count);
            builder.add(new OrbitingBlock(blocks.get(i), orbitRadius, yRot));
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
            block.addYRot(HarpConstants.ORBITING_BLOCK_ROTATING_SPEED);
        }
    }

    public List<OrbitingBlock> getOrbitingBlocks() {
        return Collections.unmodifiableList(orbitingBlocks);
    }

    public enum Serializer implements IAttachmentSerializer<OrbitingBlockHolder> {
        INSTANCE;

        private static final String ORBIT_BLOCKS_TAG = "OrbitingBlocks";

        @Override
        public OrbitingBlockHolder read(IAttachmentHolder holder, ValueInput input) {
            ValueInput.ValueInputList orbitBlocksInputList = input.childrenListOrEmpty(ORBIT_BLOCKS_TAG);
            List<OrbitingBlock> orbitingBlocks = new ArrayList<>();
            for (ValueInput blockInput : orbitBlocksInputList) {
                orbitingBlocks.add(OrbitingBlock.load(blockInput));
            }
            return new OrbitingBlockHolder(orbitingBlocks);
        }

        @Override
        public boolean write(OrbitingBlockHolder holder, ValueOutput output) {
            ValueOutput.ValueOutputList orbitBlocksInputList = output.childrenList(ORBIT_BLOCKS_TAG);
            for (OrbitingBlock block : holder.orbitingBlocks) {
                block.store(orbitBlocksInputList.addChild());
            }
            return true;
        }
    }
}
