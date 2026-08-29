package com.yuan.client.gui;

public final class YuanScrollState {
    private float value;
    private float max;
    private float velocity;
    private float visibility;
    private boolean dragging;

    public float value() { return value; }
    public int rounded() { return Math.round(value); }
    public float velocity() { return velocity; }
    public float visibility() { return visibility; }
    public boolean dragging() { return dragging; }

    public void setMax(float max) {
        this.max = Math.max(0, max);
        value = clamp(value);
    }

    public void set(float value) {
        this.value = clamp(value);
        velocity = 0;
        visibility = 1;
    }

    public void wheel(float amount, boolean inertial) {
        visibility = 1;
        if (inertial) velocity += amount * 5;
        else { value = clamp(value + amount * 28); velocity = 0; }
    }

    public void startDrag() { dragging = true; velocity = 0; visibility = 1; }
    public void stopDrag() { dragging = false; }

    public void dragTo(float mouseY, int trackTop, int trackBottom, int thumbHeight) {
        int travel = Math.max(1, trackBottom - trackTop - thumbHeight);
        float ratio = (mouseY - trackTop - thumbHeight / 2f) / travel;
        value = clamp(ratio * max);
        velocity = 0;
        visibility = 1;
    }

    public void tick(boolean animated) { update(animated, 1); }

    public void update(boolean animated, float ticks) {
        if (!animated) velocity = 0;
        if (!dragging && Math.abs(velocity) > .001f) {
            value = clamp(value + velocity * ticks);
            if (value <= 0 || value >= max) velocity = 0;
            else velocity *= (float)Math.pow(.82f, ticks);
        } else if (!dragging) velocity = 0;
        visibility = Math.max(.28f, visibility - (dragging || Math.abs(velocity) > .01f ? 0 : .025f * ticks));
    }

    private float clamp(float value) { return Math.max(0, Math.min(max, value)); }
}
