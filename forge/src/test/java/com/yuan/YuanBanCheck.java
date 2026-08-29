package com.yuan;

import com.yuan.data.YuanBanData;

import java.util.UUID;

public final class YuanBanCheck {
    public static void main(String[] args) {
        UUID session = UUID.randomUUID();
        UUID persistent = UUID.randomUUID();

        YuanBanData.clearSession();
        YuanBanData.addSession(session);
        assert YuanBanData.isSessionBanned(session);
        assert !YuanBanData.isSessionBanned(persistent);
        assert YuanBanData.removeSession(session);
        assert !YuanBanData.isSessionBanned(session);

        YuanBanData data = new YuanBanData();
        assert data.addPersistent(persistent);
        assert !data.addPersistent(persistent);
        assert data.isPersistentBanned(persistent);
        assert data.removePersistent(persistent);
        assert !data.isPersistentBanned(persistent);
    }
}
