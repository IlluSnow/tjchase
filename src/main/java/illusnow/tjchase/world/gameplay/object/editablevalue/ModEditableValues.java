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

package illusnow.tjchase.world.gameplay.object.editablevalue;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.util.ModRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEditableValues {
    public static final DeferredRegister<EditableValue<?, ?>> EDITABLE_VALUES = DeferredRegister.create(ModRegistries.EDITABLE_VALUES_KEY, TJChase.MODID);
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Double, Rocket>> BURNING_SPEED = register("rocket_base_burning_speed",
            EditableValue.of(Rocket::getBaseBurningSpeed, Rocket::setBaseBurningSpeed).withDefaultValue(1.0).withBehavior(EditableValueBehaviors.ofDouble(0.1, 10)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Integer, Rocket>> DEFAULT_FUSE_SECONDS = register("rocket_fuse_seconds",
            EditableValue.of(Rocket::getDefaultFuseSeconds, Rocket::setDefaultFuseSeconds).withDefaultValue(Rocket.DEFAULT_FUSE_SECONDS).withBehavior(EditableValueBehaviors.ofInt(1, 600)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Integer, Rocket>> INSTABURN_SECONDS = register("rocket_instaburn_seconds",
            EditableValue.of(Rocket::getInstaburnSeconds, Rocket::setInstaburnSeconds).withDefaultValue(10).withBehavior(EditableValueBehaviors.ofInt(0, 600)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Rocket.FuseDisplayDirection, Rocket>> FUSE_DISPLAY_DIRECTION = register("rocket_fuse_display_direction",
            EditableValue.of(Rocket::getFuseDisplayDirection, Rocket::setFuseDisplayDirection).withDefaultValue(Rocket.FuseDisplayDirection.FRONT).withBehavior(EditableValueBehaviors.ofEnum(Rocket.FuseDisplayDirection.values(), Rocket.FuseDisplayDirection.CODEC, Rocket.FuseDisplayDirection.STREAM_CODEC)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Double, Rocket>> FUSE_DISPLAY_DISTANCE = register("rocket_fuse_display_distance",
            EditableValue.of(Rocket::getFuseDisplayDistance, Rocket::setFuseDisplayDistance).withDefaultValue(1.3).withBehavior(EditableValueBehaviors.ofDouble(0, 10)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Double, Rocket>> FUSE_DISPLAY_HEIGHT_OFFSET = register("rocket_fuse_display_height_offset",
            EditableValue.of(Rocket::getFuseDisplayHeightOffset, Rocket::setFuseDisplayHeightOffset).withDefaultValue(0.0).withBehavior(EditableValueBehaviors.ofDouble(-10, 10)));
    public static final DeferredHolder<EditableValue<?, ?>, EditableValue<Double, Rocket>> FUSE_DISPLAY_FONT_SCALE = register("rocket_fuse_display_font_scale",
            EditableValue.of(Rocket::getFuseDisplayFontScale, Rocket::setFuseDisplayFontScale).withDefaultValue(1.0).withBehavior(EditableValueBehaviors.ofDouble(0.2, 5)));

    private ModEditableValues() {}

    private static <T, O> DeferredHolder<EditableValue<?, ?>, EditableValue<T, O>> register(String name, EditableValue.Builder<T, O> builder) {
        return EDITABLE_VALUES.register(name, () -> builder.build(name));
    }
}
