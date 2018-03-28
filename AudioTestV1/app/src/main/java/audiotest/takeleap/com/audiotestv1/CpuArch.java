package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

import android.text.TextUtils;

enum CpuArchCustom {
    x86("0dd4dbad305ff197a1ea9e6158bd2081d229e70e"),
    ARMv7("871888959ba2f063e18f56272d0d98ae01938ceb"),
    NONE((String)null);

    private String sha1;

    private CpuArchCustom(String sha1) {
        this.sha1 = sha1;
    }

    String getSha1() {
        return this.sha1;
    }

    static CpuArchCustom fromString(String sha1) {
        if(!TextUtils.isEmpty(sha1)) {
            CpuArchCustom[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                CpuArchCustom cpuArch = var1[var3];
                if(sha1.equalsIgnoreCase(cpuArch.sha1)) {
                    return cpuArch;
                }
            }
        }

        return NONE;
    }
}
