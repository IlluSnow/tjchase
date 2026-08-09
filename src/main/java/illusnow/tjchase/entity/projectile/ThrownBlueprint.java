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

import illusnow.tjchase.entity.BlueprintManager;
import illusnow.tjchase.entity.Linia;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownBlueprint extends ThrowableItemProjectile {
    private static final double HEIGHT = 8;
    private static final double WIDTH = 12;

    public ThrownBlueprint(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    public ThrownBlueprint(Level level, double x, double y, double z, ItemStack item) {
        super(ModEntities.BLUEPRINT.get(), x, y, z, level, item);
    }

    public ThrownBlueprint(Level level, LivingEntity owner, ItemStack item) {
        super(ModEntities.BLUEPRINT.get(), owner, level, item);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            double delta = 0.01;
            Vec3 location = result.getLocation();
            if (result instanceof EntityHitResult entityHitResult && entityHitResult.getEntity().onGround()) {
                location = entityHitResult.getEntity().position();
            }
            double minY = location.y + delta;
            double maxY = minY + HEIGHT;
            double minX = location.x - WIDTH / 2;
            double maxX = location.x + WIDTH / 2;
            double minZ = location.z - WIDTH / 2;
            double maxZ = location.z + WIDTH / 2;
            AABB blueprintArea = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
            if (BlueprintManager.getNearbyBlueprints(level(), blueprintArea.inflate(BlueprintManager.MAX_LENGTH_ALLOWED)).stream().map(BlueprintManager::getBlueprintAABB).anyMatch(blueprintArea::intersects)) {
                spawnAtLocation((ServerLevel) level(), getItem());
            } else {
                BlueprintManager blueprintManager = new BlueprintManager(ModEntities.BLUEPRINT_MANAGER.get(), level());
                blueprintManager.setBlueprintAABB(minX, minY, minZ, maxX, maxY, maxZ);
                if (getOwner() instanceof Player player) {
                    blueprintManager.setOwner(player);
                }
                if (result instanceof EntityHitResult entityHitResult) {
                    Entity entity = entityHitResult.getEntity();
                    if (entity instanceof Mob mob && Linia.isConvertible(mob)) {
                        Linia.convert(mob);
                    }
                }
                blueprintManager.resetConstructTimestamp();
                playSound(ModSoundEvents.BLUEPRINT_RELEASE.get());
                level().addFreshEntity(blueprintManager);
            }
            discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BLUEPRINT.get();
    }
}
