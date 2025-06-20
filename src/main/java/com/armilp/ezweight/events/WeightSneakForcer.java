package com.armilp.ezweight.events;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.commands.WeightCommands;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public class WeightSneakForcer {

    private static final String SNEAK_TICK_COUNTER_TAG = "ezweight_sneak_tick_counter";
    private static final String LAST_X = "ezweight_last_x";
    private static final String LAST_Y = "ezweight_last_y";
    private static final String LAST_Z = "ezweight_last_z";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        if (!WeightCommands.isWeightEnabledFor(player)) return;

        double weight = PlayerWeightHandler.getTotalWeight(player);
        CompoundTag data = player.getPersistentData();

        Vec3 currentPos = player.position();

        if (shouldForceSneak(weight)) {
            if (!player.isShiftKeyDown()) {

                double lastX = data.getDouble(LAST_X);
                double lastY = data.getDouble(LAST_Y);
                double lastZ = data.getDouble(LAST_Z);
                Vec3 lastPos = new Vec3(lastX, lastY, lastZ);

                if (currentPos.distanceToSqr(lastPos) > 0.0001) {
                    player.teleportTo(lastPos.x, lastPos.y, lastPos.z);
                    player.setDeltaMovement(Vec3.ZERO);
                }

                player.getAbilities().mayBuild = false;
                player.onUpdateAbilities();

                int counter = data.getInt(SNEAK_TICK_COUNTER_TAG);
                if (++counter >= 40) {
                    player.displayClientMessage(Component.translatable("message.ezweight.too_heavy").withStyle(ChatFormatting.RED), true);
                    counter = 0;
                }
                data.putInt(SNEAK_TICK_COUNTER_TAG, counter);

            } else {
                player.getAbilities().mayBuild = true;
                player.onUpdateAbilities();
                data.putInt(SNEAK_TICK_COUNTER_TAG, 0);
            }
        } else {
            // Peso bajo el umbral
            player.getAbilities().mayBuild = true;
            player.onUpdateAbilities();
            data.putInt(SNEAK_TICK_COUNTER_TAG, 0);
        }

        data.putDouble(LAST_X, currentPos.x);
        data.putDouble(LAST_Y, currentPos.y);
        data.putDouble(LAST_Z, currentPos.z);
    }

    private static boolean shouldForceSneak(double weight) {
        if (!WeightConfig.COMMON.FORCE_SNEAK_ENABLED.get()) return false;

        List<? extends String> ranges = WeightConfig.COMMON.FORCE_SNEAK_WEIGHT_RANGES.get();
        for (int i = 0; i < ranges.size() - 1; i += 2) {
            double min = WeightConfig.parseWeightValue(ranges.get(i));
            double max = WeightConfig.parseWeightValue(ranges.get(i + 1));

            if (weight >= min && weight <= max) return true;
        }
        return false;
    }
}
