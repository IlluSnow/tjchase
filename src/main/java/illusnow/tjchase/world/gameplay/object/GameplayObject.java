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

package illusnow.tjchase.world.gameplay.object;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.Nameable;

import javax.annotation.Nullable;

public interface GameplayObject<T extends GameplayObjectType<?>> extends Nameable {
    T getGameplayObjectType();

    boolean isTemplate();

    boolean isValid();

    CustomPacketPayload createServerboundPayload(Template<?> template, String name);

    @Override
    default boolean hasCustomName() {
        if (isTemplate()) {
            return Nameable.super.hasCustomName();
        }
        return false;
    }

    @Nullable
    @Override
    default Component getCustomName() {
        if (isTemplate()) {
            return Nameable.super.getCustomName();
        }
        return null;
    }

    static <O extends GameplayObject<T>, T extends GameplayObjectType<O>> Template<O> saveAsTemplate(O obj) {
        return new Template<>(obj.getGameplayObjectType().getEditableValues(), obj);
    }

    static <O extends GameplayObject<?>> void loadFromTemplate(O obj, Template<O> template) {
        template.loadTemplateData(obj);
    }
}
