package com.yuan.client.gui;

import java.util.ArrayList;
import java.util.List;

public record YuanConfigLayout(boolean wide, boolean narrow, Bounds left, Bounds categories,
                               Bounds center, Bounds right, Bounds footer) {
    public record Bounds(int left, int top, int right, int bottom) {
        public int width() { return right - left; }
        public int height() { return bottom - top; }
        public boolean contains(double x, double y) { return x >= left && x < right && y >= top && y < bottom; }
    }

    public record Scrollbar(boolean visible, int trackTop, int trackBottom, int thumbTop, int thumbHeight) {}

    public record RightControls(Bounds undo, Bounds redo, Bounds reset, Bounds copy, Bounds paste,
                                Bounds localSave, Bounds localLoad, Bounds localDelete,
                                Bounds combatDefault, Bounds combatAttack, Bounds combatDefense, Bounds combatTime,
                                Bounds presetList, Bounds rename, Bounds detail, Bounds autoSave,
                                Bounds animation, int contentBottom, int renderUndoY,
                                int renderLocalButtonsY, int renderAnimationY) {
        public int maxScroll(int viewportBottom) { return Math.max(0, contentBottom - viewportBottom); }
    }

    public static YuanConfigLayout of(int width, int height) {
        boolean wide = width >= 760;
        boolean narrow = width < 560;
        int footerTop = height - 34;
        int leftWidth = wide ? 124 : narrow ? 0 : 100;
        int rightWidth = wide ? 224 : 0;
        int categoriesBottom = narrow ? 94 : 27;
        Bounds left = new Bounds(8, 27, 8 + leftWidth, footerTop - 4);
        Bounds categories = new Bounds(narrow ? 10 : 8, narrow ? 54 : 27,
                width - 10, categoriesBottom);
        int centerLeft = narrow ? 8 : left.right() + 8;
        int centerRight = wide ? width - rightWidth - 16 : width - 8;
        Bounds center = new Bounds(centerLeft, categoriesBottom, centerRight, footerTop - 4);
        Bounds right = wide ? new Bounds(width - rightWidth - 8, 27, width - 8, footerTop - 4)
                : new Bounds(width, 27, width, footerTop - 4);
        return new YuanConfigLayout(wide, narrow, left, categories, center, right,
                new Bounds(0, footerTop, width, height));
    }

    public static RightControls rightControls(int x, int y, int width, int scroll, int presetCount) {
        return rightControls(x, y, width, scroll, presetCount, false);
    }

    public static RightControls rightControls(int x, int y, int width, int scroll, int presetCount, boolean glassPresets) {
        int yy = y + 116 + (glassPresets ? 58 : 0) - scroll;
        int undoY = yy + 17;
        Bounds undo = new Bounds(x + 12, undoY, x + 66, undoY + 18);
        Bounds redo = new Bounds(x + 72, undoY, x + 126, undoY + 18);
        Bounds reset = new Bounds(x + 132, undoY, x + 186, undoY + 18);
        yy += 46;
        int exchangeY = yy + 16;
        Bounds copy = new Bounds(x + 12, exchangeY, x + 66, exchangeY + 18);
        Bounds paste = new Bounds(x + 82, exchangeY, x + 136, exchangeY + 18);
        yy += 47;
        int combatY = yy + 16;
        Bounds combatDefault = new Bounds(x + 12, combatY, x + 56, combatY + 18);
        Bounds combatAttack = new Bounds(x + 60, combatY, x + 104, combatY + 18);
        Bounds combatDefense = new Bounds(x + 108, combatY, x + 152, combatY + 18);
        Bounds combatTime = new Bounds(x + 156, combatY, x + 200, combatY + 18);
        yy += 43;
        int localY = yy + 40;
        Bounds localSave = new Bounds(x + 12, localY, x + 64, localY + 18);
        Bounds localLoad = new Bounds(x + 66, localY, x + 118, localY + 18);
        Bounds localDelete = new Bounds(x + 120, localY, x + 172, localY + 18);
        yy += 66;
        int visiblePresets = Math.min(3, presetCount);
        Bounds presetList = new Bounds(x + 10, yy - 3, x + width - 10, yy + visiblePresets * 13);
        yy += visiblePresets * 13 + 4;
        Bounds rename = new Bounds(x + 12, yy, x + 66, yy + 18);
        yy += 22;
        Bounds detail = new Bounds(x + 10, yy + 16, x + width - 10, yy + 28);
        Bounds autoSave = new Bounds(x + 10, yy + 31, x + width - 10, yy + 43);
        Bounds animation = new Bounds(x + 12, yy + 49, x + 66, yy + 67);
        return new RightControls(undo, redo, reset, copy, paste, localSave, localLoad, localDelete,
                combatDefault, combatAttack, combatDefense, combatTime,
                presetList, rename, detail, autoSave, animation, animation.bottom() + scroll,
                undoY, localY, animation.top());
    }

    public static Bounds switchBounds(int controlX, int controlY) {
        return new Bounds(controlX + 76, controlY + 7, controlX + 134, controlY + 24);
    }

    public static Bounds favoriteBounds(int rowRight, int rowTop) {
        return new Bounds(rowRight - 80, rowTop + 2, rowRight - 52, rowTop + 20);
    }

    public static Bounds resetBounds(int rowRight, int rowTop) {
        return new Bounds(rowRight - 42, rowTop + 2, rowRight - 12, rowTop + 20);
    }

    public static Scrollbar scrollbar(int trackTop, int trackBottom, int contentHeight, int scroll) {
        int viewport = Math.max(1, trackBottom - trackTop);
        if (contentHeight <= viewport) return new Scrollbar(false, trackTop, trackBottom, trackTop, viewport);
        int thumbHeight = Math.max(18, viewport * viewport / contentHeight);
        int maxScroll = contentHeight - viewport;
        int travel = viewport - thumbHeight;
        int thumbTop = trackTop + Math.round(travel * Math.max(0, Math.min(maxScroll, scroll)) / (float)maxScroll);
        return new Scrollbar(true, trackTop, trackBottom, thumbTop, thumbHeight);
    }

    public static List<Bounds> glassPresetBounds(int x, int y, int width, int row) {
        List<Bounds> result = new ArrayList<>(7);
        int gap = 3;
        int buttonWidth = Math.max(24, (width - gap * 6) / 7);
        int yy = y + row * 23;
        for (int i = 0; i < 7; i++) {
            int left = x + i * (buttonWidth + gap);
            result.add(new Bounds(left, yy, left + buttonWidth, yy + 19));
        }
        return result;
    }
}
