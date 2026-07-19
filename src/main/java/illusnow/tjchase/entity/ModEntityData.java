package illusnow.tjchase.entity;

import illusnow.tjchase.TJChase;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public class ModEntityData {
    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Unused parameter in NeoForge's code for new entities
        RegisterSpawnPlacementsEvent.Operation dummyOperationForModEntities = RegisterSpawnPlacementsEvent.Operation.OR;
        event.register(ModEntities.ZURI.get(), SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, dummyOperationForModEntities);
    }

    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ZURI.get(), Zuri.createAttributes().build());
    }
}
