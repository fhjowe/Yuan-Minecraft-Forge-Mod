package com.yuan.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class YuanConfigAckClientHandler {
    private YuanConfigAckClientHandler() {}

    public static void handle(long requestId, boolean success, CompoundTag config, int corrections, String message) {
        if (Minecraft.getInstance().screen instanceof YuanConfigScreen screen)
            screen.handleSaveAcknowledgement(requestId, success, config, corrections, message);
    }
}
