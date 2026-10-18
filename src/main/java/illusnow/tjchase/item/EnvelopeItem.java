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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.data.loot.ModLootContextParamSets;
import illusnow.tjchase.data.loot.ModLootTables;
import illusnow.tjchase.network.s2c.OpenEnvelopeScreenPayload;
import illusnow.tjchase.sound.ModSoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnvelopeItem extends Item {
    public static final String ENVELOPE_TITLE = TJChase.prefixGuiMsg(ModItemNames.ENVELOPE + ".title");
    public static final String ENVELOPE_GREETING = TJChase.prefixGuiMsg(ModItemNames.ENVELOPE + ".greeting");
    public static final String ENVELOPE_GREETING_BAD_WEATHER = TJChase.prefixGuiMsg(ModItemNames.ENVELOPE + ".greeting_bad_weather");
    private static final Map<UUID, PendingReward> PENDING_REWARDS = new HashMap<>();

    public EnvelopeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || PENDING_REWARDS.containsKey(player.getUUID())) {
            return InteractionResult.FAIL;
        }

        ItemStack reward = generateLoot(serverPlayer);
        if (showsContent(player)) {
            PENDING_REWARDS.put(player.getUUID(), new PendingReward(reward, hand));
            PacketDistributor.sendToPlayer(serverPlayer, new OpenEnvelopeScreenPayload(new ItemStack(this), reward, hand == InteractionHand.OFF_HAND));
        } else {
            giveReward(serverPlayer, reward);
            stack.shrink(1);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundEvents.ENVELOPE_OPEN.get(), SoundSource.PLAYERS);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Nullable
    public EnvelopeTextKeys getEnvelopeTextKeys() {
        return new EnvelopeTextKeys(ENVELOPE_TITLE, ENVELOPE_GREETING, ENVELOPE_GREETING_BAD_WEATHER);
    }

    protected boolean showsContent(Player player) {
        return !player.isShiftKeyDown();
    }

    public static void claimReward(ServerPlayer player, boolean offHand) {
        PendingReward pending = PENDING_REWARDS.remove(player.getUUID());
        if (pending == null || pending.hand() != (offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND)) {
            return;
        }

        ItemStack envelope = player.getItemInHand(pending.hand());
        if (!(envelope.getItem() instanceof EnvelopeItem)) {
            return;
        }
        envelope.shrink(1);
        giveReward(player, pending.reward().copy());
    }

    public static void clearPendingReward(UUID playerId) {
        PENDING_REWARDS.remove(playerId);
    }

    private static void giveReward(ServerPlayer player, ItemStack reward) {
        if (!reward.isEmpty() && !player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
    }

    protected ItemStack generateLoot(ServerPlayer player) {
        ItemStack reward;
        LootParams params = new LootParams.Builder(player.level())
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .create(ModLootContextParamSets.COMMON_REPLY_LETTER_REWARD);
        LootTable lootTable = player.level().getServer().reloadableRegistries().getLootTable(ModLootTables.COMMON_REPLY_LETTER_REWARD);
        List<ItemStack> loots = lootTable.getRandomItems(params);
        reward = loots.isEmpty() ? ItemStack.EMPTY : loots.getFirst();
        return reward;
    }

    public record EnvelopeTextKeys(String title, String greeting, String greetingBadWeather) {}

    private record PendingReward(ItemStack reward, InteractionHand hand) {}
}
