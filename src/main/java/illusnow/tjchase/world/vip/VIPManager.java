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

package illusnow.tjchase.world.vip;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class VIPManager {
    public static final MapCodec<VIPManager> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            VIPType.CODEC.optionalFieldOf("type").forGetter(VIPManager::getType),
            Codec.INT.fieldOf("vip_days_remaining").forGetter(VIPManager::getVipDaysRemaining),
            Codec.INT.fieldOf("vip_immune_ticks").forGetter(VIPManager::getVipImmuneTicks)
    ).apply(instance, VIPManager::new));
    public static final String VIP_DAYS_REMAINING = TJChase.prefix("vip", "days_remaining");
    public static final String VIP_ACTIVATED = TJChase.prefix("vip", "activated");
    public static final String VIP_REPLACED_WARNING = TJChase.prefix("vip", "replaced_warning");
    public static final String VIP_NOT_ACTIVATED = TJChase.prefix("vip", "not_activated");
    public static final String VIP_TOO_MANY_DAYS_REMAINING = TJChase.prefix("vip", "too_long");
    public static final int MAX_VIP_IMMUNE_TICKS = 12000;
    public static final int MAX_VIP_DAYS = 30;
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private VIPType type;
    private int vipDaysRemaining;
    private int vipImmuneTicks;

    public VIPManager() {
        this(Optional.empty(), 0, 0);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public VIPManager(Optional<VIPType> type, int vipDaysRemaining, int vipImmuneTicks) {
        this.type = type.orElse(null);
        this.vipDaysRemaining = vipDaysRemaining;
        this.vipImmuneTicks = vipImmuneTicks;
    }

    public static VIPManager get(Player player) {
        return player.getData(ModAttachments.VIP_MANAGER);
    }

    public static void updateServer(Player player) {
        get(player).tick((ServerPlayer) player);
    }

    private void tick(ServerPlayer player) {
        if (vipImmuneTicks > 0) {
            vipImmuneTicks--;
        }
        int timeOfDay = Utils.getTimeOfDay(player.level());
        if (type != null && timeOfDay == 100) {
            if (vipImmuneTicks == 0) {
                giveDailyReward(player);
                vipImmuneTicks = MAX_VIP_IMMUNE_TICKS;
                vipDaysRemaining--;
                sendDaysRemainingMessage(player);
            } else {
                LOGGER.warn("Attempted to send VIP daily reward too frequently for {}! Reward is available after {} ticks, so waiting for the next morning", player.getDisplayName().getString(), vipImmuneTicks);
            }
        }
        if (vipDaysRemaining == 0) {
            type = null;
        }
    }

    public SetVIPResult setVIP(@Nullable VIPType type) {
        if (type != null && type.getDuration() > MAX_VIP_DAYS) {
            throw new IllegalArgumentException("Invalid VIPType with duration %d days longer than maximum %d days".formatted(type.getDuration(), MAX_VIP_DAYS));
        }
        if (this.type == type) {
            if (type == null) {
                return SetVIPResult.FAILURE;
            }
            if (vipDaysRemaining + type.getDuration() > MAX_VIP_DAYS) {
                return SetVIPResult.TOO_MANY_DAYS_REMAINING;
            }
            vipDaysRemaining += type.getDuration();
            return SetVIPResult.SUCCESS;
        }
        if (type == null) {
            this.type = null;
            vipDaysRemaining = 0;
            return SetVIPResult.SUCCESS;
        }
        if (type.canReplace(this.type)) {
            this.type = type;
            vipDaysRemaining = type.getDuration();
            return SetVIPResult.SUCCESS;
        }
        return SetVIPResult.FAILURE;
    }

    public void giveDailyReward(ServerPlayer player) {
        if (type == null) {
            throw new NullPointerException("VIPType not found");
        }
        for (ItemStack stack : type.getReward().createDailyReward(true)) {
            Utils.giveItem(player, stack);
        }
    }

    public void giveInitialReward(Player player) {
        if (type == null) {
            throw new NullPointerException("VIPType not found");
        }
        Utils.giveItem(player, type.getReward().getInitialReward());
    }

    public void sendDaysRemainingMessage(ServerPlayer player) {
        if (type == null) {
            throw new NullPointerException("VIPType not found");
        }
        player.sendSystemMessage(Component.translatable(VIP_DAYS_REMAINING, type.getDisplayName(), vipDaysRemaining).withStyle(type.getColor()), true);
    }

    public Optional<VIPType> getType() {
        return Optional.ofNullable(type);
    }

    public int getVipDaysRemaining() {
        return vipDaysRemaining;
    }

    public int getVipImmuneTicks() {
        return vipImmuneTicks;
    }

    public enum SetVIPResult {
        SUCCESS,
        TOO_MANY_DAYS_REMAINING,
        FAILURE
    }
}
