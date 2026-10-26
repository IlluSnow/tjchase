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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TJChase.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<MysteriousShopMenu>> MYSTERIOUS_SHOP = MENUS.register(
            "mysterious_shop", () -> IMenuTypeExtension.create(MysteriousShopMenu::new));

    private ModMenuTypes() {}
}
