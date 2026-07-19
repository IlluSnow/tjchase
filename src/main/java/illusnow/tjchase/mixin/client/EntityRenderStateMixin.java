package illusnow.tjchase.mixin.client;

import illusnow.tjchase.client.DanceEffectTypeOperator;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements DanceEffectTypeOperator {
    @Unique
    private DancingHelper.DanceEffectType tjChase$danceEffectType = DancingHelper.DanceEffectType.NONE;

    @Unique
    @Override
    public DancingHelper.DanceEffectType tjChase$getDanceEffectType() {
        return tjChase$danceEffectType;
    }

    @Unique
    @Override
    public void tjChase$setDanceEffectType(DancingHelper.DanceEffectType tjChase$danceEffectType) {
        this.tjChase$danceEffectType = tjChase$danceEffectType;
    }
}
