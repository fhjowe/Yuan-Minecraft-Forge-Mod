package com.yuan.event;

public final class YuanDroppedWeaponProtectionCheck {
    private YuanDroppedWeaponProtectionCheck() {}

    public static void run() {
        assert YuanDroppedWeaponProtection.candidateSafetyDecision(true, true, true, true, true, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(false, true, true, true, true, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(true, false, true, true, true, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(true, true, false, true, true, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(true, true, true, false, true, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(true, true, true, true, false, true);
        assert !YuanDroppedWeaponProtection.candidateSafetyDecision(true, true, true, true, true, false);
        assert YuanDroppedWeaponProtection.rescueResult(true, false)
                == YuanDroppedWeaponProtection.RescueResult.RESCUED;
        assert YuanDroppedWeaponProtection.rescueResult(false, true)
                == YuanDroppedWeaponProtection.RescueResult.HELD;
        assert YuanDroppedWeaponProtection.rescueResult(false, false)
                == YuanDroppedWeaponProtection.RescueResult.RETRY;
        assert YuanDroppedWeaponProtection.runVanillaTick(
                YuanDroppedWeaponProtection.RescueResult.RESCUED);
        assert !YuanDroppedWeaponProtection.runVanillaTick(
                YuanDroppedWeaponProtection.RescueResult.HELD);
        assert !YuanDroppedWeaponProtection.runVanillaTick(
                YuanDroppedWeaponProtection.RescueResult.RETRY);
        assert !YuanDroppedWeaponProtection.holdNoGravity(
                YuanDroppedWeaponProtection.RescueResult.RESCUED);
        assert !YuanDroppedWeaponProtection.holdNoGravity(
                YuanDroppedWeaponProtection.RescueResult.HELD);
        assert YuanDroppedWeaponProtection.holdNoGravity(
                YuanDroppedWeaponProtection.RescueResult.RETRY);
    }
}
