package com.yuan.client.cosmic;

public final class YuanIris {

    private YuanIris() {
    }

    public static boolean isShaderPackActive() {
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApi.getMethod("getInstance").invoke(null);
            return (boolean) (Boolean) irisApi.getMethod("isShaderPackInUse").invoke(instance);
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
