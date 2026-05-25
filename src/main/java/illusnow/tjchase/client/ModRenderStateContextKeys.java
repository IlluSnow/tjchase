package illusnow.tjchase.client;

import illusnow.tjchase.TJChase;
import net.minecraft.util.context.ContextKey;

public final class ModRenderStateContextKeys {
    public static final ContextKey<Float> MAX_PLAY_HARP_DURATION = new ContextKey<>(TJChase.prefix("max_play_harp_duration"));
    public static final ContextKey<Float> PARTIAL_TICKS = new ContextKey<>(TJChase.prefix("partial_ticks"));

    private ModRenderStateContextKeys() {}
}
