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

package illusnow.tjchase.world;

import illusnow.tjchase.TJChase;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ModDamageSources {
    public static final ResourceKey<DamageType> MOB_ATTACK_NO_SCALING = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("mob_attack_no_scaling"));
    public static final ResourceKey<DamageType> ORBITING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("orbiting_block"));
    public static final ResourceKey<DamageType> INDIRECT_ORBITING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("indirect_orbiting_block"));
    public static final ResourceKey<DamageType> YOGA_BALL = ResourceKey.create(Registries.DAMAGE_TYPE, TJChase.prefix("yoga_ball"));

    public static DamageSource mobAttackNoScaling(Entity attacker) {
        return source(MOB_ATTACK_NO_SCALING, attacker, attacker);
    }

    public static DamageSource orbitingBlock(Entity orbitingBlock) {
        return source(ORBITING_BLOCK, orbitingBlock, null);
    }

    public static DamageSource indirectOrbitingBlock(Entity orbitingBlock, @Nullable Entity thrower) {
        return source(INDIRECT_ORBITING_BLOCK, orbitingBlock, thrower);
    }

    public static DamageSource yogaBall(Entity yogaBall, @Nullable Entity thrower) {
        return source(YOGA_BALL, yogaBall, thrower);
    }

    public static DamageSource source(ResourceKey<DamageType> damageTypeKey, Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(getType(damageTypeKey, directEntity.level()), directEntity, causingEntity);
    }

    private static Holder<DamageType> getType(ResourceKey<DamageType> key, Level level) {
        return level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key);
    }

    public static void bootstrap(BootstrapContext<DamageType> bootstrap) {
        bootstrap.register(MOB_ATTACK_NO_SCALING, new DamageType(makeDamageTypeMsgId(MOB_ATTACK_NO_SCALING), DamageScaling.NEVER, 0.1F));
        bootstrap.register(ORBITING_BLOCK, new DamageType(makeDamageTypeMsgId(ORBITING_BLOCK), 0.1F));
        bootstrap.register(INDIRECT_ORBITING_BLOCK, new DamageType(makeDamageTypeMsgId(INDIRECT_ORBITING_BLOCK), 0.1F));
        bootstrap.register(YOGA_BALL, new DamageType(makeDamageTypeMsgId(YOGA_BALL), DamageScaling.NEVER, 0.1F));
    }

    private static String makeDamageTypeMsgId(ResourceKey<DamageType> key) {
        return key.identifier().toString().replace(':', '.');
    }
}
