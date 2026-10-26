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

import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.world.vip.VIPManager;
import illusnow.tjchase.world.vip.VIPType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;

public class VIPInvitationLetterItem extends Item {
    public VIPInvitationLetterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            ItemStack stack = serverPlayer.getItemInHand(hand);
            VIPManager manager = VIPManager.get(serverPlayer);
            VIPType prevType = manager.getType().orElse(null);
            VIPType type = stack.get(ModDataComponents.VIP_TYPE);
            if (type == null) {
                return super.use(level, player, hand);
            }
            VIPManager.SetVIPResult result = manager.setVIP(type);
            if (result == VIPManager.SetVIPResult.SUCCESS) {
                serverPlayer.sendSystemMessage(Component.translatable(VIPManager.VIP_ACTIVATED, type.getDisplayName()));
                if (prevType != null && type.canReplace(prevType)) {
                    serverPlayer.sendSystemMessage(Component.translatable(VIPManager.VIP_REPLACED_WARNING, prevType.getDisplayName(), type.getDisplayName()).withStyle(ChatFormatting.GOLD));
                }
                manager.giveInitialReward(serverPlayer);
                manager.sendDaysRemainingMessage(serverPlayer);
                stack.consume(1, serverPlayer);
                serverPlayer.awardStat(Stats.ITEM_USED.get(this));
                level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), ModSoundEvents.INVITATION_LETTER_OPEN.get(), SoundSource.PLAYERS);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                Objects.requireNonNull(prevType);
                if (result == VIPManager.SetVIPResult.TOO_MANY_DAYS_REMAINING) {
                    serverPlayer.sendSystemMessage(Component.translatable(VIPManager.VIP_TOO_MANY_DAYS_REMAINING, VIPManager.MAX_VIP_DAYS).withStyle(ChatFormatting.RED));
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable(VIPManager.VIP_NOT_ACTIVATED, type.getDisplayName(), prevType.getDisplayName()).withStyle(ChatFormatting.RED));
                }
            }
        }
        return super.use(level, player, hand);
    }
}
