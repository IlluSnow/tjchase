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
import illusnow.tjchase.world.gameplay.object.GameplayObjectType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public abstract class GameplayObjectEditorItem extends Item {
    public static final String NAME_TRANSLATION_KEY = TJChase.prefix("item", "gameplay_object_editor.carrying");

    public GameplayObjectEditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide() && createGameplayObject(context)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    public abstract GameplayObjectType<?> getLinkedGameplayObject();

    protected abstract boolean createGameplayObject(UseOnContext context);

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        if (stack.has(ModDataComponents.CARRYING_TEMPLATE)) {
            Component gameplayObjName = stack.get(ModDataComponents.GAMEPLAY_OBJECT_NAME);
            if (gameplayObjName == null) {
                gameplayObjName = getLinkedGameplayObject().getDefaultName().copy();
            } else {
                gameplayObjName = gameplayObjName.copy().withStyle(ChatFormatting.ITALIC);
            }
            return Component.translatable(NAME_TRANSLATION_KEY, name, gameplayObjName);
        }
        return name;
    }
}
