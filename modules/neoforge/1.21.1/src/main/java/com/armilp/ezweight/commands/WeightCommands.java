package com.armilp.ezweight.commands;

import com.armilp.ezweight.config.WeightConfig;
import com.armilp.ezweight.data.ItemWeightRegistry;
import com.armilp.ezweight.network.EZWeightNetwork;
import com.armilp.ezweight.network.gui.OpenWeightGuiPacket;
import com.armilp.ezweight.player.PlayerMaxWeightOverride;
import com.armilp.ezweight.player.PlayerWeightHandler;
import com.armilp.ezweight.util.BackpackIdUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class WeightCommands {

    private static final Set<UUID> disabledPlayers = new HashSet<>();
    private static boolean allDisabled = false;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ezweight")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("gui")
                                .executes(context -> {
                                    CommandSourceStack src = context.getSource();
                                    ServerPlayer player = src.getPlayerOrException();
                                    EZWeightNetwork.sendToPlayer(new OpenWeightGuiPacket(), player);
                                    src.sendSuccess(() -> Component.translatable("message.ezweight.gui_open"), true);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("backpack")
                                .then(Commands.argument("reduction%", DoubleArgumentType.doubleArg(0.0, 100.0))
                                        .then(Commands.argument("max_weight", DoubleArgumentType.doubleArg(0.0))
                                                .executes(context -> {
                                                    CommandSourceStack src = context.getSource();
                                                    ServerPlayer player = src.getPlayerOrException();

                                                    ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);

                                                    if (!BackpackIdUtils.isBackpackItem(held)) {
                                                        src.sendFailure(Component.translatable("message.ezweight.not_a_backpack"));
                                                        return 0;
                                                    }

                                                    double reductionPercent = DoubleArgumentType.getDouble(context, "reduction%");
                                                    double maxWeight = DoubleArgumentType.getDouble(context, "max_weight");
                                                    double reductionDecimal = reductionPercent / 100.0;

                                                    String uuid = BackpackIdUtils.getOrCreateInstanceUUID(held);
                                                    ResourceLocation instanceKey = BackpackIdUtils.uuidToResourceLocation(uuid);

                                                    ItemWeightRegistry.setBackpackWeightReduction(instanceKey, reductionDecimal);
                                                    ItemWeightRegistry.setBackpackMaxWeight(instanceKey, maxWeight);
                                                    ItemWeightRegistry.saveToFile(ItemWeightRegistry.getConfigFile());

                                                    BackpackIdUtils.setInstanceReduction(held, reductionDecimal);
                                                    BackpackIdUtils.setInstanceMaxWeight(held, maxWeight);

                                                    String name = held.getHoverName().getString();
                                                    src.sendSuccess(() -> Component.translatable(
                                                            "message.ezweight.backpack_updated",
                                                            name,
                                                            String.format("%.0f%%", reductionPercent),
                                                            String.format("%.1f kg", maxWeight)
                                                    ), true);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("setmaxweight")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .then(Commands.argument("weight", DoubleArgumentType.doubleArg(0.0))
                                                .executes(context -> {
                                                    CommandSourceStack src = context.getSource();
                                                    String playerName = StringArgumentType.getString(context, "player");
                                                    ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(playerName);

                                                    if (target == null) {
                                                        src.sendFailure(Component.translatable("message.ezweight.player_not_found", playerName));
                                                        return 0;
                                                    }

                                                    double newMax = DoubleArgumentType.getDouble(context, "weight");
                                                    PlayerMaxWeightOverride.set(target.getUUID(), newMax);

                                                    src.sendSuccess(() -> Component.translatable(
                                                            "message.ezweight.setmaxweight_success",
                                                            playerName,
                                                            String.format("%.1f kg", newMax)
                                                    ), true);

                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )
//                        .then(Commands.literal("resetmaxweight")
//                                .then(Commands.argument("player", StringArgumentType.word())
//                                        .executes(context -> {
//                                            CommandSourceStack src = context.getSource();
//                                            String playerName = StringArgumentType.getString(context, "player");
//                                            ServerPlayer target = src.getServer().getPlayerList().getPlayerByName(playerName);
//
//                                            if (target == null) {
//                                                src.sendFailure(Component.translatable("message.ezweight.player_not_found", playerName));
//                                                return 0;
//                                            }
//
//                                            PlayerMaxWeightOverride.clear(target.getUUID());
//
//                                            src.sendSuccess(() -> Component.translatable(
//                                                    "message.ezweight.resetmaxweight_success",
//                                                    playerName
//                                            ), true);
//
//                                            return Command.SINGLE_SUCCESS;
//                                        })
//                                )
//                        )
                        .then(Commands.literal("toggle")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(context -> {
                                            CommandSourceStack src = context.getSource();
                                            String playerName = StringArgumentType.getString(context, "player");
                                            ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(playerName);
                                            if (player == null) {
                                                src.sendFailure(Component.translatable("message.ezweight.player_not_found", playerName));
                                                return 0;
                                            }
                                            UUID uuid = player.getUUID();
                                            if (disabledPlayers.contains(uuid)) {
                                                disabledPlayers.remove(uuid);
                                                src.sendSuccess(() -> Component.translatable("message.ezweight.toggle_enabled", playerName), true);
                                            } else {
                                                disabledPlayers.add(uuid);
                                                src.sendSuccess(() -> Component.translatable("message.ezweight.toggle_disabled", playerName), true);
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("toggleall")
                                .executes(context -> {
                                    CommandSourceStack src = context.getSource();
                                    allDisabled = !allDisabled;
                                    src.sendSuccess(() -> Component.translatable(allDisabled ?
                                            "message.ezweight.toggle_all_disabled" :
                                            "message.ezweight.toggle_all_enabled"), true);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("info")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .executes(context -> {
                                            CommandSourceStack src = context.getSource();
                                            String playerName = StringArgumentType.getString(context, "player");
                                            ServerPlayer player = src.getServer().getPlayerList().getPlayerByName(playerName);
                                            if (player == null) {
                                                src.sendFailure(Component.translatable("message.ezweight.player_not_found", playerName));
                                                return 0;
                                            }
                                            double weight = PlayerWeightHandler.getTotalWeight(player);
                                            src.sendSuccess(() -> Component.translatable("message.ezweight.player_info",
                                                    playerName,
                                                    String.format("%.1f", weight),
                                                    String.format("%.1f", WeightConfig.COMMON.MAX_WEIGHT.get())
                                            ), true);
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }

    public static boolean isWeightEnabledFor(ServerPlayer player) {
        if (!WeightConfig.COMMON.NO_JUMP_WEIGHT_ENABLED.get()
                && !WeightConfig.COMMON.DAMAGE_OVERWEIGHT_ENABLED.get()) {
            return false;
        }
        if (allDisabled) return false;
        return !disabledPlayers.contains(player.getUUID());
    }
}