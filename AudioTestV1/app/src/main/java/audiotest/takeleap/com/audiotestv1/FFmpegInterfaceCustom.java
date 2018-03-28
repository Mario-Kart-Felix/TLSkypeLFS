package audiotest.takeleap.com.audiotestv1;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.util.Map;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

interface FFmpegInterfaceCustom {
    void loadBinary(FFmpegLoadBinaryResponseHandler var1) throws FFmpegNotSupportedException;

    void execute(Map<String, String> var1, String[] var2, FFmpegExecuteResponseHandler var3, int type) throws FFmpegCommandAlreadyRunningException;

    void execute(String[] var1, FFmpegExecuteResponseHandler var2, int type) throws FFmpegCommandAlreadyRunningException;

    String getDeviceFFmpegVersion() throws FFmpegCommandAlreadyRunningException;

    String getLibraryFFmpegVersion();

    boolean isFFmpegCommandRunning();

    boolean killRunningProcesses();

    void setTimeout(long var1);
}