package com.armilp.ezweight.config;

import com.armilp.ezweight.util.DebugLog;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class WeightConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        final Pair<Client, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = pair.getLeft();
        CLIENT_SPEC = pair.getRight();
    }

    public static class Common {
        public final ModConfigSpec.DoubleValue BASE_WEIGHT;
        public final ModConfigSpec.DoubleValue MAX_WEIGHT;

        public final ModConfigSpec.DoubleValue LIGHT_ENCUMBERED_FORCE;
        public final ModConfigSpec.DoubleValue ENCUMBERED_FORCE;
        public final ModConfigSpec.DoubleValue HEAVILY_ENCUMBERED_FORCE;
        public final ModConfigSpec.DoubleValue OVERBURDENED_FORCE;
        public final ModConfigSpec.DoubleValue CRUSHED_FORCE;

        public final ModConfigSpec.BooleanValue USE_DYNAMIC_WEIGHT;
        public final ModConfigSpec.BooleanValue DYN_FOOD_ENABLED;
        public final ModConfigSpec.DoubleValue DYN_FOOD_INFLUENCE_MULTIPLIER;
        public final ModConfigSpec.BooleanValue DYN_STRENGTH_ENABLED;
        public final ModConfigSpec.DoubleValue DYN_STRENGTH_BONUS;
        public final ModConfigSpec.BooleanValue DYN_CROUCH_ENABLED;
        public final ModConfigSpec.DoubleValue DYN_CROUCH_BONUS;
        public final ModConfigSpec.BooleanValue DYN_ARMOR_PENALTY_ENABLED;
        public final ModConfigSpec.DoubleValue DYN_ARMOR_PENALTY_PER_PIECE;
        public final ModConfigSpec.BooleanValue WEIGHT_DEBUG_MESSAGE;

        public final ModConfigSpec.BooleanValue NO_JUMP_WEIGHT_ENABLED;
        public final ModConfigSpec.ConfigValue<List<? extends String>> NO_JUMP_WEIGHT_RANGES;
        public final ModConfigSpec.BooleanValue DAMAGE_OVERWEIGHT_ENABLED;
        public final ModConfigSpec.ConfigValue<List<? extends String>> DAMAGE_OVERWEIGHT_THRESHOLDS;
        public final ModConfigSpec.DoubleValue DAMAGE_PER_SECOND;

        public final ModConfigSpec.BooleanValue ATTACHMENT_WEIGHT_ENABLED;
        public final ModConfigSpec.DoubleValue ATTACHMENT_WEIGHT_MULTIPLIER;
        public final ModConfigSpec.BooleanValue AMMO_WEIGHT_ENABLED;
        public final ModConfigSpec.DoubleValue AMMO_WEIGHT_PER_ROUND;
        public final ModConfigSpec.DoubleValue AMMO_WEIGHT_MULTIPLIER;

        public final ModConfigSpec.BooleanValue ITEM_TOOLTIP_ENABLED;
        public final ModConfigSpec.ConfigValue<String> ITEM_TOOLTIP_COLOR_BASE;
        public final ModConfigSpec.ConfigValue<String> ITEM_TOOLTIP_COLOR_TOTAL;

        public final ModConfigSpec.BooleanValue BACKPACK_WEIGHT_REDUCTION_ENABLED;
        public final ModConfigSpec.DoubleValue BACKPACK_WEIGHT_REDUCTION_DEFAULT;
        public final ModConfigSpec.BooleanValue BACKPACK_SHOW_TOOLTIP;
        public final ModConfigSpec.ConfigValue<String> BACKPACK_TOOLTIP_COLOR;
        public final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_BACKPACK_ITEMS;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("General");
            WEIGHT_DEBUG_MESSAGE = builder.comment("Debug message.").define("DEBUG_MESSAGE", DebugLog.isEnabled());

            BASE_WEIGHT = builder
                    .comment("Default player body weight (kilograms).")
                    .defineInRange("base_weight", 60.0, 30.0, 200.0);

            MAX_WEIGHT = builder
                    .comment("The base maximum weight a player can carry (kilograms).")
                    .defineInRange("max_weight", 140.0, 0.0, 1000.0);

            LIGHT_ENCUMBERED_FORCE = builder
                    .comment("Absolute weight threshold for light encumbrance (kilograms).")
                    .defineInRange("light_encumbered_force", 5.0, 0.0, 1000.0);

            ENCUMBERED_FORCE = builder
                    .comment("Absolute weight threshold for encumbrance (kilograms).")
                    .defineInRange("encumbered_force", 15.0, 0.0, 1000.0);

            HEAVILY_ENCUMBERED_FORCE = builder
                    .comment("Absolute weight threshold for heavy encumbrance (kilograms).")
                    .defineInRange("heavily_encumbered_force", 25.0, 0.0, 1000.0);

            OVERBURDENED_FORCE = builder
                    .comment("Absolute weight threshold for overburdened state (kilograms).")
                    .defineInRange("overburdened_force", 65.0, 0.0, 1000.0);

            CRUSHED_FORCE = builder
                    .comment("Absolute weight threshold for crushed state (kilograms).")
                    .defineInRange("crushed_force", 95.0, 0.0, 1000.0);

            USE_DYNAMIC_WEIGHT = builder
                    .comment("If true, uses dynamic weight calculations (base weight, food, strength, etc.). If false, only uses max_weight.")
                    .define("use_dynamic_weight", true);

            ITEM_TOOLTIP_ENABLED = builder
                    .comment("Enable or disable displaying item weight tooltips.")
                    .define("item_tooltip_enabled", true);
            ITEM_TOOLTIP_COLOR_BASE = builder
                    .comment("Color for base weight in item tooltip.")
                    .define("item_tooltip_color_base", "#FFD700");
            ITEM_TOOLTIP_COLOR_TOTAL = builder
                    .comment("Color for total weight in item tooltip.")
                    .define("item_tooltip_color_total", "#FFFF00");
            builder.pop();

            builder.push("DynamicWeight");
            DYN_FOOD_ENABLED = builder
                    .comment("If true, food level affects max carry weight.")
                    .define("food_enabled", true);
            DYN_FOOD_INFLUENCE_MULTIPLIER = builder
                    .comment("Multiplier for food influence (0.0 disables its effect). 1.0 = default.")
                    .defineInRange("food_influence_multiplier", 1.0, 0.0, 5.0);

            DYN_STRENGTH_ENABLED = builder
                    .comment("If true, Strength effect grants a flat bonus.")
                    .define("strength_enabled", true);
            DYN_STRENGTH_BONUS = builder
                    .comment("Flat bonus applied when Strength effect is active.")
                    .defineInRange("strength_bonus", 10.0, -1000.0, 1000.0);

            DYN_CROUCH_ENABLED = builder
                    .comment("If true, crouching grants a flat bonus.")
                    .define("crouch_enabled", true);
            DYN_CROUCH_BONUS = builder
                    .comment("Flat bonus applied while crouching.")
                    .defineInRange("crouch_bonus", 5.0, -1000.0, 1000.0);

            DYN_ARMOR_PENALTY_ENABLED = builder
                    .comment("If true, each worn armor piece applies a penalty.")
                    .define("armor_penalty_enabled", true);
            DYN_ARMOR_PENALTY_PER_PIECE = builder
                    .comment("Penalty per worn armor piece.")
                    .defineInRange("armor_penalty_per_piece", 2.5, -1000.0, 1000.0);
            builder.pop();

            builder.push("NoJump");
            NO_JUMP_WEIGHT_ENABLED = builder
                    .define("no_jump_weight_enabled", true);
            NO_JUMP_WEIGHT_RANGES = builder
                    .comment("Ranges [min, max]; use 'max' for player max weight.")
                    .defineList("no_jump_weight_ranges",
                            Arrays.asList("95", "max"),
                            val -> isValidDoubleOrMax(val));
            builder.pop();

            builder.push("DamageOverweight");
            DAMAGE_OVERWEIGHT_ENABLED = builder
                    .define("damage_overweight_enabled", true);
            DAMAGE_OVERWEIGHT_THRESHOLDS = builder
                    .comment("List of weight percent thresholds (0.0 - 1.0) where damage starts. Example: [\"0.8\"]")
                    .defineList("damage_overweight_thresholds",
                            List.of("0.8"),
                            val -> {
                                if (!(val instanceof String s)) return false;
                                try {
                                    double d = Double.parseDouble(s);
                                    return d >= 0.0 && d <= 1.0;
                                } catch (NumberFormatException e) {
                                    return false;
                                }
                            });

            DAMAGE_PER_SECOND = builder
                    .comment("Damage per second while overweight.")
                    .defineInRange("damage_per_second", 0.5, 0.0, 100.0);
            builder.pop();

            builder.push("TACZGunWeight");
            ATTACHMENT_WEIGHT_ENABLED = builder
                    .comment("If true, includes attachment weight in gun total weight.")
                    .define("attachment_weight_enabled", true);
            ATTACHMENT_WEIGHT_MULTIPLIER = builder
                    .comment("Multiplier applied to summed attachment weight.")
                    .defineInRange("attachment_weight_multiplier", 1.0, 0.0, 20.0);

            AMMO_WEIGHT_ENABLED = builder
                    .comment("If true, includes ammo weight in gun total weight.")
                    .define("ammo_weight_enabled", true);
            AMMO_WEIGHT_PER_ROUND = builder
                    .comment("Default weight per single bullet/round.")
                    .defineInRange("ammo_weight_per_round", 0.02, 0.0, 1.0);
            AMMO_WEIGHT_MULTIPLIER = builder
                    .comment("Multiplier applied to computed ammo weight (rounds * per_round).")
                    .defineInRange("ammo_weight_multiplier", 1.0, 0.0, 20.0);
            builder.pop();

            builder.push("BackpackWeight");
            BACKPACK_WEIGHT_REDUCTION_ENABLED = builder
                    .comment("If true, backpacks reduce the weight of their contents.")
                    .define("backpack_weight_reduction_enabled", true);
            BACKPACK_WEIGHT_REDUCTION_DEFAULT = builder
                    .comment("Default weight reduction for backpacks (0.20 = 20% reduction).")
                    .defineInRange("backpack_weight_reduction_default", 0.20, 0.0, 1.0);
            BACKPACK_SHOW_TOOLTIP = builder
                    .comment("If true, shows weight reduction percentage in backpack tooltip.")
                    .define("backpack_show_tooltip", true);
            BACKPACK_TOOLTIP_COLOR = builder
                    .comment("Color for backpack weight reduction tooltip.")
                    .define("backpack_tooltip_color", "#00FF00");
            CUSTOM_BACKPACK_ITEMS = builder
                    .comment(
                            "Extra item IDs to treat as backpacks (weight reduction applied to their contents).",
                            "Format: \"namespace:item_id\". Example: \"minecraft:shulker_box\""
                    )
                    .defineList("custom_backpack_items",
                            List.of(),
                            val -> val instanceof String s && ResourceLocation.tryParse(s.trim()) != null);
            builder.pop();
        }

        private static boolean isValidDoubleOrMax(Object val) {
            if (val instanceof String s) {
                if (s.equalsIgnoreCase("max")) return true;
                try {
                    Double.parseDouble(s);
                    return true;
                } catch (NumberFormatException ignored) {
                }
            }
            return false;
        }
    }

    public static double parseWeightValue(String s) {
        if (s.equalsIgnoreCase("max")) {
            return COMMON.MAX_WEIGHT.get();
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static class Client {
        public final ModConfigSpec.EnumValue<InventoryAnchor> MAIN_HUD_ANCHOR;
        public final ModConfigSpec.IntValue MAIN_HUD_OFFSET_X;
        public final ModConfigSpec.IntValue MAIN_HUD_OFFSET_Y;

        public final ModConfigSpec.IntValue MINI_HUD_X;
        public final ModConfigSpec.IntValue MINI_HUD_Y;
        public final ModConfigSpec.IntValue MINI_HUD_ICON_SIZE;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("MainHUD Position");
            MAIN_HUD_ANCHOR = builder
                    .comment("Anchor position of the main weight HUD box. Options: TOP, LEFT, RIGHT, BOTTOM")
                    .defineEnum("mainHudAnchor", InventoryAnchor.LEFT);
            MAIN_HUD_OFFSET_X = builder
                    .comment("X offset from the anchored position.")
                    .defineInRange("mainHudOffsetX", -10, -500, 500);
            MAIN_HUD_OFFSET_Y = builder
                    .comment("Y offset from the anchored position.")
                    .defineInRange("mainHudOffsetY", 0, -500, 500);
            builder.pop();

            builder.push("MiniHUD Positions");
            MINI_HUD_ICON_SIZE = builder
                    .comment("Size (width and height) of the mini-HUD icon in pixels.")
                    .defineInRange("miniHudIconSize", 18, 4, 64);
            MINI_HUD_X = builder
                    .comment("X position of the mini-HUD icon.")
                    .defineInRange("miniHudX", 15, 0, 500);
            MINI_HUD_Y = builder
                    .comment("Y position of the mini-HUD icon.")
                    .defineInRange("miniHudY", 65, 0, 500);
            builder.pop();
        }

        public enum InventoryAnchor {
            TOP, LEFT, RIGHT, BOTTOM
        }
    }
}