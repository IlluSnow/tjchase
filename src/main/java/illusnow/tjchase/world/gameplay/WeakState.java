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

package illusnow.tjchase.world.gameplay;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.network.s2c.UpdateWeakStatusPayload;
import illusnow.tjchase.util.Utils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class WeakState {
    public static final MapCodec<WeakState> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("recover_ticks").forGetter(WeakState::getRecoverTicks),
            Identifier.CODEC.fieldOf("weak_anim_still").forGetter(WeakState::getWeakAnimStill),
            Identifier.CODEC.fieldOf("weak_anim_moving").forGetter(WeakState::getWeakAnimMoving),
            Codec.BOOL.fieldOf("stuns_after_weak").forGetter(WeakState::stunsAfterWeak)
    ).apply(instance, WeakState::new));
    public static final Codec<WeakState> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<RegistryFriendlyByteBuf, WeakState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WeakState::getRecoverTicks,
            Identifier.STREAM_CODEC, WeakState::getWeakAnimStill,
            Identifier.STREAM_CODEC, WeakState::getWeakAnimMoving,
            ByteBufCodecs.BOOL, WeakState::stunsAfterWeak,
            WeakState::new
    );
    public static final int DEFAULT_RECOVER_TICKS = 200;
    public static final Identifier DEFAULT_WEAK_ANIM_STILL = ModPlayerAnimationIDs.CRAWL_STILL;
    public static final Identifier DEFAULT_WEAK_ANIM_MOVING = ModPlayerAnimationIDs.CRAWL_MOVING;
    public static final float WEAK_THRESHOLD_HEALTH = 1E-5F;
    public static final EntityDimensions WEAK_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F);
    public static final Identifier WEAK_SPEED_REDUCTION_MODIFIER_ID = TJChase.prefix("weak_speed_reduction");
    public static final Identifier WEAK_INTERACTION_RANGE_MODIFIER_ID = TJChase.prefix("interaction_range_reduction");
    public static final AttributeModifier WEAK_SPEED_REDUCTION_MODIFIER = new AttributeModifier(WEAK_SPEED_REDUCTION_MODIFIER_ID, -0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final AttributeModifier WEAK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(WEAK_INTERACTION_RANGE_MODIFIER_ID, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private int recoverTicks;
    private Identifier weakAnimStill;
    private Identifier weakAnimMoving;
    private boolean stunsAfterWeak;

    public WeakState() {
        this(0, DEFAULT_WEAK_ANIM_STILL, DEFAULT_WEAK_ANIM_MOVING, false);
    }

    public WeakState(int recoverTicks, Identifier weakAnimStill, Identifier weakAnimMoving, boolean stunsAfterWeak) {
        this.recoverTicks = recoverTicks;
        this.weakAnimStill = weakAnimStill;
        this.weakAnimMoving = weakAnimMoving;
        this.stunsAfterWeak = stunsAfterWeak;
    }

    public static boolean weakInsteadOfDie(Player player) {
        return true;
    }

    public boolean isWeak() {
        return recoverTicks > 0;
    }

    public void setWeak(Player player, int recoverTicks) {
        Preconditions.checkArgument(recoverTicks > 0, "Weak duration must be positive");
        if (isWeak()) {
            return;
        }
        this.recoverTicks = recoverTicks;
        startBeingWeak(player);
    }

    public void recoverFromWeak(Player player) {
        this.recoverTicks = 0;
        stopBeingWeak(player);
    }

    public int getRecoverTicks() {
        return recoverTicks;
    }

    public boolean serverTick(Player player) {
        if (recoverTicks > 0) {
            recoverTicks--;
            if (recoverTicks == 0) {
                stopBeingWeak(player);
            }
            if (player.isSpectator() || player.isCreative()) {
                recoverFromWeak(player);
            }
            return true;
        }
        return false;
    }

    private void startBeingWeak(Player player) {
        Utils.checkAndGetAttribute(player, Attributes.MOVEMENT_SPEED).addPermanentModifier(WEAK_SPEED_REDUCTION_MODIFIER);
        Utils.checkAndGetAttribute(player, Attributes.ENTITY_INTERACTION_RANGE).addPermanentModifier(WEAK_INTERACTION_RANGE_MODIFIER);
        Utils.checkAndGetAttribute(player, Attributes.BLOCK_INTERACTION_RANGE).addPermanentModifier(WEAK_INTERACTION_RANGE_MODIFIER);
        player.syncData(ModAttachments.WEAK_STATE);
        PacketDistributor.sendToAllPlayers(new UpdateWeakStatusPayload(player.getUUID(), true));
        player.refreshDimensions();
    }

    private void stopBeingWeak(Player player) {
        Utils.checkAndGetAttribute(player, Attributes.MOVEMENT_SPEED).removeModifier(WEAK_SPEED_REDUCTION_MODIFIER);
        Utils.checkAndGetAttribute(player, Attributes.ENTITY_INTERACTION_RANGE).removeModifier(WEAK_INTERACTION_RANGE_MODIFIER);
        Utils.checkAndGetAttribute(player, Attributes.BLOCK_INTERACTION_RANGE).removeModifier(WEAK_INTERACTION_RANGE_MODIFIER);
        player.syncData(ModAttachments.WEAK_STATE);
        PacketDistributor.sendToAllPlayers(new UpdateWeakStatusPayload(player.getUUID(), false));
        player.refreshDimensions();
    }

    public Identifier getWeakAnimStill() {
        return weakAnimStill;
    }

    public Identifier getWeakAnimMoving() {
        return weakAnimMoving;
    }

    public void setWeakAnim(Identifier weakAnimStill, Identifier weakAnimMoving) {
        this.weakAnimStill = weakAnimStill;
        this.weakAnimMoving = weakAnimMoving;
    }

    public boolean stunsAfterWeak() {
        return stunsAfterWeak;
    }

    public void setStunsAfterWeak(boolean stunsAfterWeak) {
        this.stunsAfterWeak = stunsAfterWeak;
    }
}
