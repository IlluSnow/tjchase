package illusnow.tjchase.client.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.data.*;
import illusnow.tjchase.item.enchantment.ModEnchantments;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public final class ModDataGenerators {
    @SubscribeEvent
    public static void onGatherDataClient(GatherDataEvent.Client event) {
        event.createProvider(ModLanguageProvider.EnUs::new);
        event.createProvider(ModLanguageProvider.ZhCn::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModParticleDescriptionProvider::new);
        event.createProvider(ModSoundProvider::new);
        event.createProvider(ModEntityTypeTagsProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, ModDamageSources::bootstrap)
                .add(Registries.ENCHANTMENT, ModEnchantments::bootstrap));
        event.createProvider(ModDamageTypeTagsProvider::new);
        event.createProvider(ModEnchantmentTagsProvider::new);
    }

    public static String getDamageTypeMsg(ResourceKey<DamageType> key) {
        return getDamageTypeMsg(key, "");
    }

    public static String getDamageTypeMsg(ResourceKey<DamageType> key, String suffix) {
        return "death.attack." + key.identifier().toString().replace(':', '.') + (suffix.isEmpty() ? "" : ".") + suffix;
    }

    private ModDataGenerators() {}
}
