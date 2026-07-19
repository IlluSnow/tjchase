package illusnow.tjchase.network;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.Zuri;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record PlayDanceTimePayload(int zuriId) implements CustomPacketPayload {
    public static final Type<PlayDanceTimePayload> TYPE = new Type<>(TJChase.prefix("play_dance_time"));
    public static final StreamCodec<ByteBuf, PlayDanceTimePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlayDanceTimePayload::zuriId,
            PlayDanceTimePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Nullable
    public Zuri getZuri(Level level) {
        return level.getEntity(zuriId) instanceof Zuri zuri ? zuri : null;
    }
}
