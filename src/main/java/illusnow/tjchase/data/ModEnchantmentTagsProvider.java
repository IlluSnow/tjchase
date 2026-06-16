package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.enchantment.ModEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(EnchantmentTags.NON_TREASURE).add(ModEnchantments.FORCEFUL, ModEnchantments.EXPLOSIVE, ModEnchantments.OVERLOAD, ModEnchantments.LIGHTWEIGHT, ModEnchantments.ANTIGRAVITY, ModEnchantments.SEEKING);
        tag(EnchantmentTags.TREASURE).add(ModEnchantments.ANGEL_TOM_PASSIVE_2, ModEnchantments.ANGEL_TOM_WEAPON_2, ModEnchantments.ANGEL_TOM_WEAPON_3);
    }
}
