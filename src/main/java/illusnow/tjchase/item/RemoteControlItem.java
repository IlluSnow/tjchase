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

package illusnow.tjchase.item;

import com.google.common.base.Suppliers;
import illusnow.tjchase.client.renderer.item.RemoteControlItemRenderer;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RemoteControlItem extends Item implements GeoItem {
    private static final String CONTROLLER_NAME = "TriggerableAnimations";
    private static final String PRESS_ANIM_NAME = "Press";
    private static final RawAnimation PRESS = RawAnimation.begin().thenPlay("press");
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public RemoteControlItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(CONTROLLER_NAME, test -> PlayState.STOP)
                .triggerableAnim(PRESS_ANIM_NAME, PRESS));
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<RemoteControlItem>> renderer = Suppliers.memoize(RemoteControlItemRenderer::new);

            @Override
            public GeoItemRenderer<RemoteControlItem> getGeoItemRenderer() {
                return renderer.get();
            }
        });
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            playSoundAndAnimation(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.level().isClientSide() && interactionTarget instanceof Controllable controllableInteractionTarget) {
            playSoundAndAnimation(player.level(), player, usedHand);
            boolean controlled = Controllable.control(player, controllableInteractionTarget);
            if (controlled) {
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    private void playSoundAndAnimation(Level level, Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        long instanceId = GeoItem.getOrAssignId(itemInHand, (ServerLevel) level);
        triggerAnim(player, instanceId, CONTROLLER_NAME, PRESS_ANIM_NAME);
        if (!player.isSilent()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.REMOTE_CONTROL_PRESS, SoundSource.PLAYERS, 0.35F, player.getRandom().triangle(1, 0.1F));
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
