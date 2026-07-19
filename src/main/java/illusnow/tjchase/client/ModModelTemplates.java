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

package illusnow.tjchase.client;

import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

public final class ModModelTemplates {
    public static final ExtendedModelTemplate HARP = ExtendedModelTemplateBuilder.of(ModelTemplates.FLAT_ITEM)
            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, b ->
                    b.rotation(0, -90, -55).translation(0, 4.0F, 0.5F).scale(0.85F))
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, b ->
                    b.rotation(0, 90, 55).translation(0, 4.0F, 0.5F).scale(0.85F))
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, b ->
                    b.rotation(0, -90, 25).translation(1.13F, 3.2F, 1.13F).scale(0.68F))
            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, b ->
                    b.rotation(0, 90, -25).translation(1.13F, 3.2F, 1.13F).scale(0.68F))
            .build();

    private ModModelTemplates() {}
}
