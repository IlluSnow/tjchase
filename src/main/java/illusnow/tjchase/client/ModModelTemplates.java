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
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ElementBuilder;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

public final class ModModelTemplates {
    private static final float OFFS = 1e-3F;

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
    public static final ExtendedModelTemplate MAILBOX = ExtendedModelTemplateBuilder.builder()
            .requiredTextureSlot(TextureSlot.PARTICLE)
            .element(builder -> emptyBox(builder, 2, 0, 2, 14, 10, 14))
            .element(builder -> emptyBox(builder, 1.5F, 10, 1.5F, 14.5F, 11, 14.5F))
            .element(builder -> emptyBox(builder, 1, 11, 1, 15, 14, 15))
            .build();

    private static ElementBuilder emptyBox(ElementBuilder builder, float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        return builder.from(fromX + OFFS, fromY + OFFS, fromZ + OFFS).to(toX - OFFS, toY - OFFS, toZ - OFFS).allFaces((d, b) -> b.texture(TextureSlot.PARTICLE));
    }
}
