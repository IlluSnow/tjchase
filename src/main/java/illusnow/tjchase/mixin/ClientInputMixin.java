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

package illusnow.tjchase.mixin;

import illusnow.tjchase.util.OriginalInputAccessor;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientInput.class)
public class ClientInputMixin implements OriginalInputAccessor {
    @Unique
    private Input tjChase$originalInput = Input.EMPTY;

    @Unique
    @Override
    public Input tjChase$getOriginalInput() {
        return tjChase$originalInput;
    }

    @Unique
    @Override
    public void tjChase$setOriginalInput(Input originalInput) {
        this.tjChase$originalInput = originalInput;
    }
}
