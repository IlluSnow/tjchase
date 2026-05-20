package illusnow.tjchase.block;

import illusnow.tjchase.TJChase;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(TJChase.MODID);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_ACACIA_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_ACACIA_LEAVES, TintedParticleLeavesBlock::new, Blocks.ACACIA_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, UntintedParticleLeavesBlock> TEMPORARY_AZALEA_LEAVES
            = registerAzaleaLeafCopy(ModBlockNames.TEMPORARY_AZALEA_LEAVES, Blocks.AZALEA_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_BIRCH_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_BIRCH_LEAVES, TintedParticleLeavesBlock::new, Blocks.BIRCH_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_DARK_OAK_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_DARK_OAK_LEAVES, TintedParticleLeavesBlock::new, Blocks.DARK_OAK_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, UntintedParticleLeavesBlock> TEMPORARY_FLOWERING_AZALEA_LEAVES
            = registerAzaleaLeafCopy(ModBlockNames.TEMPORARY_FLOWERING_AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_JUNGLE_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_JUNGLE_LEAVES, TintedParticleLeavesBlock::new, Blocks.JUNGLE_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, Block> TEMPORARY_MOSS_BLOCK
            = registerCopy(ModBlockNames.TEMPORARY_MOSS_BLOCK, Block::new, Blocks.MOSS_BLOCK, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_OAK_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_OAK_LEAVES, TintedParticleLeavesBlock::new, Blocks.OAK_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, TintedParticleLeavesBlock> TEMPORARY_SPRUCE_LEAVES
            = registerLeafCopy(ModBlockNames.TEMPORARY_SPRUCE_LEAVES, TintedParticleLeavesBlock::new, Blocks.SPRUCE_LEAVES, BlockBehaviour.Properties::noLootTable);
    public static final DeferredHolder<Block, Block> TEMPORARY_VINE
            = registerCopy(ModBlockNames.TEMPORARY_VINE, LadderBlock::new, Blocks.LADDER, prop -> prop
            .mapColor(MapColor.PLANT)
            .replaceable()
            .noCollision()
            .strength(0.2F)
            .sound(SoundType.VINE)
            .noLootTable()
            .ignitedByLava());

    private ModBlocks() {}

    private static DeferredHolder<Block, Block> register(String name, UnaryOperator<Block.Properties> op) {
        return register(name, Block::new, op);
    }

    private static <T extends Block> DeferredHolder<Block, T> register(String name, Function<? super Block.Properties, ? extends T> factory, UnaryOperator<Block.Properties> op) {
        return BLOCKS.register(name, () -> factory.apply(op.apply(BlockBehaviour.Properties.of().setId(createBlockId(name)))));
    }

    private static <T extends Block> DeferredHolder<Block, T> registerCopy(String name, Function<? super Block.Properties, ? extends T> factory, BlockBehaviour block, UnaryOperator<Block.Properties> op) {
        return BLOCKS.register(name, () -> factory.apply(op.apply(BlockBehaviour.Properties.ofFullCopy(block).setId(createBlockId(name)))));
    }

    private static DeferredHolder<Block, UntintedParticleLeavesBlock> registerAzaleaLeafCopy(String name, BlockBehaviour block, UnaryOperator<Block.Properties> op) {
        return BLOCKS.register(name, () -> new UntintedParticleLeavesBlock(0.01F, ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, -9399763), op.apply(BlockBehaviour.Properties.ofFullCopy(block).setId(createBlockId(name)))));
    }

    private static <T extends LeavesBlock> DeferredHolder<Block, T> registerLeafCopy(String name, BiFunction<? super Float, ? super Block.Properties, ? extends T> factory, BlockBehaviour block, UnaryOperator<Block.Properties> op) {
        return registerLeafCopy(name, factory, 0.01F, block, op);
    }

    private static <T extends LeavesBlock> DeferredHolder<Block, T> registerLeafCopy(String name, BiFunction<? super Float, ? super Block.Properties, ? extends T> factory, float leafParticleChance, BlockBehaviour block, UnaryOperator<Block.Properties> op) {
        return BLOCKS.register(name, () -> factory.apply(leafParticleChance, op.apply(BlockBehaviour.Properties.ofFullCopy(block).setId(createBlockId(name)))));
    }

    private static ResourceKey<Block> createBlockId(String name) {
        return ResourceKey.create(Registries.BLOCK, TJChase.prefix(name));
    }
}
