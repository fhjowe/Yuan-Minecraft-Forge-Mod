package com.yuan.client;

public final class YuanKeyBindingsCheck {
    private YuanKeyBindingsCheck() {}

    public static void check() {
        assert YuanKeyBindings.request(0, false) == YuanKeyBindings.KeyRequest.NONE;
        assert YuanKeyBindings.request(1, false) == YuanKeyBindings.KeyRequest.CONFIG;
        assert YuanKeyBindings.request(1, true) == YuanKeyBindings.KeyRequest.NONE
                : "open screens must drain clicks without requesting a reopen";
    }
}
