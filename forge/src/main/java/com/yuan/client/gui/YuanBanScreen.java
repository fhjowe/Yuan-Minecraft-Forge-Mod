package com.yuan.client.gui;

import com.yuan.Yuan;
import com.yuan.data.YuanBanClientState;
import com.yuan.network.BanListRequestPacket;
import com.yuan.network.BanRemovePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YuanBanScreen extends Screen {
    private final Screen parent;
    private int page;
    private int revision = -1;

    public YuanBanScreen(Screen parent) {
        super(Component.literal("虚渊 · 禁止重生管理"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildEntries();
        Yuan.CHANNEL.sendToServer(new BanListRequestPacket());
    }

    private void rebuildEntries() {
        revision = YuanBanClientState.revision();
        clearWidgets();
        List<Entry> entries = entries();
        int pages = Math.max(1, (entries.size() + 7) / 8);
        page = Math.max(0, Math.min(page, pages - 1));
        int start = page * 8;
        int x = width / 2 - 150;
        for (int i = 0; i < 8 && start + i < entries.size(); i++) {
            Entry entry = entries.get(start + i);
            int y = 45 + i * 24;
            addRenderableWidget(Button.builder(Component.literal(
                    (entry.persistent ? "[持久] " : "[会话] ") + entry.id),
                    button -> {
                        Yuan.CHANNEL.sendToServer(new BanRemovePacket(entry.id));
                    }).bounds(x, y, 300, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("上一页"), b -> { page--; rebuildEntries(); })
                .bounds(width / 2 - 150, height - 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("刷新"), b -> Yuan.CHANNEL.sendToServer(new BanListRequestPacket()))
                .bounds(width / 2 - 85, height - 28, 50, 20).build());
        addRenderableWidget(Button.builder(Component.literal("全部解除"), b -> Yuan.CHANNEL.sendToServer(BanRemovePacket.all()))
                .bounds(width / 2 - 30, height - 28, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("下一页"), b -> { page++; rebuildEntries(); })
                .bounds(width / 2 + 45, height - 28, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("返回"), b -> onClose())
                .bounds(width / 2 + 110, height - 28, 40, 20).build());
    }

    private static List<Entry> entries() {
        List<Entry> result = new ArrayList<>();
        for (UUID id : YuanBanClientState.session()) result.add(new Entry(id, false));
        for (UUID id : YuanBanClientState.persistent()) result.add(new Entry(id, true));
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        if (revision != YuanBanClientState.revision()) rebuildEntries();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFD700);
        graphics.drawCenteredString(font, "点击条目解除，需管理员权限", width / 2, 28, 0xAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public void onClose() { Minecraft.getInstance().setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }

    private record Entry(UUID id, boolean persistent) {}
}
