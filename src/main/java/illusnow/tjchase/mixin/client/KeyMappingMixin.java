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

package illusnow.tjchase.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import illusnow.tjchase.client.ClientPassivePlayerHelper;
import illusnow.tjchase.util.Utils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {
    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean modifyDown(boolean original) {
        if (tjChase$isDisabled((KeyMapping) (Object) this)) {
            return false;
        }
        return original;
    }

    @ModifyReturnValue(method = "consumeClick", at = @At(value = "RETURN", ordinal = 1))
    private boolean modifyConsumeClick(boolean original) {
        if (tjChase$isDisabled((KeyMapping) (Object) this)) {
            return false;
        }
        return original;
    }

    @Unique
    private static boolean tjChase$isDisabled(KeyMapping keyMapping) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && Utils.isPassive(player)) {
            return ClientPassivePlayerHelper.isKeyDisabled(keyMapping);
        }
        return false;
    }
}
