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

package illusnow.tjchase.item.enchantment;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItemNames;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class EntityDebugStickItem extends Item {
    public static final String SET_FACING_SOUTH = makeMsgKey("facing_south");
    public static final String SET_FACING_WEST = makeMsgKey("facing_west");
    public static final String SET_FACING_NORTH = makeMsgKey("facing_north");
    public static final String SET_FACING_EAST = makeMsgKey("facing_east");
    public static final String[] SET_FACING = new String[]{SET_FACING_SOUTH, SET_FACING_WEST, SET_FACING_NORTH, SET_FACING_EAST};
    public static final String RESUME = makeMsgKey("resume_ai");
    public static final String KILL = makeMsgKey("kill");
    public static final String REMOVE = makeMsgKey("remove");

    public EntityDebugStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!(interactionTarget instanceof Mob mob)) {
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
        if (player.isShiftKeyDown() && mob.isNoAi()) {
            mob.setNoAi(false);
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable(RESUME), true);
            }
            return InteractionResult.SUCCESS;
        }
        if (!mob.isNoAi()) {
            mob.setNoAi(true);
            mob.snapTo(Vec3.atBottomCenterOf(mob.blockPosition()), 0, 0);
            mob.setYHeadRot(0);
            mob.setYBodyRot(0);
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable(SET_FACING_SOUTH), true);
            }
        } else {
            float yRot = mob.getYRot();
            int rounded = Math.round(yRot);
            float newYRot;
            if (rounded % 90 != 0) {
                newYRot = Math.round(rounded / 90F) * 90;
            } else {
                newYRot = (yRot + 90) % 360;
            }
            mob.setYRot(newYRot);
            mob.setYBodyRot(newYRot);
            mob.setYHeadRot(newYRot);
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.translatable(SET_FACING[(int) newYRot / 90]), true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity.level() instanceof ServerLevel level && entity.isAttackable()) {
            if (player.isShiftKeyDown()) {
                entity.remove(Entity.RemovalReason.KILLED);
                entity.gameEvent(GameEvent.ENTITY_DIE);
                if (!entity.isAlive()) {
                    player.displayClientMessage(Component.translatable(REMOVE, entity.getDisplayName()), true);
                }
            } else {
                entity.kill(level);
                if (!entity.isAlive()) {
                    player.displayClientMessage(Component.translatable(KILL, entity.getDisplayName()), true);
                }
            }
        }
        return true;
    }

    private static String makeMsgKey(String facing) {
        return TJChase.prefixMsg(ModItemNames.ENTITY_DEBUG_STICK + "." + facing);
    }
}
