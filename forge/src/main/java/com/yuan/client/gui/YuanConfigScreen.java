package com.yuan.client.gui;

import com.yuan.Yuan;
import com.yuan.client.render.YuanSwordTooltipRenderer;
import com.yuan.item.YuanConfig;
import com.yuan.item.YuanSwordItem;
import com.yuan.network.ConfigSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.IdentityHashMap;
import net.minecraft.client.gui.components.AbstractWidget;

public class YuanConfigScreen extends Screen {
    private static final int GOLD = 0xFFE1B65A;
    private static final int TEXT = 0xFFE5E0E8;
    private static final int MUTED = 0xFF948A9E;
    private static final int PANEL = 0xE6121018;
    private static final int PANEL_ALT = 0xE61A1620;
    private static final int BORDER = 0xFF3A3042;
    private static final long AUTO_SAVE_DELAY_MS = 250L;

    private final ItemStack stack;
    private final ItemStack previewStack;
    private final YuanConfigHistory history;
    private final YuanClientPreferences preferences;
    private EditBox searchBox;
    private EditBox valueBox;
    private EditBox presetNameBox;
    private String category;
    private String editingKey;
    private String status = "";
    private final YuanScrollState centerScroll = new YuanScrollState();
    private final YuanScrollState rightScroll = new YuanScrollState();
    private String draggingScrollbar = "";
    private int categoryScroll;
    private int presetScroll;
    private long saveDueAt;
    private boolean confirmClose;
    private boolean showRightOverlay;
    private final YuanSaveState saveState = new YuanSaveState();
    private CompoundTag sentDraft = new CompoundTag();
    private final Map<Long, CompoundTag> sentDrafts = new HashMap<>();
    private CompoundTag latestApplied = new CompoundTag();
    private boolean closeAfterSave;
    private boolean preferencesSaved;
    private YuanPresetManager.ImportResult pendingImport;
    private String pendingImportSource = "";
    private boolean showAnimationSettings;
    private String pendingFileAction = "";
    private String draggingKey;
    private Float pendingInputValue;
    private final Map<String, Float> dragStartValues = new HashMap<>();
    private final Map<String, YuanUiAnimation.Track> animations = new HashMap<>();
    private final YuanUiAnimation.Track drawerAnimation = new YuanUiAnimation.Track(0);
    private final YuanUiAnimation.Track modalAnimation = new YuanUiAnimation.Track(0);
    private final YuanUiAnimation.Track listAnimation = new YuanUiAnimation.Track(1);
    private long dangerSweepUntil;
    private long actionSweepUntil;
    private int expectedServerIdentity;
    private String selectedPreset = "";
    private int selectedGlassVisual = -1;
    private int selectedGlassColor = -1;
    private int pointerX;
    private int pointerY;
    private long pressUntil;
    private long lastScrollFrameNanos;

    public YuanConfigScreen(ItemStack stack) {
        super(Component.literal("虚渊 神域控制台"));
        this.stack = stack == null ? ItemStack.EMPTY : stack;
        CompoundTag identityConfig = this.stack.getTagElement("YuanConfig");
        this.expectedServerIdentity = identityConfig == null ? 0 : identityConfig.toString().hashCode();
        this.previewStack = this.stack.copy();
        CompoundTag original = this.stack.getTagElement("YuanConfig");
        this.history = new YuanConfigHistory(original == null ? new CompoundTag() : original.copy());
        this.latestApplied = this.history.original();
        this.preferences = YuanClientPreferences.load();
        this.category = YuanConfigCatalog.categories().contains(preferences.lastCategory)
                ? preferences.lastCategory : "攻击";
        syncPreview();
    }

