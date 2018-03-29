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

}
