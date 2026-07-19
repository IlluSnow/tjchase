package illusnow.tjchase.mixin.client;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.client.DanceEffectTypeOperator;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void extractDanceEffectType(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        DancingHelper.DanceEffectType danceEffectType = entity.getData(ModAttachments.DANCE_EFFECT_TYPE.get());
        ((DanceEffectTypeOperator) reusedState).tjChase$setDanceEffectType(danceEffectType);
    }
}
