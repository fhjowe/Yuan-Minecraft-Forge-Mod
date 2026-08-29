package com.yuan.client.gui;

public final class YuanUiAnimation {
    public enum Easing { LINEAR, SMOOTH, OUT_BACK }

    private YuanUiAnimation() {}

    public static final class Track {
        private float start;
        private float target;
        private long startTime;
        private long duration;
        private Easing easing = Easing.SMOOTH;

        public Track(float value) { start = target = value; }

        public void to(float target, long now, long duration, Easing easing) {
            start = value(now);
            this.target = target;
            this.startTime = now;
            this.duration = Math.max(0, duration);
            this.easing = easing;
        }

        public float value(long now) {
            if (duration == 0 || now >= startTime + duration) return target;
            float t = Math.max(0, Math.min(1, (now - startTime) / (float)duration));
            float eased = switch (easing) {
                case LINEAR -> t;
                case SMOOTH -> t * t * (3 - 2 * t);
                case OUT_BACK -> {
                    float c1 = 1.2f, c3 = c1 + 1;
                    float p = t - 1;
                    yield 1 + c3 * p * p * p + c1 * p * p;
                }
            };
            return start + (target - start) * eased;
        }

        public boolean active(long now) { return duration > 0 && now < startTime + duration; }
        public float target() { return target; }
    }
}
