package com.armilp.ezweight.util;

public final class DebugLog {

    private static volatile boolean enabled = false;

    private DebugLog() {
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void log(String message) {
        if (enabled) {
            System.out.println("[EzWeight] " + message);
        }
    }

    public static void log(String format, Object... args) {
        if (enabled) {
            System.out.println("[EzWeight] " + String.format(format, args));
        }
    }
}