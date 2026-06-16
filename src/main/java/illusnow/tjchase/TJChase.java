package illusnow.tjchase;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.particle.ModParticleTypes;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TJChase.MODID)
public class TJChase {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tjchase";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public TJChase(IEventBus bus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        bus.addListener(this::commonSetup);

        ModAttachments.ATTACHMENT_TYPES.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(bus);
        ModEnchantmentEffectComponents.ENCHANTMENT_COMPONENT_TYPES.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(bus);
        ModItems.ITEMS.register(bus);
        ModParticleTypes.PARTICLE_TYPES.register(bus);
        ModSoundEvents.SOUND_EVENTS.register(bus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static Identifier prefix(String name) {
        return Identifier.fromNamespaceAndPath(MODID, name);
    }

    public static String prefix(String type, String name) {
        return type + "." + MODID + "." + name;
    }

    public static String prefixMsg(String name) {
        return prefix("message", name);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info("{}{}", Config.magicNumberIntroduction, Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }
}
