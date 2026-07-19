package illusnow.tjchase.util;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class AngelTomPassive2Tracker {
    public static final MapCodec<AngelTomPassive2Tracker> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.LONG.fieldOf("interval").forGetter(AngelTomPassive2Tracker::getInterval),
                    Codec.INT.fieldOf("healCount").forGetter(AngelTomPassive2Tracker::getHealCount),
                    Codec.FLOAT.fieldOf("healAmount").forGetter(AngelTomPassive2Tracker::getHealAmount),
                    Codec.LONG.fieldOf("healTick").forGetter(tracker -> tracker.healTick),
                    Codec.BOOL.fieldOf("onceTriggered").forGetter(tracker -> tracker.onceTriggered)
            ).apply(instance, AngelTomPassive2Tracker::new)
    );
    public static final Codec<AngelTomPassive2Tracker> CODEC = MAP_CODEC.codec();
    // Currently unused
    public static final StreamCodec<ByteBuf, AngelTomPassive2Tracker> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, AngelTomPassive2Tracker::getInterval,
            ByteBufCodecs.INT, AngelTomPassive2Tracker::getHealCount,
            ByteBufCodecs.FLOAT, AngelTomPassive2Tracker::getHealAmount,
            ByteBufCodecs.LONG, tracker -> tracker.healTick,
            ByteBufCodecs.BOOL, tracker -> tracker.onceTriggered,
            AngelTomPassive2Tracker::new
    );
    public static final long DEFAULT_INTERVAL = 20;
    public static final int DEFAULT_HEAL_COUNT = 3;
    public static final float DEFAULT_HEAL_AMOUNT = 2;
    private long interval;
    private int healCount;
    private float healAmount;
    private long healTick;
    private boolean onceTriggered;

    public AngelTomPassive2Tracker(long interval, int healCount, float healAmount) {
        this(interval, healCount, healAmount, 0, false);
    }

    public AngelTomPassive2Tracker(long interval, int healCount, float healAmount, long healTick, boolean onceTriggered) {
        this.interval = interval;
        this.healCount = healCount;
        this.healAmount = healAmount;
        this.healTick = healTick;
        this.onceTriggered = onceTriggered;
    }

    public static Optional<AngelTomPassive2Tracker> load(String key, ValueInput input) {
        return input.read(key, CODEC);
    }

    public static AngelTomPassive2Tracker createDefault() {
        return new AngelTomPassive2Tracker(DEFAULT_INTERVAL, DEFAULT_HEAL_COUNT, DEFAULT_HEAL_AMOUNT);
    }

    public void resetAttributes(long interval, int healCount, float healAmount) {
        this.interval = interval;
        this.healCount = healCount;
        this.healAmount = healAmount;
    }

    public void triggerPassive2(LivingEntity entity) {
        healTick = entity.level().getGameTime();
        Utils.clearNegativeEffectsAndFire(entity);
        onceTriggered = true;
    }

    public boolean hasBuff(LivingEntity entity) {
        int delta = getDelta(entity);
        return delta / interval <= healCount;
    }

    private boolean canStillHeal(LivingEntity entity) {
        int delta = getDelta(entity);
        return delta != 0 && delta % interval == 0 && delta / interval <= healCount;
    }

    private int getDelta(LivingEntity entity) {
        return Ints.saturatedCast(entity.level().getGameTime() - healTick);
    }

    public void update(LivingEntity entity) {
        if (onceTriggered) {
            if (canStillHeal(entity)) {
                entity.heal(healAmount);
            }
            if (hasBuff(entity)) {
                Utils.sendTJChaseBuffParticles(entity, 1, 1, 0);
            }
        }
    }

    public void save(String key, ValueOutput output) {
        output.store(key, CODEC, this);
    }

    public long getInterval() {
        return interval;
    }

    public int getHealCount() {
        return healCount;
    }

    public float getHealAmount() {
        return healAmount;
    }
}
