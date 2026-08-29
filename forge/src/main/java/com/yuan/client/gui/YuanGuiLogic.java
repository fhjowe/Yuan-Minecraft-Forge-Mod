package com.yuan.client.gui;

public final class YuanGuiLogic {
    private YuanGuiLogic() {}

    public static int clampScroll(int offset, int max) {
        return Math.max(0, Math.min(max, offset));
    }

    public static boolean shouldHandleModeScroll(boolean screenOpen, double delta) {
        return !screenOpen && delta != 0;
    }
}
