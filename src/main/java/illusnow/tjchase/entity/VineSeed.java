package illusnow.tjchase.entity;

import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.VineGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class VineSeed extends ThrowableItemProjectile {
    public static final float GROW_SOUND_VOLUME = 7.5F;
    private static final VineGenerator.VineMaterialSelector DECORATIVE_SELECTOR = new VineGenerator.VineMaterialSelector(
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_ACACIA_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_BIRCH_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_OAK_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get().defaultBlockState(), 5),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_AZALEA_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get().defaultBlockState(), 1),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_MOSS_BLOCK.get().defaultBlockState(), 20)
    );
    private static final VineGenerator.VineMaterialSelector PLATFORM_SELECTOR = new VineGenerator.VineMaterialSelector(
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_ACACIA_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_BIRCH_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_OAK_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get().defaultBlockState(), 10),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get().defaultBlockState(), 5),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_AZALEA_LEAVES.get().defaultBlockState(), 15),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get().defaultBlockState(), 1),
            new VineGenerator.VineMaterial(ModBlocks.TEMPORARY_MOSS_BLOCK.get().defaultBlockState(), 150)
    );

    public VineSeed(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public VineSeed(Level level, double x, double y, double z, ItemStack item) {
        super(ModEntities.VINE_SEED.get(), x, y, z, level, item);
    }

    public VineSeed(Level level, LivingEntity owner, ItemStack item) {
        super(ModEntities.VINE_SEED.get(), owner, level, item);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.VINE_SEED.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            int maxHeight = 30;
            BlockPos bottomCenter = BlockPos.containing(result.getLocation());
            Direction direction = Direction.NORTH;
            if (getOwner() != null) {
                direction = Direction.getApproximateNearest(result.getLocation().x - getOwner().getX(), 0, result.getLocation().z - getOwner().getZ()).getOpposite();
            }
            VineGenerator vineGenerator = new VineGenerator(bottomCenter,
                    direction,
                    DECORATIVE_SELECTOR,
                    PLATFORM_SELECTOR);
            for (int i = 0; i < maxHeight; i++) {
                vineGenerator.placeVineOfHeight(level(), i, maxHeight, 1.5);
            }
            vineGenerator.placeTop(level(), maxHeight, 1.25, direction);
            level().playSound(null, bottomCenter, ModSoundEvents.VINE_GROW.get(), SoundSource.AMBIENT, GROW_SOUND_VOLUME, 1.0F);
            VineManager vineManager = new VineManager(ModEntities.VINE_MANAGER.get(), level(), vineGenerator);
            vineManager.setPos(Vec3.atBottomCenterOf(bottomCenter));
            level().addFreshEntity(vineManager);
            discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return super.getAddEntityPacket(entity);
    }
}
