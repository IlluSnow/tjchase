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

import com.google.common.base.Suppliers;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.util.ModComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public enum VIPType implements StringRepresentable, TooltipProvider {
    GOLD(0, "gold", 7, ChatFormatting.YELLOW, () -> new VIPReward(Items.EMERALD, 3, Items.STICK, Items.COPPER_INGOT)),
    DIAMOND(1, "diamond", 7, ChatFormatting.AQUA, () -> new VIPReward(ModItems.STAMP.get(), 3, Items.BREAD, Items.IRON_INGOT)),
    NETHERITE(2, "netherite", 15, ChatFormatting.LIGHT_PURPLE, () -> new VIPReward(ModItems.STAMP.get(), 5, Items.APPLE, Items.GOLD_INGOT));

    private final int id;
    private final String key;
    private final int duration;
    private final ChatFormatting color;
    private final Supplier<VIPReward> reward;

    public static final String VIP_DAYS_INFO = TJChase.prefix("tooltip", "vip.days_info");
    public static final String VIP_REPLACEABLE_WARNING = TJChase.prefix("tooltip", "vip.replaceable_warning");
    public static final String VIP_FREQUENCY_WARNING = TJChase.prefix("tooltip", "vip.frequency_remaining");
    public static final String VIP_DURATION = TJChase.prefix("tooltip", "vip.duration");
    public static final String VIP_INITIAL_REWARD = TJChase.prefix("tooltip", "vip.initial_reward");
    public static final String VIP_DAILY_REWARD = TJChase.prefix("tooltip", "vip.daily_reward");

    public static final StringRepresentable.EnumCodec<VIPType> CODEC = StringRepresentable.fromEnum(VIPType::values);
    private static final IntFunction<VIPType> BY_ID = ByIdMap.continuous(VIPType::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, VIPType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, VIPType::getId);

    @SuppressWarnings("NullableProblems")
    VIPType(int id, String key, int duration, ChatFormatting color, Supplier<VIPReward> reward) {
        this.id = id;
        this.key = key;
        this.duration = duration;
        this.color = color;
        this.reward = Suppliers.memoize(reward::get);
    }

    @Override
    public String getSerializedName() {
        return key;
    }

    public boolean canReplace(@Nullable VIPType other) {
        return other == null || getId() > other.getId();
    }

    public int getId() {
        return id;
    }

    public int getDuration() {
        return duration;
    }

    public String getDisplayNameId() {
        return TJChase.prefix("vip", key);
    }

    public Component getDisplayName() {
        return Component.translatable(getDisplayNameId());
    }

    public ChatFormatting getColor() {
        return color;
    }

    public VIPReward getReward() {
        return reward.get();
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (canReplace(GOLD)) {
            tooltipAdder.accept(Component.translatable(VIP_REPLACEABLE_WARNING).withStyle(ChatFormatting.GOLD));
        }
        tooltipAdder.accept(Component.translatable(VIP_DURATION, getDuration()));
        tooltipAdder.accept(Component.empty());
        tooltipAdder.accept(Component.translatable(VIP_INITIAL_REWARD));
        ItemStack initialReward = getReward().getInitialReward();
        tooltipAdder.accept(Component.translatable(ModComponents.VANILLA_CONTAINER_ITEM_COUNT_MSGID, initialReward.getHoverName(), initialReward.getCount()).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.empty());
        tooltipAdder.accept(Component.translatable(VIP_DAILY_REWARD));
        for (ItemStack dailyReward : getReward().createDailyReward(true)) {
            tooltipAdder.accept(Component.translatable(ModComponents.VANILLA_CONTAINER_ITEM_COUNT_MSGID, dailyReward.getHoverName(), dailyReward.getCount()).withStyle(ChatFormatting.GRAY));
        }
        tooltipAdder.accept(Component.empty());
        tooltipAdder.accept(Component.translatable(VIP_DAYS_INFO).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true)));
        tooltipAdder.accept(Component.translatable(VIP_FREQUENCY_WARNING, VIPManager.MAX_VIP_IMMUNE_TICKS / 20 / 60).withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(true)));
    }
}
