package audiotest.takeleap.com.audiotestv1;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

class UtilCustom {
    UtilCustom() {
    }

    static boolean isDebug(Context context) {
        ApplicationInfo var10001 = context.getApplicationContext().getApplicationInfo();
        return 0 != (var10001.flags &= 2);
    }

    static void close(InputStream inputStream) {
        if(inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    static void close(OutputStream outputStream) {
        if(outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException var2) {
                ;
            }
        }

    }

    static String convertInputStreamToString(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String str;
            while((str = r.readLine()) != null) {
                sb.append(str);
            }

            return sb.toString();
        } catch (IOException var4) {
            Log.d("STREAM_AUDIO", var4.getMessage());
            return null;
        }
    }

    static void destroyProcess(Process process) {
        if(process != null) {
            process.destroy();
        }

    }

    static boolean killAsync(AsyncTask asyncTask) {
        return asyncTask != null && !asyncTask.isCancelled() && asyncTask.cancel(true);
    }

    static boolean isProcessCompleted(Process process) {
        try {
            if(process == null) {
                return true;
            } else {
                process.exitValue();
                return true;
            }
        } catch (IllegalThreadStateException var2) {
            return false;
        }
    }
}
