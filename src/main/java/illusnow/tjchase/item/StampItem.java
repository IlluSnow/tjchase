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

import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.world.GoldenFeatherLuckinessHelper;
import illusnow.tjchase.world.MailboxDelivery;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class StampItem extends Item {
    public StampItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.getBlockState(context.getClickedPos()).is(ModBlocks.MAILBOX.get())) {
            return super.useOn(context);
        }
        if (level.isClientSide()) {
            return super.useOn(context);
        }
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        boolean success = false;
        if (player != null) {
            if (player.isShiftKeyDown()) {
                boolean goldenFeather = GoldenFeatherLuckinessHelper.checkWhetherToGiveGoldenFeather(player);
                ItemStack envelope = new ItemStack(goldenFeather ? ModItems.MYSTERIOUS_ENVELOPE.get() : ModItems.ENVELOPE.get());
                if (!player.getInventory().add(envelope)) {
                    player.drop(envelope, false);
                }
                success = true;
            } else {
                success = MailboxDelivery.start((ServerLevel) level, context.getClickedPos(), player);
            }
        }
        if (success) {
            context.getItemInHand().consume(1, context.getPlayer());
            player.awardStat(Stats.ITEM_USED.get(ModItems.STAMP.get()));
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }
}
