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

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin implements illusnow.tjchase.util.ItemStackRenderStateMixinHelper {
    @Unique
    private float tjChase$shelfY;

    @Unique
    @Override
    public float tjChase$getShelfY() {
        return tjChase$shelfY;
    }

    @Unique
    @Override
    public void tjChase$setShelfY(float shelfY) {
        this.tjChase$shelfY = shelfY;
    }
}
