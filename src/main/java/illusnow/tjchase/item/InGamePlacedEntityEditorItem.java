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

package illusnow.tjchase.item;

import illusnow.tjchase.entity.gameplay.InGamePlacedEntity;
import illusnow.tjchase.world.gameplay.object.GameplayObject;
import illusnow.tjchase.world.gameplay.object.GameplayObjectType;
import illusnow.tjchase.world.gameplay.object.Template;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public class InGamePlacedEntityEditorItem<T extends InGamePlacedEntity<T>> extends GameplayObjectEditorItem {
    private final Supplier<EntityType<T>> entityType;
    private final Supplier<GameplayObjectType<T>> gameplayObjectType;

    public InGamePlacedEntityEditorItem(Properties properties, Supplier<EntityType<T>> entityType, Supplier<GameplayObjectType<T>> gameplayObjectType) {
        super(properties);
        this.entityType = entityType;
        this.gameplayObjectType = gameplayObjectType;
    }

    @Override
    protected boolean createGameplayObject(UseOnContext context) {
        context = new BlockPlaceContext(context);
        Vec3 position = getClickedPos(context);
        if (!canCreate(context, position)) {
            return false;
        }
        T entity = entityType.get().create(context.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
        if (entity != null) {
            ItemStack stack = context.getItemInHand();
            Template<?> template = stack.get(ModDataComponents.CARRYING_TEMPLATE);
            if (template != null) {
                entity.loadFromTemplate(template);
                if (stack.get(ModDataComponents.GAMEPLAY_OBJECT_NAME) != null) {
                    entity.setCustomName(stack.get(ModDataComponents.GAMEPLAY_OBJECT_NAME));
                    stack.remove(ModDataComponents.GAMEPLAY_OBJECT_NAME);
                }
                stack.remove(ModDataComponents.CARRYING_TEMPLATE);
            } else if ((template = stack.get(ModDataComponents.PLACE_TEMPLATE)) != null) {
                entity.loadFromTemplate(template);
            }
            entity.setTemplate(true);
            entity.snapTo(position);
            float yRot = (context.getRotation() + 180) % 360F;
            if (yRot < 45 || yRot >= 315) {
                entity.setYRot(0);
            } else if (yRot < 135) {
                entity.setYRot(90);
            } else if (yRot < 225) {
                entity.setYRot(180);
            } else {
                entity.setYRot(270);
            }
            boolean added = context.getLevel().addFreshEntity(entity);
            if (added) {
                entity.onPlaced();
            }
            return added;
        }
        return false;
    }

    @Override
    public GameplayObjectType<?> getLinkedGameplayObject() {
        return gameplayObjectType.get();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return super.isFoil(stack) || stack.has(ModDataComponents.CARRYING_TEMPLATE);
    }

    private boolean canCreate(UseOnContext context, Vec3 position) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return false;
        }
        AABB checkRange = entityType.get().getSpawnAABB(position.x, position.y, position.z);
        if (!level.getEntitiesOfClass(Entity.class, checkRange, entity -> entity instanceof GameplayObject<?>).isEmpty()) {
            return false;
        }
        for (int i = 0; i < entityType.get().getHeight(); i++) {
            BlockPos above = context.getClickedPos().above(i);
            if (!level.getBlockState(above).isEmpty()) {
                if (level.getBlockState(above).canBeReplaced()) {
                    level.destroyBlock(above, false);
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    private static Vec3 getClickedPos(UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        if (!context.getLevel().getBlockState(clickedPos).isAir() && context.getLevel().getBlockState(clickedPos).canBeReplaced()) {
            return Vec3.atBottomCenterOf(clickedPos);
        }
        return Vec3.atBottomCenterOf(clickedPos).add(0, context.getClickLocation().y - clickedPos.getY(), 0);
    }
}
