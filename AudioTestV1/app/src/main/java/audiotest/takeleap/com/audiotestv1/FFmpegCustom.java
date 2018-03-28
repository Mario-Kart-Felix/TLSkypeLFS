package audiotest.takeleap.com.audiotestv1;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.*;

import com.github.hiteshsondhi88.libffmpeg.*;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Map;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

public class FFmpegCustom implements FFmpegInterfaceCustom {
    private final Context context;
    private FFmpegExecuteAsyncTaskCustom ffmpegExecuteAsyncTask;
    private FFmpegLoadLibraryAsyncTaskCustom ffmpegLoadLibraryAsyncTask;
    private static final long MINIMUM_TIMEOUT = 10000L;
    private long timeout = 9223372036854775807L;
    private static FFmpegCustom instance = null;

    public FFmpegCustom(Context context) {
        this.context = context;
        //Log.setDEBUG(Util.isDebug(this.context));
    }

    public static FFmpegCustom getInstance(Context context) {
        if(instance == null) {
            instance = new FFmpegCustom(context);
        }

        return instance;
    }

    public void loadBinary(FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler) throws FFmpegNotSupportedException {
        String cpuArchNameFromAssets = null;
//        switch(null.$SwitchMap$com$github$hiteshsondhi88$libffmpeg$CpuArch[CpuArchHelperCustom.getCpuArch().ordinal()]) {
//            case 1:
//                Log.d("STREAM_AUDIO","Loading FFmpeg for x86 CPU");
//                cpuArchNameFromAssets = "x86";
//                break;
//            case 2:
//                Log.d("STREAM_AUDIO","Loading FFmpeg for armv7 CPU");
//
//                break;
//            case 3:
//                throw new FFmpegNotSupportedException("Device not supported");
//        }

        cpuArchNameFromAssets = "armeabi-v7a";

        if(!TextUtils.isEmpty(cpuArchNameFromAssets)) {
            this.ffmpegLoadLibraryAsyncTask = new FFmpegLoadLibraryAsyncTaskCustom(this.context, cpuArchNameFromAssets, ffmpegLoadBinaryResponseHandler);
            this.ffmpegLoadLibraryAsyncTask.execute(new Void[0]);
        } else {
            throw new FFmpegNotSupportedException("Device not supported");
        }
    }

    public void execute(Map<String, String> environvenmentVars, String[] cmd, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler) throws FFmpegCommandAlreadyRunningException {
        if(this.ffmpegExecuteAsyncTask != null && !this.ffmpegExecuteAsyncTask.isProcessCompleted())
        {
            throw new FFmpegCommandAlreadyRunningException("FFmpeg command is already running, you are only allowed to run single command at a time");
        }
        else if(cmd.length != 0)
        {
            String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(this.context, environvenmentVars)};

            Log.d("STREAM_AUDIO", FileUtilsCustom.getFFmpeg(this.context, environvenmentVars));

            String[] command = (String[])this.concatenate(ffmpegBinary, cmd);

            this.ffmpegExecuteAsyncTask = new FFmpegExecuteAsyncTaskCustom(command, this.timeout, ffmpegExecuteResponseHandler);
            this.ffmpegExecuteAsyncTask.execute(new Void[0]);
        } else {
            throw new IllegalArgumentException("shell command cannot be empty");
        }
    }

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = ((String[])Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public void execute(String[] cmd, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler) throws FFmpegCommandAlreadyRunningException {
        this.execute((Map)null, cmd, ffmpegExecuteResponseHandler);
    }

    public String getDeviceFFmpegVersion() throws FFmpegCommandAlreadyRunningException {
        ShellCommandCustom shellCommand = new ShellCommandCustom();
        CommandResultCustom commandResult = shellCommand.runWaitFor(new String[]{FileUtilsCustom.getFFmpeg(this.context), "-version"});
        return commandResult.success?commandResult.output.split(" ")[2]:"";
    }

    public String getLibraryFFmpegVersion() {
        return this.context.getString(com.github.hiteshsondhi88.libffmpeg.R.string.shipped_ffmpeg_version);
    }

    public boolean isFFmpegCommandRunning() {
        return this.ffmpegExecuteAsyncTask != null && !this.ffmpegExecuteAsyncTask.isProcessCompleted();
    }

    public boolean killRunningProcesses() {
        return UtilCustom.killAsync(this.ffmpegLoadLibraryAsyncTask) || UtilCustom.killAsync(this.ffmpegExecuteAsyncTask);
    }

    public void setTimeout(long timeout) {
        if(timeout >= 10000L) {
            this.timeout = timeout;
        }
    }
}
