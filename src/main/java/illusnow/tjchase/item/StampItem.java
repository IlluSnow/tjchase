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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.MailboxBlock;
import illusnow.tjchase.util.Utils;
import illusnow.tjchase.world.LuckinessHelper;
import illusnow.tjchase.world.MailboxDelivery;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class StampItem extends Item {
    public static final String INVALID_MAILBOX = TJChase.prefixMsg(ModItemNames.STAMP + ".invalid_mailbox");

    public StampItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return super.useOn(context);
        }
        BlockPos pos = context.getClickedPos();
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        if (!MailboxBlock.isValidMailbox(level, pos)) {
            if (player != null) {
                player.sendSystemMessage(Component.translatable(INVALID_MAILBOX).withStyle(ChatFormatting.RED));
            }
            return super.useOn(context);
        }
        boolean success = false;
        if (player != null) {
            if (player.isShiftKeyDown()) {
                boolean goldenFeather = LuckinessHelper.checkWhetherToGiveGoldenFeather(player);
                ItemStack envelope = new ItemStack(goldenFeather ? ModItems.MYSTERIOUS_ENVELOPE.get() : ModItems.ENVELOPE.get());
                Utils.giveItem(player, envelope);
                success = true;
            } else {
                success = MailboxDelivery.start((ServerLevel) level, pos, player);
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
