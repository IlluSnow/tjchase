package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModItemTags.HARPS).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
        tag(Tags.Items.RANGED_WEAPON_TOOLS).addTag(ModItemTags.HARPS);
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
        tag(ItemTags.VANISHING_ENCHANTABLE).add(ModItems.HARP.get(), ModItems.NETHERITE_HARP.get());
    }
}
