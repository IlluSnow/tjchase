/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package illusnow.tjchase.world.inventory;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.util.Utils;
import illusnow.tjchase.world.MysteriousShopTrade;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MysteriousShopMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int TRADE_SLOT_START = 1;
    public static final int TRADE_SLOT_COUNT = 6;
    public static final int PLAYER_SLOT_START = 7;
    public static final int VISIBLE_TRADE_COUNT = 4;
    public static final int GOLDEN_FEATHER_X = 35;
    public static final int GOLDEN_FEATHER_Y = 90;
    public static final int TRADE_X = 76;
    public static final int TRADE_Y0 = 40;
    public static final int TRADE_WIDTH = 125;
    public static final int TRADE_HEIGHT = 18;
    public static final String REFRESH_SECONDS = TJChase.prefixGuiMsg(MysteriousShopTrade.NAME + ".time_remaining");
    private final Container input = new SimpleContainer(1);
    private final List<MysteriousShopTrade> trades;
    private final ContainerLevelAccess access;
    private final long seed;

    public MysteriousShopMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public MysteriousShopMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    private MysteriousShopMenu(int containerId, Inventory inventory, @Nullable RegistryFriendlyByteBuf buf, ContainerLevelAccess access) {
        super(ModMenuTypes.MYSTERIOUS_SHOP.get(), containerId);
        this.access = access;
        UUID uuid = inventory.player.getUUID();
        int day = Utils.getDay(inventory.player.level());
        this.seed = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits() ^ day;
        this.trades = createTrades(inventory.player.level().registryAccess(), seed);
        addSlot(new Slot(input, 0, GOLDEN_FEATHER_X, GOLDEN_FEATHER_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.GOLDEN_FEATHER.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (int i = 0; i < TRADE_SLOT_COUNT; i++) {
            final int slot = i;
            addSlot(new Slot(new SimpleContainer(1), 0, TRADE_X + 1, TRADE_Y0 + 1 + i * TRADE_HEIGHT) {
                @Override
                public boolean isActive() {
                    return getTrade(slot) != null;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }

                @Override
                public ItemStack getItem() {
                    MysteriousShopTrade trade = getTrade(slot);
                    return trade == null ? ItemStack.EMPTY : trade.result(inventory.player.level(), seed);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        addStandardInventorySlots(inventory, 36, 137);
    }

    public MysteriousShopMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        this(containerId, inventory, null, access);
    }

    @Nullable
    private MysteriousShopTrade getTrade(int visibleIndex) {
        if (input.getItem(0).isEmpty()) {
            return null;
        }
        return visibleIndex >= 0 && visibleIndex < trades.size() ? trades.get(visibleIndex) : null;
    }

    public List<MysteriousShopTrade> getTrades() {
        return trades;
    }

    public long getSeed() {
        return seed;
    }

    public boolean hasInput() {
        return !input.getItem(0).isEmpty();
    }

    private static List<MysteriousShopTrade> createTrades(RegistryAccess access, long seed) {
        List<MysteriousShopTrade> permanent = new ArrayList<>();
        List<MysteriousShopTrade> refreshable = new ArrayList<>();
        for (Holder<MysteriousShopTrade> holder : access.lookupOrThrow(ModRegistries.MYSTERIOUS_SHOP_TRADES_KEY).listElements().toList()) {
            MysteriousShopTrade trade = holder.value();
            if (trade.permanent()) {
                permanent.add(trade);
            } else {
                refreshable.add(trade);
            }
        }
        permanent.sort(Comparator.comparing(MysteriousShopTrade::id));
        int maxRefreshableTradeCount = VISIBLE_TRADE_COUNT - permanent.size();
        refreshable = MysteriousShopTrade.getRandomTrades(access, seed, maxRefreshableTradeCount);
        permanent.addAll(refreshable);
        return permanent;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= 0 && id < TRADE_SLOT_COUNT) {
            MysteriousShopTrade trade = getTrade(id);
            if (trade == null || input.getItem(0).isEmpty()) {
                return false;
            }
            if (player.level().isClientSide()) {
                return true;
            }
            input.removeItem(0, 1);
            ItemStack result = trade.result(player.level(), seed);
            Utils.giveItem(player, result);
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == INPUT_SLOT) {
                if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= TRADE_SLOT_START && index < PLAYER_SLOT_START) {
                return ItemStack.EMPTY;
            } else if (stack.is(ModItems.GOLDEN_FEATHER.get())) {
                if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_SLOT_START) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.MAILBOX.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, input);
    }
}
