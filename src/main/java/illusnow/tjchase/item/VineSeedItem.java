package illusnow.tjchase.item;

import illusnow.tjchase.entity.VineSeed;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;

public class VineSeedItem extends Item implements ProjectileItem {
    private static final float PROJECTILE_SHOOT_POWER = 1.5F;
    private static final int COOLDOWN_TICKS = 60;

    public VineSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSoundEvents.VINE_SEED_THROW.get(),
                SoundSource.NEUTRAL,
                0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (level instanceof ServerLevel serverlevel) {
            Projectile.spawnProjectileFromRotation(VineSeed::new, serverlevel, stack, player, 0.0F, PROJECTILE_SHOOT_POWER, 1.0F);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {
        return new VineSeed(level, position.x(), position.y(), position.z(), stack);
    }
}