    @Override
    protected void init() {
        clearWidgets();
        int left = leftWidth();
        searchBox = new EditBox(font, 14, 30, Math.max(90, left - 28), 18, Component.literal("搜索设置"));
        searchBox.setHint(Component.literal("搜索名称、说明或配置键"));
        searchBox.setValue(preferences.search);
        searchBox.setResponder(value -> { preferences.search = value; centerScroll.set(0); });
        addRenderableWidget(searchBox);

        valueBox = new EditBox(font, centerX() + centerWidth() - 94, 34, 76, 18, Component.literal("数值"));
        valueBox.visible = false;
        valueBox.setFilter(value -> value.isEmpty() || value.equals("-") || value.matches("-?\\d*(\\.\\d*)?"));
        valueBox.setResponder(this::applyDirectValue);
        addRenderableWidget(valueBox);

        int presetX = wide() ? rightX() + 12 : width - Math.min(250, width - 32);
        int presetW = wide() ? Math.max(80, rightWidth() - 24) : Math.min(226, width - 56);
        presetNameBox = new EditBox(font, presetX, height - 111, presetW, 18, Component.literal("预设名称"));
        presetNameBox.setHint(Component.literal("预设名称"));
        presetNameBox.setValue(preferences.lastPreset);
        presetNameBox.setResponder(value -> preferences.lastPreset = value);
        presetNameBox.visible = wide() || showRightOverlay;
        addRenderableWidget(presetNameBox);

        addRenderableWidget(Button.builder(Component.literal("取消"), button -> requestClose())
                .bounds(width / 2 - 78, height - 27, 72, 20).build());
        addRenderableWidget(Button.builder(Component.literal(preferences.autoSave ? "自动保存" : "保存更改"), button -> saveNow())
                .bounds(width / 2 + 6, height - 27, 84, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (preferences.autoSave && saveDueAt != 0 && System.currentTimeMillis() >= saveDueAt) {
            if (saveNow()) saveDueAt = 0;
            else saveDueAt = System.currentTimeMillis() + 1000;
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long frameNanos = System.nanoTime();
        if (lastScrollFrameNanos != 0) {
            float ticks = Math.min(2, (frameNanos - lastScrollFrameNanos) / 50_000_000f);
            boolean inertial = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS)
                    && !preferences.reduceMotion;
            centerScroll.update(inertial, ticks);
            rightScroll.update(inertial, ticks);
        }
        lastScrollFrameNanos = frameNanos;
        pointerX = mouseX;
        pointerY = mouseY;
        renderBackground(g);
        renderShell(g);
        super.render(g, mouseX, mouseY, partialTick);
        renderActionSweep(g);
        if (confirmClose) renderCloseConfirmation(g);
        if (pendingImport != null) renderImportPreview(g);
        if (showAnimationSettings) renderAnimationSettings(g);
        if (!pendingFileAction.isEmpty()) renderFileConfirmation(g);
    }

    private void renderShell(GuiGraphics g) {
        g.fill(0, 0, width, 25, 0xF00C0A10);
        g.drawString(font, "虚渊 神域控制台", 14, 9, GOLD);
        String dirty = history.changedKeys().isEmpty() ? "无未保存修改" : history.changedKeys().size() + " 项已修改";
        g.drawString(font, dirty, width - font.width(dirty) - 14, 9, history.isDirty() ? GOLD : MUTED);

        renderLeft(g);
        if (narrow()) renderNarrowCategories(g);
        renderCenter(g);
        if (wide()) renderRight(g);
        else {
            g.fill(width - 91, 29, width - 10, 49, PANEL_ALT);
            g.drawCenteredString(font, "预览与管理", width - 50, 35, GOLD);
            if (showRightOverlay || drawerAnimation.active(now())) renderRightOverlay(g);
        }

        g.fill(0, height - 34, width, height, 0xF00C0A10);
        String mode = preferences.autoSave ? "自动保存: 开" : "自动保存: 关";
        g.drawString(font, mode + "  |  Ctrl+F 搜索  Ctrl+Z/Y 撤销重做  Ctrl+S 保存", 12, height - 22, MUTED);
        if (!status.isEmpty()) {
            float alpha = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.SAVE_STATUS)
                    ? animated("status-alpha", 1, YuanClientPreferences.AnimationCategory.SAVE_STATUS, 120) : 1;
            int color = ((int)(255 * alpha) << 24) | (GOLD & 0xFFFFFF);
            g.drawString(font, status, width - font.width(status) - 12, height - 22, color);
        }
    }

    private void renderLeft(GuiGraphics g) {
        if (narrow()) return;
        int left = leftWidth();
        panel(g, 8, 27, left, height - 38);
        int y = 57;
        nav(g, "收藏", y, "收藏".equals(category), preferences.favorites.size()); y += 22;
        nav(g, "已修改", y, "已修改".equals(category), history.changedKeys().size()); y += 26;
        for (String name : YuanConfigCatalog.categories()) {
            nav(g, name, y, name.equals(category), countCategory(name));
            y += 22;
        }
        g.fill(14, height - 82, left - 6, height - 81, BORDER);
        g.drawString(font, "说明详细度", 16, height - 74, MUTED);
        g.drawString(font, detailLabel(), 16, height - 60, GOLD);
        g.drawString(font, preferences.autoSave ? "保存: 自动" : "保存: 显式", 16, height - 47, TEXT);
    }

    private void renderNarrowCategories(GuiGraphics g) {
        int x = 12 - categoryScroll, y = 54;
        List<String> names = new ArrayList<>();
        names.addAll(navigation());
        g.enableScissor(10, 52, width - 10, 75);
        for (String name : names) {
            int w = font.width(name) + 14;
            g.fill(x, y, x + w, y + 18, name.equals(category) ? 0xFF4B3820 : 0xFF211B28);
            g.drawCenteredString(font, name, x + w / 2, y + 6, name.equals(category) ? GOLD : TEXT);
            x += w + 4;
        }
        g.disableScissor();
    }

    private void nav(GuiGraphics g, String label, int y, boolean selected, int count) {
        if (selected) g.fill(13, y - 3, leftWidth() - 7, y + 16, 0xFF4B3820);
        g.drawString(font, label, 18, y + 2, selected ? GOLD : TEXT);
        String n = Integer.toString(count);
        g.drawString(font, n, leftWidth() - font.width(n) - 13, y + 2, MUTED);
    }

    private void renderCenter(GuiGraphics g) {
        clampCenterScroll();
        int x = centerX();
        int w = centerWidth();
        int top = centerTop();
        panel(g, x, top, x + w, height - 38);
        float titleProgress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS)
                ? listAnimation.value(now()) : 1;
        int titleAlpha = Math.round(255 * titleProgress);
        g.drawString(font, categoryTitle(), x + 14, top + 9 + Math.round((1 - titleProgress) * 4),
                (titleAlpha << 24) | (TEXT & 0xFFFFFF));
        g.drawString(font, categoryDescription(), x + 14, top + 22 + Math.round((1 - titleProgress) * 4),
                (titleAlpha << 24) | (MUTED & 0xFFFFFF));
        String filter = preferences.modifiedOnly ? "仅看修改: 开" : "仅看修改: 关";
        g.drawString(font, filter, x + w - font.width(filter) - 14, top + 22, preferences.modifiedOnly ? GOLD : MUTED);
        g.fill(x + 10, top + 36, x + w - 10, top + 37, BORDER);

        List<YuanConfigCatalog.Setting> visible = visibleSettings();
        int listTop = centerListTop();
        float listProgress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS)
                ? listAnimation.value(now()) : 1;
        int y = listTop - centerScroll.rounded() + listAnimationOffset();
        int bottom = height - 43;
        g.enableScissor(x + 6, listTop - 6, x + w - 6, bottom);
        if (visible.isEmpty()) {
            g.drawCenteredString(font, "没有匹配的设置", x + w / 2, listTop + 31, MUTED);
            g.drawCenteredString(font, "清除搜索或关闭筛选后重试", x + w / 2, listTop + 47, MUTED);
        }
        for (YuanConfigCatalog.Setting setting : visible) {
            int rowHeight = rowHeight(setting);
            if (y + rowHeight >= listTop - 6 && y < bottom) renderSetting(g, setting, x + 12, y, w - 24, rowHeight);
            y += rowHeight + 7;
        }
        g.disableScissor();
        renderCenterScrollbar(g, x, w, listTop, bottom);
    }

    private void renderSetting(GuiGraphics g, YuanConfigCatalog.Setting setting, int x, int y, int w, int h) {
        boolean changed = YuanConfigCatalog.groupChanged(setting, history.changedKeys());
        boolean favorite = YuanConfigCatalog.groupFavorite(setting, preferences.favorites);
        boolean enabled = YuanConfigCatalog.enabled(setting.key(), history.draft());
        g.fill(x, y, x + w, y + h, enabled ? PANEL_ALT : 0xE6151319);
        g.fill(x, y, x + 3, y + h, changed ? GOLD : BORDER);
        g.drawString(font, displayLabel(setting), x + 10, y + 8, enabled ? TEXT : MUTED);
        YuanConfigLayout.Bounds favoriteBounds = YuanConfigLayout.favoriteBounds(x + w, y);
        YuanConfigLayout.Bounds resetBounds = YuanConfigLayout.resetBounds(x + w, y);
        g.drawCenteredString(font, favorite ? "◆" : "◇", (favoriteBounds.left() + favoriteBounds.right()) / 2,
                favoriteBounds.top() + 6, favorite ? GOLD : MUTED);
        g.drawCenteredString(font, "↺", (resetBounds.left() + resetBounds.right()) / 2,
                resetBounds.top() + 6, MUTED);

        List<String> explanation = setting.explain(preferences.detail);
        int lineY = y + 23;
        for (String line : explanation) {
            g.drawString(font, trim(line, w - 150), x + 10, lineY, line.startsWith("警告:") ? 0xFFFF8E78 : MUTED);
            lineY += 11;
        }
        renderControl(g, setting, x + w - 154, y + h - 30, 142, enabled);
    }

    private void renderControl(GuiGraphics g, YuanConfigCatalog.Setting setting, int x, int y, int w, boolean enabled) {
        CompoundTag draft = history.draft();
        int color = enabled ? TEXT : 0xFF625B67;
        if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN) {
            boolean value = draft.contains(setting.key()) ? draft.getBoolean(setting.key()) : setting.defaultValue() != 0;
            float on = animated(setting.key() + ":switch", value ? 1 : 0,
                    YuanClientPreferences.AnimationCategory.CONTROLS, 140);
            YuanConfigLayout.Bounds bounds = YuanConfigLayout.switchBounds(x, y);
            g.fill(bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), value && enabled ? 0xFF665126 : 0xFF2B2630);
            int knob = bounds.left() + 3 + Math.round(on * 41);
            g.fill(knob, y + 10, knob + 11, y + 21, value && enabled ? GOLD : MUTED);
            g.drawString(font, value ? "开启" : "关闭", x + 30, y + 11, value && enabled ? GOLD : MUTED);
            return;
        }
        float value = draft.contains(setting.key())
                ? setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER ? draft.getInt(setting.key()) : draft.getFloat(setting.key())
                : setting.defaultValue();
        if (dragStartValues.containsKey(setting.key())) value = dragStartValues.get(setting.key());
        if (!setting.group().isEmpty()) { renderRgbControl(g, setting.group(), x, y, enabled); return; }
        if (setting.kind() != YuanConfigCatalog.Kind.ENUM)
            value = animated(setting.key(), value, YuanClientPreferences.AnimationCategory.VALUES, 120);
        if (setting.kind() == YuanConfigCatalog.Kind.ENUM) {
            int segment = Math.round(value);
            int count = (int)setting.max() - (int)setting.min() + 1;
            for (int i = (int)setting.min(); i <= setting.max(); i++) {
                int sx = x + (i - (int)setting.min()) * 140 / count;
                int ex = x + (i - (int)setting.min() + 1) * 140 / count - 2;
                g.fill(sx, y + 6, ex, y + 24, i == segment && enabled ? 0xFF5B4625 : 0xFF28222E);
                g.drawCenteredString(font, trim(YuanConfigCatalog.valueLabel(setting.key(), i), ex - sx - 2),
                        (sx + ex) / 2, y + 12,
                        i == segment && enabled ? GOLD : color);
            }
            return;
        }
        float ratio = (value - setting.min()) / Math.max(.0001f, setting.max() - setting.min());
        g.fill(x, y + 2, x + 140, y + 5, 0xFF292330);
        g.fill(x, y + 2, x + Math.round(140 * ratio), y + 5, enabled ? GOLD : MUTED);
        int thumb = x + Math.round(140 * ratio);
        g.fill(thumb - 2, y, thumb + 3, y + 8, enabled ? GOLD : MUTED);
        if (!setting.group().isEmpty()) renderColorSwatch(g, setting.group(), x - 20, y + 11, enabled);
        g.fill(x, y + 11, x + 18, y + 28, 0xFF28222E);
        g.drawCenteredString(font, "-", x + 9, y + 16, color);
        g.fill(x + 21, y + 11, x + 117, y + 28, 0xFF201B26);
        String valueText = setting.kind() == YuanConfigCatalog.Kind.ENUM
                ? YuanConfigCatalog.valueLabel(setting.key(), Math.round(value)) : format(value);
        g.drawCenteredString(font, valueText, x + 69, y + 16, enabled ? GOLD : MUTED);
        g.fill(x + 120, y + 11, x + 140, y + 28, 0xFF28222E);
        g.drawCenteredString(font, "+", x + 130, y + 16, color);
    }

    private void renderColorSwatch(GuiGraphics g, String group, int x, int y, boolean enabled) {
        CompoundTag tag = history.draft();
        String r = group.equals("tint") ? YuanConfig.K_GLASS_TINT_R : YuanConfig.K_GLASS_SHADOW_R;
        String gr = group.equals("tint") ? YuanConfig.K_GLASS_TINT_G : YuanConfig.K_GLASS_SHADOW_G;
        String b = group.equals("tint") ? YuanConfig.K_GLASS_TINT_B : YuanConfig.K_GLASS_SHADOW_B;
        int rgb = ((int)tag.getFloat(r) << 16) | ((int)tag.getFloat(gr) << 8) | (int)tag.getFloat(b);
        g.fill(x, y, x + 14, y + 14, (enabled ? 0xFF000000 : 0x88000000) | rgb);
    }

    private void renderRgbControl(GuiGraphics g, String group, int x, int y, boolean enabled) {
        CompoundTag tag = history.draft();
        String[] keys = rgbKeys(group);
        int[] values = {(int)tag.getFloat(keys[0]), (int)tag.getFloat(keys[1]), (int)tag.getFloat(keys[2])};
        int rgb = (values[0] << 16) | (values[1] << 8) | values[2];
        g.fill(x, y + 5, x + 16, y + 23, (enabled ? 0xFF000000 : 0x88000000) | rgb);
        String[] labels = {"R", "G", "B"};
        for (int i = 0; i < 3; i++) {
            int bx = x + 20 + i * 39;
            g.fill(bx, y + 5, bx + 36, y + 22, 0xFF201B26);
            g.drawCenteredString(font, labels[i] + values[i], bx + 18, y + 10, enabled ? TEXT : MUTED);
        }
        int[] presets = {0x000000, 0xFFFFFF, 0xE1B65A, 0x4696FF};
        for (int i = 0; i < presets.length; i++) {
            int px = x + 20 + i * 28;
            g.fill(px, y + 25, px + 22, y + 30, 0xFF000000 | presets[i]);
        }
    }

    private void renderRight(GuiGraphics g) {
        renderRightAt(g, rightX(), 27, rightWidth(), height - 65);
    }

    private void renderRightOverlay(GuiGraphics g) {
        int w = Math.min(250, width - 32);
        float progress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.PANELS)
                ? drawerAnimation.value(now()) : (showRightOverlay ? 1 : 0);
        int x = width - Math.round((w + 12) * progress);
        g.fill(0, 25, width, height - 34, ((int)(0x99 * progress) << 24));
        renderRightAt(g, x, 29, w, height - 70);
    }

    private int overlayX() {
        int w = Math.min(250, width - 32);
        float progress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.PANELS)
                ? drawerAnimation.value(now()) : (showRightOverlay ? 1 : 0);
        return width - Math.round((w + 12) * progress);
    }

    private void renderRightAt(GuiGraphics g, int x, int y, int w, int h) {
        panel(g, x, y, x + w, y + h);
        g.drawString(font, "实时预览", x + 12, y + 10, TEXT);
        g.fillGradient(x + 12, y + 26, x + w - 12, y + 104, 0xFF251433, 0xFF143444);
        syncPreview();
        YuanSwordTooltipRenderer.renderGlassPanel(g, previewStack, x + 21, y + 42, w - 42, 48);

        int clipTop = y + 110;
        int clipBottom = y + h - 6;
        List<String> presets = YuanPresetManager.list();
        boolean glass = "液态玻璃".equals(realCategory());
        YuanConfigLayout.RightControls controls = YuanConfigLayout.rightControls(x, y, w, rightScroll.rounded(), presets.size(), glass);
        if (glass) renderGlassPresets(g, x + 12, y + 110, w - 24);
        int yy = y + 116 + (glass ? 58 : 0) - rightScroll.rounded();
        g.enableScissor(x + 5, clipTop, x + w - 5, clipBottom);
        g.drawString(font, "修改 " + history.changedKeys().size() + " 项", x + 12, yy, history.isDirty() ? GOLD : MUTED);
        action(g, controls.undo(), "撤销", history.canUndo());
        action(g, controls.redo(), "重做", history.canRedo());
        action(g, controls.reset(), "本页默认", YuanConfigCatalog.categories().contains(category));
        g.drawString(font, "配置交换", x + 12, controls.copy().top() - 16, TEXT);
        action(g, controls.copy(), "复制代码", true);
        action(g, controls.paste(), "粘贴代码", true);
        g.drawString(font, "快捷预设", x + 12, controls.combatDefault().top() - 16, TEXT);
        action(g, controls.combatDefault(), "默认", true);
        action(g, controls.combatAttack(), "攻击", true);
        action(g, controls.combatDefense(), "防御", true);
        action(g, controls.combatTime(), "时停", true);
        g.drawString(font, "本地预设", x + 12, controls.localSave().top() - 40, TEXT);
        presetNameBox.setX(x + 12);
        presetNameBox.setY(controls.localSave().top() - 24);
        presetNameBox.setWidth(Math.max(80, w - 24));
        presetNameBox.visible = controls.localSave().top() - 24 >= clipTop && controls.localSave().top() - 6 <= clipBottom;
        action(g, controls.localSave(), "保存", true);
        action(g, controls.localLoad(), "载入", true);
        action(g, controls.localDelete(), "删除", true);
        int presetY = controls.presetList().top() + 3;
        for (int i = presetScroll; i < Math.min(presets.size(), presetScroll + 3); i++) {
            String name = presets.get(i);
            boolean selected = name.equals(presetName());
            g.drawString(font, (selected ? "> " : "  ") + trim(name, w - 30), x + 12, presetY, selected ? GOLD : MUTED);
            presetY += 13;
        }
        action(g, controls.rename(), "重命名", !selectedPreset.isEmpty());
        g.drawString(font, "界面偏好", x + 12, controls.detail().top() - 16, TEXT);
        g.drawString(font, "说明: " + detailLabel(), x + 12, controls.detail().top(), GOLD);
        g.drawString(font, preferences.autoSave ? "保存模式: 自动" : "保存模式: 显式", x + 12, controls.autoSave().top(), GOLD);
        action(g, controls.animation(), "动画设置", true);
        if (dangerSweepUntil > now()) {
            float progress = 1 - (dangerSweepUntil - now()) / (float)Math.max(1, duration(500));
            int sweepX = x + Math.round((w - 24) * progress);
            g.fill(sweepX - 10, clipTop, sweepX, clipBottom, 0x44E1B65A);
        }
        g.disableScissor();
        YuanConfigLayout.Scrollbar scrollbar = YuanConfigLayout.scrollbar(clipTop, clipBottom,
                controls.contentBottom() - clipTop, rightScroll.rounded());
        renderScrollbar(g, x + w - 4, scrollbar, rightScroll);
    }

    private void action(GuiGraphics g, int x, int y, String label, boolean enabled) {
        g.fill(x, y, x + 54, y + 18, enabled ? 0xFF2E2735 : 0xFF1B181F);
        g.drawCenteredString(font, label, x + 27, y + 6, enabled ? TEXT : 0xFF5F5864);
    }

    private void action(GuiGraphics g, YuanConfigLayout.Bounds bounds, String label, boolean enabled) {
        boolean hover = enabled && bounds.contains(pointerX, pointerY);
        boolean pressed = hover && now() < pressUntil;
        int offset = pressed && preferences.animationEnabled(YuanClientPreferences.AnimationCategory.CONTROLS) ? 1 : 0;
        int fill = !enabled ? 0xFF1B181F : hover ? 0xFF463950 : 0xFF2E2735;
        g.fill(bounds.left(), bounds.top() + offset, bounds.right(), bounds.bottom() + offset, fill);
        g.drawCenteredString(font, label, (bounds.left() + bounds.right()) / 2, bounds.top() + 6 + offset,
                enabled ? TEXT : 0xFF5F5864);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        pressUntil = now() + categoryDuration(YuanClientPreferences.AnimationCategory.CONTROLS, 90);
        if (valueBox != null && valueBox.visible && !valueBox.isMouseOver(mouseX, mouseY)) commitDirectValue();
        if (pendingImport != null) return clickImportPreview(mouseX, mouseY);
        if (showAnimationSettings) return clickAnimationSettings(mouseX, mouseY);
        if (!pendingFileAction.isEmpty()) return clickFileConfirmation(mouseX, mouseY);
        if (confirmClose) {
            if (inside(mouseX, mouseY, width / 2 - 75, height / 2 + 17, 68, 20)) {
                if (saveNow()) { confirmClose = false; closeAfterSave = true; }
            }
            else if (inside(mouseX, mouseY, width / 2 + 7, height / 2 + 17, 68, 20)) closeDirect();
            else {
                confirmClose = false;
                closeAfterSave = false;
            }
            return true;
        }
        if (clickScrollbar(mouseX, mouseY)) return true;
        if (!wide() && inside(mouseX, mouseY, width - 91, 29, 81, 20)) {
            showRightOverlay = !showRightOverlay;
            drawerAnimation.to(showRightOverlay ? 1 : 0, now(), categoryDuration(YuanClientPreferences.AnimationCategory.PANELS, 200), YuanUiAnimation.Easing.SMOOTH);
            presetNameBox.visible = showRightOverlay;
            return true;
        }
        if (showRightOverlay && mouseX < overlayX()) {
            showRightOverlay = false;
            drawerAnimation.to(0, now(), categoryDuration(YuanClientPreferences.AnimationCategory.PANELS, 180), YuanUiAnimation.Easing.SMOOTH);
            presetNameBox.visible = false;
            return true;
        }
        if (narrow() && clickNarrowCategories(mouseX, mouseY)) return true;
        if (clickLeft(mouseX, mouseY) || clickCenter(mouseX, mouseY) || clickRight(mouseX, mouseY)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean clickLeft(double mx, double my) {
        if (narrow()) return false;
        if (mx > leftWidth() || my < 54) return false;
        int y = 57;
        if (inside(mx, my, 12, y - 3, leftWidth() - 19, 19)) return selectCategory("收藏"); y += 22;
        if (inside(mx, my, 12, y - 3, leftWidth() - 19, 19)) return selectCategory("已修改"); y += 26;
        for (String name : YuanConfigCatalog.categories()) {
            if (inside(mx, my, 12, y - 3, leftWidth() - 19, 19)) return selectCategory(name);
            y += 22;
        }
        if (my >= height - 78 && my <= height - 55) {
            preferences.detail = YuanConfigCatalog.Detail.values()[(preferences.detail.ordinal() + 1) % 3];
            preferences.save();
            return true;
        }
        if (my >= height - 55 && my <= height - 38) {
            preferences.autoSave = !preferences.autoSave;
            if (preferences.autoSave && history.isDirty()) saveDueAt = System.currentTimeMillis() + AUTO_SAVE_DELAY_MS;
            preferences.save();
            status = preferences.autoSave ? "自动保存已开启" : "显式保存已开启";
            return true;
        }
        return false;
    }

    private boolean clickNarrowCategories(double mx, double my) {
        if (my < 54 || my >= 72) return false;
        int x = 12 - categoryScroll, y = 54;
        List<String> names = new ArrayList<>();
        names.addAll(navigation());
        for (String name : names) {
            int w = font.width(name) + 14;
            if (my >= y && my < y + 18 && mx >= x && mx < x + w) return selectCategory(name);
            x += w + 4;
        }
        return false;
    }

    private boolean clickCenter(double mx, double my) {
        int x = centerX();
        int w = centerWidth();
        int top = centerTop();
        int listTop = centerListTop();
        if (mx < x || mx > x + w || my < top || my > height - 38) return false;
        if (inside(mx, my, x + w - 95, top + 16, 83, 17)) {
            preferences.modifiedOnly = !preferences.modifiedOnly;
            clampCenterScroll();
            preferences.save();
            return true;
        }
        if (my < listTop - 6 || my > height - 43) return false;
        int y = listTop - centerScroll.rounded() + listAnimationOffset();
        for (YuanConfigCatalog.Setting setting : visibleSettings()) {
            int h = rowHeight(setting);
            if (my >= y && my <= y + h) {
                if (YuanConfigLayout.favoriteBounds(x + w - 12, y).contains(mx, my)) {
                    YuanConfigCatalog.toggleFavorite(setting, preferences.favorites);
                    preferences.save(); return true;
                }
                if (YuanConfigLayout.resetBounds(x + w - 12, y).contains(mx, my)) {
                    if (setting.group().isEmpty()) resetSetting(setting); else history.resetGroup(setting.group());
                    changed(); return true;
                }
                int controlX = x + w - 166;
                int controlY = y + h - 30;
                boolean enabled = YuanConfigCatalog.enabled(setting.key(), history.draft());
                if (!enabled) { status = "此设置当前依赖条件未满足"; return true; }
                if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN
                        && YuanConfigLayout.switchBounds(controlX, controlY).contains(mx, my)) {
                    CompoundTag draft = history.draft();
                    boolean value = draft.contains(setting.key()) ? draft.getBoolean(setting.key()) : setting.defaultValue() != 0;
                    history.setBoolean(setting.key(), !value);
                    if (!value && setting.dangerous() && preferences.animationEnabled(YuanClientPreferences.AnimationCategory.DANGER))
                        dangerSweepUntil = now() + duration(500);
                    changed(); return true;
                }
                if (setting.kind() != YuanConfigCatalog.Kind.BOOLEAN) {
                    if (!setting.group().isEmpty()) {
                        if (inside(mx, my, controlX + 20, controlY + 5, 117, 17)) {
                            int channel = Mth.clamp((int)((mx - controlX - 20) / 39), 0, 2);
                            editValue(YuanConfigCatalog.byKey(rgbKeys(setting.group())[channel])); return true;
                        }
                        if (inside(mx, my, controlX + 20, controlY + 25, 112, 7)) {
                            applyRgbPreset(setting.group(), Mth.clamp((int)((mx - controlX - 20) / 28), 0, 3));
                            return true;
                        }
                    }
                    if (setting.kind() == YuanConfigCatalog.Kind.ENUM && inside(mx, my, controlX, controlY + 6, 140, 18)) {
                        history.setInt(setting.key(), enumValueAt(setting, mx, controlX, 140));
                        changed(); return true;
                    }
                    if (inside(mx, my, controlX, controlY, 140, 8)) {
                        draggingKey = setting.key();
                        CompoundTag draft = history.draft();
                        dragStartValues.put(setting.key(), setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER
                                ? (float)draft.getInt(setting.key()) : draft.getFloat(setting.key()));
                        updateSlider(setting, mx, controlX); return true;
                    }
                    if (inside(mx, my, controlX, controlY + 11, 18, 17)) { adjust(setting, -setting.step()); return true; }
                    if (inside(mx, my, controlX + 120, controlY + 11, 20, 17)) { adjust(setting, setting.step()); return true; }
                    if (inside(mx, my, controlX + 21, controlY + 11, 96, 17)) { editValue(setting); return true; }
                }
            }
            y += h + 7;
        }
        return false;
    }

    private boolean clickRight(double mx, double my) {
        if (!wide() && !showRightOverlay) return false;
        int x = wide() ? rightX() : overlayX();
        int y = wide() ? 27 : 29;
        int w = wide() ? rightWidth() : Math.min(250, width - 32);
        if (mx < x || mx > x + w) return false;
        List<String> presets = YuanPresetManager.list();
        boolean glass = "液态玻璃".equals(realCategory());
        YuanConfigLayout.RightControls controls = YuanConfigLayout.rightControls(x, y, w, rightScroll.rounded(), presets.size(), glass);
        if (glass && clickGlassPresets(mx, my, x + 12, y + 110, w - 24)) return true;
        if (controls.undo().contains(mx, my)) { history.undo(); triggerActionSweep(); changed(); return true; }
        if (controls.redo().contains(mx, my)) { history.redo(); triggerActionSweep(); changed(); return true; }
        if (controls.reset().contains(mx, my) && YuanConfigCatalog.categories().contains(category)) {
            history.resetCategory(category); triggerActionSweep(); changed(); return true;
        }
        if (controls.copy().contains(mx, my)) { minecraft.keyboardHandler.setClipboard(YuanPresetManager.toJson(history.draft())); status = "配置码已复制"; return true; }
        if (controls.paste().contains(mx, my)) { importJson(minecraft.keyboardHandler.getClipboard()); return true; }
        if (controls.combatDefault().contains(mx, my)) { applyCombatPreset(0); return true; }
        if (controls.combatAttack().contains(mx, my)) { applyCombatPreset(1); return true; }
        if (controls.combatDefense().contains(mx, my)) { applyCombatPreset(2); return true; }
        if (controls.combatTime().contains(mx, my)) { applyCombatPreset(3); return true; }
        if (controls.localSave().contains(mx, my)) { localSave(); return true; }
        if (controls.localLoad().contains(mx, my)) { localLoad(); return true; }
        if (controls.localDelete().contains(mx, my)) { localDelete(); return true; }
        int presetY = controls.presetList().top();
        for (int i = presetScroll; i < Math.min(presets.size(), presetScroll + 3); i++) {
            if (inside(mx, my, x + 10, presetY, w - 20, 13)) {
                selectedPreset = presets.get(i); presetNameBox.setValue(selectedPreset); return true;
            }
            presetY += 13;
        }
        if (controls.rename().contains(mx, my) && !selectedPreset.isEmpty()) {
            pendingFileAction = "rename"; return true;
        }
        if (controls.detail().contains(mx, my)) {
            preferences.detail = YuanConfigCatalog.Detail.values()[(preferences.detail.ordinal() + 1) % 3];
            preferences.save(); return true;
        }
        if (controls.autoSave().contains(mx, my)) {
            preferences.autoSave = !preferences.autoSave;
            if (preferences.autoSave && history.isDirty()) saveDueAt = System.currentTimeMillis() + AUTO_SAVE_DELAY_MS;
            preferences.save(); return true;
        }
        if (controls.animation().contains(mx, my)) {
            showAnimationSettings = true;
            modalAnimation.to(1, now(), categoryDuration(YuanClientPreferences.AnimationCategory.PANELS, 180), YuanUiAnimation.Easing.OUT_BACK);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!draggingScrollbar.isEmpty()) {
            dragScrollbar(mouseY);
            return true;
        }
        if (draggingKey != null) {
            YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(draggingKey);
            if (setting != null) updateSlider(setting, mouseX, centerX() + centerWidth() - 166);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!draggingScrollbar.isEmpty()) {
            if (draggingScrollbar.equals("center")) centerScroll.stopDrag(); else rightScroll.stopDrag();
            draggingScrollbar = "";
            return true;
        }
        if (draggingKey != null && dragStartValues.containsKey(draggingKey)) {
            float value = dragStartValues.get(draggingKey);
            String key = draggingKey;
            draggingKey = null;
            dragStartValues.clear();
            YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(key);
            if (setting != null && (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER)) history.setInt(key, Math.round(value));
            else history.setFloat(key, value);
            changed();
            return true;
        }
        draggingKey = null;
        dragStartValues.clear();
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (narrow() && mouseY >= 52 && mouseY <= 75) {
            categoryScroll = Mth.clamp(categoryScroll - (int)Math.signum(delta) * 40, 0, maxCategoryScroll());
            return true;
        }
        if ((wide() && mouseX >= rightX()) || (!wide() && showRightOverlay && mouseX >= overlayX())) {
            List<String> presets = YuanPresetManager.list();
            int panelX = wide() ? rightX() : overlayX();
            int panelY = wide() ? 27 : 29;
            int panelW = wide() ? rightWidth() : Math.min(250, width - 32);
            boolean glass = "液态玻璃".equals(realCategory());
            YuanConfigLayout.RightControls controls = YuanConfigLayout.rightControls(panelX, panelY, panelW, rightScroll.rounded(), presets.size(), glass);
            if (presets.size() > 3 && controls.presetList().contains(mouseX, mouseY)) {
                presetScroll = Mth.clamp(presetScroll - (int)Math.signum(delta), 0, presets.size() - 3);
                return true;
            }
            int x = panelX;
            int y = wide() ? 27 : 29;
            int w = wide() ? rightWidth() : Math.min(250, width - 32);
            int bottom = y + (wide() ? height - 65 : height - 70) - 6;
            int max = YuanConfigLayout.rightControls(x, y, w, 0, YuanPresetManager.list().size(), glass).maxScroll(bottom);
            rightScroll.setMax(max);
            rightScroll.wheel((float)-delta, preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS) && !preferences.reduceMotion);
            return true;
        }
        if (mouseX >= centerX() && mouseX <= centerX() + centerWidth()) {
            centerScroll.setMax(maxCenterScroll());
            centerScroll.wheel((float)-delta, preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS) && !preferences.reduceMotion);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (valueBox != null && valueBox.visible && valueBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) { commitDirectValue(); return true; }
            YuanConfigCatalog.Setting editing = YuanConfigCatalog.byKey(editingKey);
            if (editing != null && (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN)) {
                float step = editing.step() * (hasShiftDown() ? 5 : 1) * (keyCode == GLFW.GLFW_KEY_UP ? 1 : -1);
                CompoundTag draft = history.draft();
                float base = pendingInputValue == null
                    ? editing.kind() == YuanConfigCatalog.Kind.ENUM || editing.kind() == YuanConfigCatalog.Kind.INTEGER ? draft.getInt(editingKey) : draft.getFloat(editingKey)
                        : pendingInputValue;
                pendingInputValue = Mth.clamp(base + step, editing.min(), editing.max());
                valueBox.setValue(format(pendingInputValue));
                return true;
            }
        }
        if (hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_F) { setFocused(searchBox); searchBox.setFocused(true); return true; }
            if (keyCode == GLFW.GLFW_KEY_Z) { history.undo(); changed(); return true; }
            if (keyCode == GLFW.GLFW_KEY_Y) { history.redo(); changed(); return true; }
            if (keyCode == GLFW.GLFW_KEY_S) { saveNow(); return true; }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { requestClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() { requestClose(); }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public static boolean worldAdjustWidgetVisible(boolean adjusting, boolean adjustmentWidget,
                                                   boolean normalVisibility) {
        return adjusting ? adjustmentWidget : normalVisibility;
    }

    @Override
    public void removed() {
        savePreferences();
        super.removed();
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        super.onFilesDrop(paths);
    }

    private void editValue(YuanConfigCatalog.Setting setting) {
        editingKey = setting.key();
        CompoundTag draft = history.draft();
        float value = draft.contains(setting.key())
                ? setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER ? draft.getInt(setting.key()) : draft.getFloat(setting.key())
                : setting.defaultValue();
        valueBox.setValue(format(value));
        pendingInputValue = value;
        valueBox.visible = true;
        valueBox.setFocused(true);
    }

    private void applyDirectValue(String text) {
        if (editingKey == null || text.isBlank() || text.equals("-") || text.endsWith(".")) return;
        try { pendingInputValue = Float.parseFloat(text); }
        catch (NumberFormatException ignored) { }
    }

    private void commitDirectValue() {
        if (editingKey != null && pendingInputValue != null) {
            YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(editingKey);
            commitNumericValue(history, setting, pendingInputValue);
            changed();
        }
        editingKey = null;
        pendingInputValue = null;
        valueBox.visible = false;
        valueBox.setFocused(false);
    }

    private void resetSetting(YuanConfigCatalog.Setting setting) {
        if (setting.kind() == YuanConfigCatalog.Kind.BOOLEAN) history.setBoolean(setting.key(), setting.defaultValue() != 0);
        else if (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER) history.setInt(setting.key(), (int)setting.defaultValue());
        else history.setFloat(setting.key(), setting.defaultValue());
        changed();
    }

    private void adjust(YuanConfigCatalog.Setting setting, float delta) {
        CompoundTag draft = history.draft();
        float current = draft.contains(setting.key())
                ? setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER ? draft.getInt(setting.key()) : draft.getFloat(setting.key())
                : setting.defaultValue();
        if (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER) history.setInt(setting.key(), Math.round(current + delta));
        else history.setFloat(setting.key(), current + delta);
        changed();
    }

    private void applyCombatPreset(int preset) {
        ItemStack temporary = stack.copy();
        temporary.getOrCreateTag().put("YuanConfig", history.draft());
        switch (preset) {
            case 0 -> YuanConfig.resetDefault(temporary);
            case 1 -> YuanConfig.presetAttack(temporary);
            case 2 -> YuanConfig.presetDefense(temporary);
            case 3 -> YuanConfig.presetTimeStop(temporary);
            default -> { return; }
        }
        history.replace(temporary.getOrCreateTagElement("YuanConfig"));
        status = switch (preset) { case 0 -> "已恢复默认预设"; case 1 -> "已应用攻击预设";
            case 2 -> "已应用防御预设"; default -> "已应用时停预设"; };
        triggerActionSweep();
        changed();
    }

    private void applyRgbPreset(String group, int preset) {
        int[] colors = {0x000000, 0xFFFFFF, 0xE1B65A, 0x4696FF};
        int rgb = colors[Mth.clamp(preset, 0, colors.length - 1)];
        String[] keys = rgbKeys(group);
        history.edit(tag -> {
            tag.putFloat(keys[0], (rgb >> 16) & 255);
            tag.putFloat(keys[1], (rgb >> 8) & 255);
            tag.putFloat(keys[2], rgb & 255);
        });
        changed();
    }

    private String[] rgbKeys(String group) {
        return group.equals("tint")
                ? new String[]{YuanConfig.K_GLASS_TINT_R, YuanConfig.K_GLASS_TINT_G, YuanConfig.K_GLASS_TINT_B}
                : new String[]{YuanConfig.K_GLASS_SHADOW_R, YuanConfig.K_GLASS_SHADOW_G, YuanConfig.K_GLASS_SHADOW_B};
    }

    private void changed() {
        syncPreview();
        status = history.changedKeys().size() + " 项待保存";
        if (preferences.autoSave) saveDueAt = System.currentTimeMillis() + AUTO_SAVE_DELAY_MS;
    }

    private void updateSlider(YuanConfigCatalog.Setting setting, double mouseX, int controlX) {
        float ratio = Mth.clamp((float)((mouseX - controlX) / 140.0), 0, 1);
        float raw = setting.min() + ratio * (setting.max() - setting.min());
        float snapped = Math.round(raw / setting.step()) * setting.step();
        dragStartValues.put(setting.key(), Mth.clamp(snapped, setting.min(), setting.max()));
        syncPreview();
    }

    private boolean saveNow() {
        if (minecraft.player == null || minecraft.player.getMainHandItem() != stack
                || !(stack.getItem() instanceof YuanSwordItem)) {
            status = "保存失败: 请在主手持有打开界面时的虚渊";
            return false;
        }
        CompoundTag draft = history.draft();
        CompoundTag config = buildSavePayload(draft);
        if (preferences.autoSave) stack.getOrCreateTag().put("YuanConfig", config.copy());
        long requestId = saveState.begin(preferences.autoSave);
        sentDraft = draft.copy();
        sentDrafts.put(requestId, draft.copy());
        Yuan.CHANNEL.sendToServer(new ConfigSyncPacket(config, minecraft.player.getInventory().selected, requestId, expectedServerIdentity));
        expectedServerIdentity = config.toString().hashCode();
        status = preferences.autoSave ? "等待服务端确认" : "正在同步";
        return true;
    }

    public void handleSaveAcknowledgement(long requestId, boolean success, CompoundTag config,
                                          int corrections, String message) {
        if (!saveState.acknowledge(requestId, success, config, message)) return;
        CompoundTag submitted = sentDrafts.remove(requestId);
        if (!success) {
            expectedServerIdentity = latestApplied.toString().hashCode();
            stack.getOrCreateTag().put("YuanConfig", latestApplied.copy());
            syncPreview();
            status = "同步失败: " + message;
            closeAfterSave = false;
            return;
        }
        latestApplied = config.copy();
        expectedServerIdentity = config.toString().hashCode();
        if (submitted != null && history.draft().equals(submitted)) history.reconcile(config);
        else history.markSaved(config);
        stack.getOrCreateTag().put("YuanConfig", config.copy());
        syncPreview();
        status = corrections == 0 ? "配置已同步" : "配置已同步, 服务端修正 " + corrections + " 项";
        triggerActionSweep();
        if (closeAfterSave && !history.isDirty()) closeDirect();
        closeAfterSave = false;
    }

    private void requestClose() {
        requestWeaponClose();
    }

    private void requestWeaponClose() {
        if (saveState.hasPendingExplicit()) {
            status = "正在等待服务端确认";
            closeAfterSave = true;
            return;
        }
        if (preferences.autoSave && history.isDirty()) {
            if (saveNow()) closeAfterSave = true;
            else confirmClose = true;
        } else if (history.isDirty()) confirmClose = true;
        else closeDirect();
    }

    private void closeDirect() {
        savePreferences();
        minecraft.setScreen(null);
    }

    private void savePreferences() {
        if (preferencesSaved) return;
        preferencesSaved = true;
        preferences.lastCategory = realCategory();
        preferences.save();
    }

    private void importJson(String json) {
        try {
            pendingImport = YuanPresetManager.parse(json, history.draft());
            pendingImportSource = "剪贴板";
            modalAnimation.to(1, now(), categoryDuration(YuanClientPreferences.AnimationCategory.PANELS, 180), YuanUiAnimation.Easing.OUT_BACK);
        } catch (Exception error) {
            status = "导入失败: " + error.getMessage();
        }
    }

    private void localSave() {
        if (YuanPresetManager.exists(presetName())) { pendingFileAction = "overwrite"; return; }
        try { YuanPresetManager.save(presetName(), history.draft()); status = "本地预设已保存"; }
        catch (Exception error) { status = "保存预设失败"; }
    }

    private void localLoad() {
        try { pendingImport = YuanPresetManager.load(presetName(), history.draft()); pendingImportSource = presetName();
            modalAnimation.to(1, now(), categoryDuration(YuanClientPreferences.AnimationCategory.PANELS, 180), YuanUiAnimation.Easing.OUT_BACK); }
        catch (Exception error) { status = "找不到本地预设"; }
    }

    private void localDelete() {
        pendingFileAction = "delete";
    }

    private String presetName() {
        String value = presetNameBox == null ? preferences.lastPreset : presetNameBox.getValue();
        return YuanPresetManager.normalizedName(value == null || value.isBlank() ? "quick" : value);
    }

    private boolean selectCategory(String value) {
        category = value;
        centerScroll.set(0); rightScroll.set(0); preferences.lastCategory = realCategory(); preferences.save();
        listAnimation.to(0, now(), 0, YuanUiAnimation.Easing.LINEAR);
        listAnimation.to(1, now(), categoryDuration(YuanClientPreferences.AnimationCategory.LISTS, 180), YuanUiAnimation.Easing.SMOOTH);
        return true;
    }


    public static <T> T lazyPageCheck(boolean selected, Supplier<T> factory) {
        return selected ? factory.get() : null;
    }

    private List<YuanConfigCatalog.Setting> visibleSettings() {
        Set<String> changed = history.changedKeys();
        return YuanConfigCatalog.visibleSettings(category, preferences.search, preferences.modifiedOnly,
                preferences.favorites, changed);
    }

    private int countCategory(String name) {
        return (int)YuanConfigCatalog.all().stream().filter(setting -> setting.category().equals(name)).count();
    }

    private int maxCenterScroll() {
        int total = centerContentHeight();
        int viewport = height - 43 - centerListTop();
        return Math.max(0, total - viewport);
    }

    private void clampCenterScroll() { centerScroll.setMax(maxCenterScroll()); }

    private int centerContentHeight() {
        List<YuanConfigCatalog.Setting> settings = visibleSettings();
        if (settings.isEmpty()) return 0;
        return settings.stream().mapToInt(setting -> rowHeight(setting) + 7).sum() - 7;
    }

    private void renderCenterScrollbar(GuiGraphics g, int x, int w, int top, int bottom) {
        YuanConfigLayout.Scrollbar bar = YuanConfigLayout.scrollbar(top, bottom, centerContentHeight(), centerScroll.rounded());
        renderScrollbar(g, x + w - 5, bar, centerScroll);
    }

    private void renderScrollbar(GuiGraphics g, int x, YuanConfigLayout.Scrollbar bar, YuanScrollState state) {
        if (!bar.visible()) return;
        float visibility = state.visibility();
        int trackAlpha = Math.round(0x66 * visibility);
        int thumbAlpha = Math.round(0xFF * visibility);
        g.fill(x, bar.trackTop(), x + 2, bar.trackBottom(), (trackAlpha << 24) | (BORDER & 0xFFFFFF));
        boolean dragging = state.dragging();
        int expand = dragging && preferences.animationEnabled(YuanClientPreferences.AnimationCategory.CONTROLS) ? 1 : 0;
        g.fill(x - 1 - expand, bar.thumbTop(), x + 3 + expand, bar.thumbTop() + bar.thumbHeight(),
                (thumbAlpha << 24) | (GOLD & 0xFFFFFF));
    }

    private void renderGlassPresets(GuiGraphics g, int x, int y, int w) {
        String[] visual = {"默认", "清透", "折射", "柔霜", "晶体", "暗曜", "虚渊"};
        String[] colors = {"无色", "冰蓝", "紫", "鎏金", "极光", "玫瑰", "黑曜"};
        List<YuanConfigLayout.Bounds> visualBounds = YuanConfigLayout.glassPresetBounds(x, y, w, 0);
        List<YuanConfigLayout.Bounds> colorBounds = YuanConfigLayout.glassPresetBounds(x, y, w, 1);
        for (int i = 0; i < 7; i++) {
            presetButton(g, visualBounds.get(i), visual[i], i == selectedGlassVisual);
            presetButton(g, colorBounds.get(i), colors[i], i == selectedGlassColor);
        }
    }

    private void presetButton(GuiGraphics g, YuanConfigLayout.Bounds bounds, String label, boolean selected) {
        boolean hover = bounds.contains(pointerX, pointerY);
        boolean pressed = hover && now() < pressUntil;
        int offset = pressed && preferences.animationEnabled(YuanClientPreferences.AnimationCategory.CONTROLS) ? 1 : 0;
        int fill = selected ? 0xFF5B4625 : hover ? 0xFF463950 : 0xFF28222E;
        g.fill(bounds.left(), bounds.top() + offset, bounds.right(), bounds.bottom() + offset, fill);
        g.drawCenteredString(font, trim(label, bounds.width() - 2), (bounds.left() + bounds.right()) / 2,
                bounds.top() + 6 + offset, selected ? GOLD : TEXT);
    }

    private boolean clickGlassPresets(double mx, double my, int x, int y, int w) {
        List<YuanConfigLayout.Bounds> visual = YuanConfigLayout.glassPresetBounds(x, y, w, 0);
        List<YuanConfigLayout.Bounds> colors = YuanConfigLayout.glassPresetBounds(x, y, w, 1);
        for (int i = 0; i < 7; i++) {
            if (visual.get(i).contains(mx, my)) { applyGlassPreset(i, true); return true; }
            if (colors.get(i).contains(mx, my)) { applyGlassPreset(i, false); return true; }
        }
        return false;
    }

    private void applyGlassPreset(int preset, boolean visual) {
        ItemStack temporary = stack.copy();
        temporary.getOrCreateTag().put("YuanConfig", history.draft());
        if (visual) {
            YuanConfig.applyGlassVisualPreset(temporary, preset);
            selectedGlassVisual = preset;
        } else {
            YuanConfig.applyGlassColorPreset(temporary, preset);
            selectedGlassColor = preset;
        }
        history.replace(temporary.getOrCreateTagElement("YuanConfig"));
        triggerActionSweep();
        status = visual ? "已应用液态玻璃视觉预设" : "已应用液态玻璃颜色预设";
        changed();
    }

    private boolean clickScrollbar(double mx, double my) {
        int centerTop = centerListTop(), centerBottom = height - 43;
        YuanConfigLayout.Scrollbar centerBar = YuanConfigLayout.scrollbar(centerTop, centerBottom,
                centerContentHeight(), centerScroll.rounded());
        int centerX = centerX() + centerWidth() - 5;
        if (centerBar.visible() && mx >= centerX - 8 && mx <= centerX + 8
                && my >= centerBar.trackTop() && my <= centerBar.trackBottom()) {
            centerScroll.setMax(maxCenterScroll()); centerScroll.startDrag(); draggingScrollbar = "center";
            centerScroll.dragTo((float)my, centerBar.trackTop(), centerBar.trackBottom(), centerBar.thumbHeight());
            return true;
        }
        int x = wide() ? rightX() : overlayX();
        if (!wide() && !showRightOverlay) return false;
        int y = wide() ? 27 : 29, w = wide() ? rightWidth() : Math.min(250, width - 32);
        int h = wide() ? height - 65 : height - 70;
        int clipTop = y + 110, clipBottom = y + h - 6;
        boolean glass = "液态玻璃".equals(realCategory());
        YuanConfigLayout.RightControls controls = YuanConfigLayout.rightControls(x, y, w, rightScroll.rounded(),
                YuanPresetManager.list().size(), glass);
        int contentHeight = controls.contentBottom() - clipTop;
        YuanConfigLayout.Scrollbar rightBar = YuanConfigLayout.scrollbar(clipTop, clipBottom, contentHeight, rightScroll.rounded());
        int barX = x + w - 4;
        if (rightBar.visible() && mx >= barX - 8 && mx <= barX + 8
                && my >= rightBar.trackTop() && my <= rightBar.trackBottom()) {
            rightScroll.setMax(controls.maxScroll(clipBottom)); rightScroll.startDrag(); draggingScrollbar = "right";
            rightScroll.dragTo((float)my, rightBar.trackTop(), rightBar.trackBottom(), rightBar.thumbHeight());
            return true;
        }
        return false;
    }

    private void dragScrollbar(double mouseY) {
        if (draggingScrollbar.equals("center")) {
            int top = centerListTop(), bottom = height - 43;
            YuanConfigLayout.Scrollbar bar = YuanConfigLayout.scrollbar(top, bottom, centerContentHeight(), centerScroll.rounded());
            centerScroll.dragTo((float)mouseY, top, bottom, bar.thumbHeight());
        } else {
            int x = wide() ? rightX() : overlayX();
            int y = wide() ? 27 : 29, w = wide() ? rightWidth() : Math.min(250, width - 32);
            int h = wide() ? height - 65 : height - 70;
            int top = y + 110, bottom = y + h - 6;
            boolean glass = "液态玻璃".equals(realCategory());
            YuanConfigLayout.RightControls controls = YuanConfigLayout.rightControls(x, y, w, rightScroll.rounded(),
                    YuanPresetManager.list().size(), glass);
            YuanConfigLayout.Scrollbar bar = YuanConfigLayout.scrollbar(top, bottom,
                    controls.contentBottom() - top, rightScroll.rounded());
            rightScroll.dragTo((float)mouseY, top, bottom, bar.thumbHeight());
        }
    }

    public static int enumValueAt(YuanConfigCatalog.Setting setting, double mouseX, int controlX, int width) {
        int count = (int)setting.max() - (int)setting.min() + 1;
        int index = Mth.clamp((int)((mouseX - controlX) * count / width), 0, count - 1);
        return (int)setting.min() + index;
    }

    private int rowHeight(YuanConfigCatalog.Setting setting) {
        return 43 + setting.explain(preferences.detail).size() * 11;
    }

    private void syncPreview() {
        CompoundTag preview = buildPreviewConfig(history.draft(), dragStartValues);
        if (preferences.animationEnabled(YuanClientPreferences.AnimationCategory.PREVIEW)) {
            for (YuanConfigCatalog.Setting setting : YuanConfigCatalog.all()) {
                if (setting.kind() != YuanConfigCatalog.Kind.NUMBER) continue;
                float target = preview.getFloat(setting.key());
                preview.putFloat(setting.key(), animated("preview:" + setting.key(), target,
                        YuanClientPreferences.AnimationCategory.PREVIEW, 160));
            }
        }
        previewStack.getOrCreateTag().put("YuanConfig", preview);
    }

    public static void commitNumericValue(YuanConfigHistory history, YuanConfigCatalog.Setting setting, float value) {
        if (setting != null && (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER))
            history.setInt(setting.key(), Math.round(value));
        else if (setting != null) history.setFloat(setting.key(), value);
    }

    public static CompoundTag buildPreviewConfig(CompoundTag draft, Map<String, Float> pendingValues) {
        CompoundTag preview = draft.copy();
        for (var entry : pendingValues.entrySet()) {
            YuanConfigCatalog.Setting setting = YuanConfigCatalog.byKey(entry.getKey());
            if (setting != null && (setting.kind() == YuanConfigCatalog.Kind.ENUM || setting.kind() == YuanConfigCatalog.Kind.INTEGER))
                preview.putInt(entry.getKey(), Math.round(entry.getValue()));
            else preview.putFloat(entry.getKey(), entry.getValue());
        }
        return preview;
    }

    public static CompoundTag buildSavePayload(CompoundTag draft) {
        return YuanConfig.sanitize(draft);
    }

    private float animated(String key, float target, YuanClientPreferences.AnimationCategory category, long milliseconds) {
        if (!preferences.animationEnabled(category)) return target;
        YuanUiAnimation.Track track = animations.computeIfAbsent(key, ignored -> new YuanUiAnimation.Track(target));
        if (Float.compare(track.target(), target) != 0)
            track.to(target, now(), categoryDuration(category, milliseconds), YuanUiAnimation.Easing.SMOOTH);
        return track.value(now());
    }

    private String displayLabel(YuanConfigCatalog.Setting setting) {
        if (setting.group().equals("tint")) return "玻璃色调 " + setting.label().substring(setting.label().length() - 1);
        if (setting.group().equals("shadow")) return "阴影颜色 " + setting.label().substring(setting.label().length() - 1);
        return setting.label();
    }

    private void renderImportPreview(GuiGraphics g) {
        renderModalBackdrop(g);
        int w = Math.min(300, width - 20), h = Math.min(170, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        panel(g, x, y, x + w, y + h);
        g.drawString(font, "导入预览: " + pendingImportSource, x + 16, y + 14, GOLD);
        g.drawString(font, "将修改 " + pendingImport.changedKeys().size() + " 项", x + 16, y + 38, TEXT);
        g.drawString(font, "未知字段 " + pendingImport.unknownKeys().size() + " 项", x + 16, y + 54, MUTED);
        g.drawString(font, "越界修正 " + pendingImport.clampedKeys().size() + " 项", x + 16, y + 70, MUTED);
        g.drawString(font, "保留未提供字段 " + pendingImport.preservedCount() + " 项", x + 16, y + 86, MUTED);
        if (!pendingImport.dangerousKeys().isEmpty())
            g.drawString(font, "警告: 将启用 " + pendingImport.dangerousKeys().size() + " 个危险能力", x + 16, y + 105, 0xFFFF8E78);
        modalButton(g, x + 62, y + 135, "确认导入", true);
        modalButton(g, x + 166, y + 135, "取消", false);
    }

    private boolean clickImportPreview(double mx, double my) {
        int w = Math.min(300, width - 20), h = Math.min(170, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        if (inside(mx, my, x + 62, y + 135, 82, 22)) {
            history.replace(pendingImport.config());
            status = "已导入 " + pendingImport.changedKeys().size() + " 项";
            pendingImport = null; changed(); return true;
        }
        if (inside(mx, my, x + 166, y + 135, 72, 22)) { pendingImport = null; return true; }
        return true;
    }

    private void renderFileConfirmation(GuiGraphics g) {
        renderModalBackdrop(g);
        int w = Math.min(240, width - 20), h = Math.min(110, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        panel(g, x, y, x + w, y + h);
        String title = switch (pendingFileAction) { case "delete" -> "删除本地预设？"; case "rename" -> "重命名本地预设？"; default -> "覆盖已有预设？"; };
        g.drawCenteredString(font, title, width / 2, y + 20, GOLD);
        g.drawCenteredString(font, presetName(), width / 2, y + 42, TEXT);
        modalButton(g, x + 35, y + 72, "确认", true);
        modalButton(g, x + 133, y + 72, "取消", false);
    }

    private boolean clickFileConfirmation(double mx, double my) {
        int w = Math.min(240, width - 20), h = Math.min(110, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        if (inside(mx, my, x + 35, y + 72, 72, 22)) {
            try {
                if (pendingFileAction.equals("delete")) YuanPresetManager.delete(selectedPreset.isEmpty() ? presetName() : selectedPreset);
                else if (pendingFileAction.equals("rename")) YuanPresetManager.rename(selectedPreset, presetName());
                else YuanPresetManager.save(presetName(), history.draft());
                status = switch (pendingFileAction) { case "delete" -> "本地预设已删除"; case "rename" -> "本地预设已重命名"; default -> "本地预设已覆盖"; };
            } catch (Exception error) { status = "本地预设操作失败"; }
            pendingFileAction = ""; return true;
        }
        if (inside(mx, my, x + 133, y + 72, 72, 22)) { pendingFileAction = ""; return true; }
        return true;
    }

    private void renderAnimationSettings(GuiGraphics g) {
        renderModalBackdrop(g);
        int w = Math.min(330, width - 20), h = Math.min(210, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        panel(g, x, y, x + w, y + h);
        g.drawString(font, "动画设置", x + 16, y + 14, GOLD);
        g.drawString(font, "预设: " + animationPresetLabel(), x + 16, y + 38, TEXT);
        g.drawString(font, "总开关: " + onOff(preferences.animations), x + 190, y + 38, preferences.animations ? GOLD : MUTED);
        YuanClientPreferences.AnimationCategory[] categories = YuanClientPreferences.AnimationCategory.values();
        for (int i = 0; i < categories.length; i++) {
            YuanClientPreferences.AnimationCategory category = categories[i];
            boolean enabled = preferences.animationCategories.contains(category);
            int column = i / 4, row = i % 4;
            int cx = x + 18 + column * (w / 2), cy = y + 62 + row * 18;
            g.drawString(font, animationCategoryLabel(category), cx, cy, TEXT);
            g.drawString(font, enabled ? "开" : "关", cx + w / 2 - 46, cy, enabled ? GOLD : MUTED);
        }
        int yy = y + 139;
        g.drawString(font, "速度: " + preferences.animationSpeed + "x", x + 20, yy, TEXT);
        g.drawString(font, "强度: " + intensityLabel(), x + 118, yy, TEXT);
        g.drawString(font, "减少动态: " + onOff(preferences.reduceMotion), x + 205, yy,
                preferences.reduceMotion ? GOLD : MUTED);
        modalButton(g, x + 64, y + h - 34, "恢复预设", false);
        modalButton(g, x + 190, y + h - 34, "完成", true);
    }

    private boolean clickAnimationSettings(double mx, double my) {
        int w = Math.min(330, width - 20), h = Math.min(210, height - 20), x = width / 2 - w / 2, y = height / 2 - h / 2;
        if (inside(mx, my, x + 12, y + 28, 150, 24)) {
            YuanClientPreferences.AnimationPreset[] values = YuanClientPreferences.AnimationPreset.values();
            preferences.applyAnimationPreset(values[(preferences.animationPreset.ordinal() + 1) % values.length]);
            preferences.save(); return true;
        }
        if (inside(mx, my, x + 180, y + 28, 125, 24)) {
            preferences.animations = !preferences.animations; preferences.save(); return true;
        }
        YuanClientPreferences.AnimationCategory[] categories = YuanClientPreferences.AnimationCategory.values();
        for (int i = 0; i < categories.length; i++) {
            YuanClientPreferences.AnimationCategory category = categories[i];
            int column = i / 4, row = i % 4;
            int cx = x + 12 + column * (w / 2), cy = y + 55 + row * 18;
            if (inside(mx, my, cx, cy, w / 2 - 14, 18)) {
                if (!preferences.animationCategories.add(category)) preferences.animationCategories.remove(category);
                preferences.save(); return true;
            }
        }
        int yy = y + 132;
        if (inside(mx, my, x + 14, yy, 94, 22)) {
            float[] speeds = {.5f, .75f, 1f, 1.25f, 1.5f};
            int index = 0;
            for (int i = 0; i < speeds.length; i++) if (speeds[i] == preferences.animationSpeed) index = i;
            preferences.animationSpeed = speeds[(index + 1) % speeds.length]; preferences.save(); return true;
        }
        if (inside(mx, my, x + 108, yy, 86, 22)) {
            YuanClientPreferences.AnimationIntensity[] values = YuanClientPreferences.AnimationIntensity.values();
            preferences.animationIntensity = values[(preferences.animationIntensity.ordinal() + 1) % values.length];
            preferences.save(); return true;
        }
        if (inside(mx, my, x + 194, yy, 120, 22)) {
            preferences.reduceMotion = !preferences.reduceMotion; preferences.save(); return true;
        }
        if (inside(mx, my, x + 64, y + h - 34, 82, 22)) {
            preferences.applyAnimationPreset(preferences.animationPreset); preferences.save(); return true;
        }
        if (inside(mx, my, x + 190, y + h - 34, 72, 22)) { showAnimationSettings = false; return true; }
        return true;
    }

    private void renderModalBackdrop(GuiGraphics g) {
        float progress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.PANELS)
                ? modalAnimation.value(now()) : 1;
        g.fill(0, 0, width, height, ((int)(0xAA * progress) << 24));
    }

    private void modalButton(GuiGraphics g, int x, int y, String label, boolean primary) {
        g.fill(x, y, x + 82, y + 22, primary ? 0xFF5B4625 : 0xFF28222E);
        g.drawCenteredString(font, label, x + 41, y + 7, primary ? GOLD : TEXT);
    }

    private long now() { return System.currentTimeMillis(); }
    private void triggerActionSweep() {
        actionSweepUntil = now() + categoryDuration(YuanClientPreferences.AnimationCategory.SAVE_STATUS, 420);
    }
    private void renderActionSweep(GuiGraphics g) {
        if (actionSweepUntil <= now() || !preferences.animationEnabled(YuanClientPreferences.AnimationCategory.SAVE_STATUS)) return;
        long duration = Math.max(1, categoryDuration(YuanClientPreferences.AnimationCategory.SAVE_STATUS, 420));
        float progress = 1 - (actionSweepUntil - now()) / (float)duration;
        int left = centerX() + 8;
        int right = centerX() + centerWidth() - 8;
        int sweepX = left + Math.round((right - left) * progress);
        g.fill(sweepX - 14, centerTop(), sweepX, height - 38, 0x33E1B65A);
    }
    private long duration(long smooth) { return preferences.animationDuration(smooth); }
    private long categoryDuration(YuanClientPreferences.AnimationCategory category, long smooth) {
        return preferences.animationEnabled(category) ? duration(smooth) : 0;
    }
    private int animationDistance(int medium) {
        if (preferences.reduceMotion) return 0;
        return switch (preferences.animationIntensity) {
            case LOW -> medium / 2;
            case MEDIUM -> medium;
            case HIGH -> medium * 2;
        };
    }
    private int listAnimationOffset() {
        float progress = preferences.animationEnabled(YuanClientPreferences.AnimationCategory.LISTS)
                ? listAnimation.value(now()) : 1;
        return Math.round((1 - progress) * animationDistance(8));
    }
    private String onOff(boolean value) { return value ? "开启" : "关闭"; }
    private String animationPresetLabel() {
        return switch (preferences.animationPreset) { case OFF -> "关闭"; case LIGHT -> "轻量"; case SMOOTH -> "丝滑"; case ARTIFACT -> "神器"; };
    }
    private String animationCategoryLabel(YuanClientPreferences.AnimationCategory category) {
        return switch (category) {
            case PANELS -> "面板与抽屉"; case LISTS -> "分类与列表"; case CONTROLS -> "控件反馈";
            case VALUES -> "数值与颜色"; case SAVE_STATUS -> "保存与历史"; case PREVIEW -> "ReGlass 预览";
            case DANGER -> "危险能力演出";
        };
    }
    private String intensityLabel() {
        return switch (preferences.animationIntensity) { case LOW -> "低"; case MEDIUM -> "中"; case HIGH -> "高"; };
    }

    private String realCategory() {
        return YuanConfigCatalog.categories().contains(category) ? category : "攻击";
    }

    public static List<String> navigation() {
        List<String> names = new ArrayList<>();
        names.add("收藏");
        names.add("已修改");
        names.addAll(YuanConfigCatalog.categories());
        return List.copyOf(names);
    }


    private String categoryTitle() {
        return switch (category) {
            case "收藏" -> "收藏设置";
            case "已修改" -> "已修改设置";
            default -> category + "配置";
        };
    }

    private String categoryDescription() {
        return switch (realCategory()) {
            case "攻击" -> "控制攻击方式、模式强度和命中特性";
            case "防御" -> "控制无敌、反击与飞行能力";
            case "时停" -> "控制时间停止方式和冻结范围";
            case "清除" -> "管理永久清除与封禁持久化";
            case "数值" -> "调整攻击距离、速度与范围";
            case "液态玻璃" -> "调整 Tooltip 的 ReGlass 光学参数";
            default -> "浏览和调整虚渊能力";
        };
    }

    private String detailLabel() {
        return switch (preferences.detail) { case CONCISE -> "简洁"; case PRACTICAL -> "实用"; case REFERENCE -> "百科"; };
    }

    private YuanConfigLayout layout() { return YuanConfigLayout.of(width, height); }
    private int leftWidth() { return layout().left().right(); }
    private boolean narrow() { return layout().narrow(); }
    private int centerTop() { return layout().center().top(); }
    private int centerListTop() { return centerTop() + 45; }
    private boolean wide() { return layout().wide(); }
    private int rightWidth() { return layout().right().width(); }
    private int rightX() { return layout().right().left(); }
    private int centerX() { return layout().center().left(); }
    private int centerWidth() { return layout().center().width(); }
    private int maxCategoryScroll() {
        int total = 12;
        List<String> names = new ArrayList<>();
        names.addAll(navigation());
        for (String name : names) total += font.width(name) + 18;
        return Math.max(0, total - width + 20);
    }

    private void panel(GuiGraphics g, int x1, int y1, int x2, int y2) {
        g.fill(x1, y1, x2, y2, PANEL);
        g.fill(x1, y1, x2, y1 + 1, BORDER); g.fill(x1, y2 - 1, x2, y2, BORDER);
        g.fill(x1, y1, x1 + 1, y2, BORDER); g.fill(x2 - 1, y1, x2, y2, BORDER);
    }

    private void renderCloseConfirmation(GuiGraphics g) {
        g.fill(0, 0, width, height, 0xAA000000);
        int x = width / 2 - 120, y = height / 2 - 45;
        panel(g, x, y, x + 240, y + 90);
        g.drawCenteredString(font, "存在未保存的修改", width / 2, y + 14, GOLD);
        g.drawCenteredString(font, "保存后关闭，还是放弃修改？", width / 2, y + 31, TEXT);
        g.fill(width / 2 - 75, height / 2 + 17, width / 2 - 7, height / 2 + 37, 0xFF5B4625);
        g.drawCenteredString(font, "保存", width / 2 - 41, height / 2 + 24, GOLD);
        g.fill(width / 2 + 7, height / 2 + 17, width / 2 + 75, height / 2 + 37, 0xFF28222E);
        g.drawCenteredString(font, "放弃", width / 2 + 41, height / 2 + 24, TEXT);
    }

    private static boolean inside(double x, double y, int bx, int by, int bw, int bh) {
        return x >= bx && x < bx + bw && y >= by && y < by + bh;
    }

    private String trim(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) return text;
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private static String format(float value) {
        return value == Math.round(value) ? Integer.toString(Math.round(value)) : String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
