package illusnow.tjchase.world;

import illusnow.tjchase.TJChase;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> ORBITING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("orbiting_block"));
    public static final ResourceKey<DamageType> INDIRECT_ORBITING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("indirect_orbiting_block"));

    public static DamageSource orbitingBlock(Entity orbitingBlock) {
        return source(ORBITING_BLOCK, orbitingBlock, null);
    }

    public static DamageSource indirectOrbitingBlock(Entity orbitingBlock, @Nullable Entity thrower) {
        return source(INDIRECT_ORBITING_BLOCK, orbitingBlock, thrower);
    }

    public static DamageSource source(ResourceKey<DamageType> damageTypeKey, Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(getType(damageTypeKey, directEntity.level()), directEntity, causingEntity);
    }

    private static Holder<DamageType> getType(ResourceKey<DamageType> key, Level level) {
        return level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
    }

    public static void bootstrap(BootstrapContext<DamageType> bootstrap) {
        bootstrap.register(ORBITING_BLOCK, new DamageType(makeDamageTypeMsgId(ORBITING_BLOCK), 0.1F));
        bootstrap.register(INDIRECT_ORBITING_BLOCK, new DamageType(makeDamageTypeMsgId(INDIRECT_ORBITING_BLOCK), 0.1F));
    }

    private static String makeDamageTypeMsgId(ResourceKey<DamageType> key) {
        return key.identifier().toString().replace(':', '.');
    }
}
