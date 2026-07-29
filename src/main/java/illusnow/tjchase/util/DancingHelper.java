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

package illusnow.tjchase.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.tag.ModEntityTypeTags;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.IntFunction;

public final class DancingHelper {
    private DancingHelper() {}

    public static boolean canDanceWithZuri(Mob mob, Zuri zuri) {
        if (mob instanceof Zuri || mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        return zuri.canActivelyAttack(mob) && zuri.distanceToSqr(mob) <= Zuri.INFLUENCE_RADIUS * Zuri.INFLUENCE_RADIUS;
    }

    public static boolean canContinueToDanceWithZuri(Mob mob, Zuri zuri) {
        if (mob instanceof Zuri || mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        return zuri.canActivelyAttack(mob) && zuri.isAlive() && zuri.isDancing();
    }

    public static DanceEffectType getDanceEffectType(Mob mob) {
        Zuri zuri = getZuriDancingWith(mob).map(ref -> ref.getEntity(mob.level(), Zuri.class)).orElse(null);
        if (zuri == null) {
            return DanceEffectType.NONE;
        }
        return getDanceEffectType(mob, zuri);
    }

    public static DanceEffectType getDanceEffectType(Mob mob, Zuri zuri) {
        if (!canContinueToDanceWithZuri(mob, zuri)) {
            return DanceEffectType.NONE;
        }
        DanceEffectType specifiedDanceEffectType = getSpecifiedDanceEffectType(mob);
        if (specifiedDanceEffectType != null) {
            return specifiedDanceEffectType;
        }
        if (Utils.canFly(mob) || Utils.isAquatic(mob)) {
            return DanceEffectType.PARTIAL;
        }
        return DanceEffectType.FULL;
    }

    private static @Nullable DanceEffectType getSpecifiedDanceEffectType(Mob mob) {
        boolean fullyControllable = mob.getType().is(ModEntityTypeTags.ZURI_FULLY_CONTROLLABLE);
        if (fullyControllable) {
            return DanceEffectType.FULL;
        }
        boolean partiallyControllable = mob.getType().is(ModEntityTypeTags.ZURI_PARTIALLY_CONTROLLABLE);
        if (partiallyControllable) {
            return DanceEffectType.PARTIAL;
        }
        boolean uncontrollable = mob.getType().is(ModEntityTypeTags.ZURI_UNCONTROLLABLE);
        if (uncontrollable) {
            return DanceEffectType.NONE;
        }
        return null;
    }

    public static boolean isAiAffectedByZuri(Mob mob) {
        return getDanceEffectType(mob) == DanceEffectType.FULL;
    }

    public static boolean isTargetingAffectedByZuri(Mob mob) {
        return getDanceEffectType(mob) != DanceEffectType.NONE;
    }

    public static Zuri getZuriDancingWithDirectly(Entity entity) {
        return getZuriDancingWith(entity)
                .map(ref -> EntityReference.get(ref, entity.level(), Zuri.class))
                .orElseThrow();
    }

    public static boolean hasValidZuriDancingWith(Entity entity) {
        return getZuriDancingWith(entity).isPresent();
    }

    public static Optional<EntityReference<Zuri>> getZuriDancingWith(Entity entity) {
        return getDancingData(entity)
                .filter(ref -> {
                    Zuri zuri = EntityReference.get(ref, entity.level(), Zuri.class);
                    return zuri != null && zuri.isAlive() && !zuri.isSpectator();
                });
    }

    public static Optional<EntityReference<Zuri>> getDancingData(Entity entity) {
        return entity.getData(ModAttachments.DANCING_WITH);
    }

    public static void controlDancingMob(Mob victim, Zuri controllerZuri) {
        DanceEffectType type = getDanceEffectType(victim, controllerZuri);
        if (type != DanceEffectType.FULL) {
            return;
        }
        BlockPos dancingPos = controllerZuri.getDancingTargetPos();
        if (dancingPos == null) {
            dancingPos = controllerZuri.getDefaultDancingTargetPos();
        }
        Vec3 moveTo = Vec3.atBottomCenterOf(adaptDancingPos(victim, dancingPos));
        double speed = victim.getAttributeValue(Attributes.MOVEMENT_SPEED);
        if (Utils.noPhysics(victim)) {
            victim.getMoveControl().setWantedPosition(moveTo.x, moveTo.y, moveTo.z, Zuri.DANCE_SPEED / speed);
        } else {
            victim.getNavigation().moveTo(moveTo.x, moveTo.y, moveTo.z, Zuri.DANCE_SPEED / speed);
        }
    }

    @SuppressWarnings("deprecation")
    private static BlockPos adaptDancingPos(Mob victim, BlockPos dancingPos) {
        if (Utils.canFly(victim)) {
            return dancingPos;
        }
        return RandomPos.moveUpOutOfSolid(dancingPos, victim.level().getMaxY(), pos -> victim.level().getBlockState(pos).isSolid());
    }

    public static void clearAttachmentData(Mob mob) {
        mob.setData(ModAttachments.DANCING_WITH.get(), Optional.empty());
        mob.setData(ModAttachments.DANCE_EFFECT_TYPE.get(), DanceEffectType.NONE);
    }

    public enum DanceEffectType implements StringRepresentable {
        NONE(0, "none", null),
        PARTIAL(1, "partial", "textures/misc/zuri_for_dancing_mark_partial.png"),
        FULL(2, "full", "textures/misc/zuri_for_dancing_mark.png");

        public static final Codec<DanceEffectType> CODEC = StringRepresentable.fromEnum(DanceEffectType::values);
        public static final MapCodec<DanceEffectType> MAP_CODEC = CODEC.fieldOf("dance_effect_type");
        private static final IntFunction<DanceEffectType> BY_ID = ByIdMap.continuous(DanceEffectType::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, DanceEffectType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, DanceEffectType::getId);
        private final int id;
        private final String name;
        @Nullable
        private final String textureLocation;

        DanceEffectType(int id, String name, @Nullable String textureLocation) {
            this.id = id;
            this.name = name;
            this.textureLocation = textureLocation;
        }

        public int getId() {
            return id;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Nullable
        public Identifier getTextureLocation() {
            if (textureLocation == null) {
                return null;
            }
            return TJChase.prefix(textureLocation);
        }
    }
}
