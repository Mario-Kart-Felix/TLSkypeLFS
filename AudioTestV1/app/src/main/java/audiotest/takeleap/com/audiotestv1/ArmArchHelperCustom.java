package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

class ArmArchHelperCustom {
    ArmArchHelperCustom() {
    }

    native String cpuArchFromJNI();

    boolean isARM_v7_CPU(String cpuInfoString) {
        return cpuInfoString.contains("v7");
    }

    boolean isNeonSupported(String cpuInfoString) {
        return cpuInfoString.contains("-neon");
    }

    static {
        System.loadLibrary("ARM_ARCH");
    }
}