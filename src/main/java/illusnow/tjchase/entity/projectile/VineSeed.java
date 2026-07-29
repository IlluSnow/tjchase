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

package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.VineManager;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.VineGenerator;
import illusnow.tjchase.util.WeightedBlock;
import illusnow.tjchase.util.WeightedBlockSelector;
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
    private static final WeightedBlockSelector DECORATIVE_SELECTOR = new WeightedBlockSelector(
            new WeightedBlock(ModBlocks.TEMPORARY_ACACIA_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_BIRCH_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_OAK_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get().defaultBlockState(), 5),
            new WeightedBlock(ModBlocks.TEMPORARY_AZALEA_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get().defaultBlockState(), 1),
            new WeightedBlock(ModBlocks.TEMPORARY_MOSS_BLOCK.get().defaultBlockState(), 20)
    );
    private static final WeightedBlockSelector PLATFORM_SELECTOR = new WeightedBlockSelector(
            new WeightedBlock(ModBlocks.TEMPORARY_ACACIA_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_BIRCH_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_OAK_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_JUNGLE_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_SPRUCE_LEAVES.get().defaultBlockState(), 10),
            new WeightedBlock(ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get().defaultBlockState(), 5),
            new WeightedBlock(ModBlocks.TEMPORARY_AZALEA_LEAVES.get().defaultBlockState(), 15),
            new WeightedBlock(ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get().defaultBlockState(), 1),
            new WeightedBlock(ModBlocks.TEMPORARY_MOSS_BLOCK.get().defaultBlockState(), 150)
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
            if (!isSilent()) {
                level().playSound(null, bottomCenter, ModSoundEvents.VINE_GROW.get(), SoundSource.AMBIENT, GROW_SOUND_VOLUME, 1.0F);
            }
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
