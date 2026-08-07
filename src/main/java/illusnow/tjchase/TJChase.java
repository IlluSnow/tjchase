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

package illusnow.tjchase;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModDataComponents;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.item.enchantment.ModEnchantmentEffectComponents;
import illusnow.tjchase.particle.ModParticleTypes;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.ModMolangQueries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
        ModAttachments.ATTACHMENT_TYPES.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(bus);
        ModDataComponents.DATA_COMPONENTS.register(bus);
        ModEnchantmentEffectComponents.ENCHANTMENT_COMPONENT_TYPES.register(bus);
        ModEntities.ENTITY_TYPES.register(bus);
        ModEntityDataSerializers.ENTITY_DATA_SERIALIZERS.register(bus);
        ModItems.ITEMS.register(bus);
        ModParticleTypes.PARTICLE_TYPES.register(bus);
        ModSoundEvents.SOUND_EVENTS.register(bus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        ModMolangQueries.register();
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

    public static String prefixCommand(String name) {
        return prefix("commands", name);
    }

    public static String prefixEnum(String name) {
        return MODID + ":" + name;
    }
}
