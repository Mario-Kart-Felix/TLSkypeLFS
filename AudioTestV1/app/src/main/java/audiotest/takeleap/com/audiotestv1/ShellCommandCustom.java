package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

import android.util.Log;

import java.io.IOException;

class ShellCommandCustom {
    ShellCommandCustom() {
    }

    Process run(String[] commandString) {
        Process process = null;

        try {
            process = Runtime.getRuntime().exec(commandString);
        } catch (IOException var4) {
            Log.d("STREAM_AUDIO", "Exception while trying to run: " + commandString, var4);
        }

        return process;
    }

    CommandResultCustom runWaitFor(String[] s) {
        Process process = this.run(s);
        Integer exitValue = null;
        String output = null;

        try {
            if(process != null) {
                exitValue = Integer.valueOf(process.waitFor());
                if(CommandResultCustom.success(exitValue)) {
                    output = UtilCustom.convertInputStreamToString(process.getInputStream());
                } else {
                    output = UtilCustom.convertInputStreamToString(process.getErrorStream());
                }
            }
        } catch (InterruptedException var9) {
            Log.d("STREAM_AUDIO", "Interrupt exception", var9);
        } finally {
            UtilCustom.destroyProcess(process);
        }

        return new CommandResultCustom(CommandResultCustom.success(exitValue), output);
    }
}
