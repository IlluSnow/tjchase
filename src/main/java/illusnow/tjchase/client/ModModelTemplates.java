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
