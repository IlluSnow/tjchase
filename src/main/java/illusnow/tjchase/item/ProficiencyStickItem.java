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
import illusnow.tjchase.entity.TJChaseCharacter;
import illusnow.tjchase.entity.proficency.ProficiencyLevel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ProficiencyStickItem extends Item {
    public static final String INVALID_ENTITY = makeMsgKey("invalid_entity");
    public static final String PROFICIENCY_NOT_SET = makeMsgKey("proficiency_not_set");
    public static final String SET_PROFICIENCY = makeMsgKey("set_proficiency");
    public static final String GET_PROFICIENCY = makeMsgKey("get_proficiency");

    public ProficiencyStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!(interactionTarget instanceof TJChaseCharacter tjc)) {
            player.displayClientMessage(Component.translatable(INVALID_ENTITY, interactionTarget.getDisplayName()).withStyle(ChatFormatting.RED), true);
            return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
        }
        if (player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable(GET_PROFICIENCY, interactionTarget.getDisplayName(), tjc.getProficiencyLevel().makeDisplayName()), true);
        } else {
            Integer proficiencyData = stack.get(ModDataComponents.PROFICIENCY);
            if (proficiencyData == null) {
                player.displayClientMessage(Component.translatable(PROFICIENCY_NOT_SET, stack.getHoverName().copy().withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.RED), true);
                return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
            }
            ProficiencyLevel proficiencyLevel = ProficiencyLevel.fromTotalPoints(proficiencyData);
            player.displayClientMessage(Component.translatable(SET_PROFICIENCY, interactionTarget.getDisplayName(), proficiencyLevel.makeDisplayName()), true);
            tjc.setProficiencyPoints(proficiencyData);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component component = stack.get(DataComponents.ITEM_NAME);
        if (component != null) {
            Integer proficiencyData = stack.get(ModDataComponents.PROFICIENCY);
            if (proficiencyData == null) {
                return component;
            } else {
                ProficiencyLevel proficiencyLevel = ProficiencyLevel.fromTotalPoints(proficiencyData);
                return component.copy().append(CommonComponents.space()).append(ComponentUtils.wrapInSquareBrackets(proficiencyLevel.makeDisplayName().withStyle(proficiencyLevel.getStyle())));
            }
        }
        return super.getName(stack);
    }

    private static String makeMsgKey(String suffix) {
        return TJChase.prefixMsg(ModItemNames.PROFICIENCY_STICK + "." + suffix);
    }
}
