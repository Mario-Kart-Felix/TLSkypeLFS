package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

import android.util.*;
import android.os.Build;

class CpuArchHelperCustom {
    CpuArchHelperCustom() {
    }

    static CpuArchCustom getCpuArch() {
        Log.d("STREAM_AUDIO","Build.CPU_ABI : " + Build.CPU_ABI);
        if(!Build.CPU_ABI.equals(getx86CpuAbi()) && !Build.CPU_ABI.equals(getx86_64CpuAbi())) {
            if(Build.CPU_ABI.equals(getArmeabiv7CpuAbi())) {
                ArmArchHelperCustom cpuNativeArchHelper = new ArmArchHelperCustom();
                String archInfo = cpuNativeArchHelper.cpuArchFromJNI();
                if(cpuNativeArchHelper.isARM_v7_CPU(archInfo)) {
                    return CpuArchCustom.ARMv7;
                }
            } else if(Build.CPU_ABI.equals(getArm64CpuAbi())) {
                return CpuArchCustom.ARMv7;
            }

            return CpuArchCustom.NONE;
        } else {
            return CpuArchCustom.x86;
        }
    }

    static String getx86CpuAbi() {
        return "x86";
    }

    static String getx86_64CpuAbi() {
        return "x86_64";
    }

    static String getArm64CpuAbi() {
        return "arm64-v8a";
    }

    static String getArmeabiv7CpuAbi() {
        return "armeabi-v7a";
    }
}
