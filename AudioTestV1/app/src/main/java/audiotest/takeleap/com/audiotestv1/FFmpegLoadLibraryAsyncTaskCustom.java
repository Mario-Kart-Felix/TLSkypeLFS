package audiotest.takeleap.com.audiotestv1;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

class FFmpegLoadLibraryAsyncTaskCustom extends AsyncTask<Void, Void, Boolean> {
    private final String cpuArchNameFromAssets;
    private final FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler;
    private final Context context;

    FFmpegLoadLibraryAsyncTaskCustom(Context context, String cpuArchNameFromAssets, FFmpegLoadBinaryResponseHandler ffmpegLoadBinaryResponseHandler) {
        this.context = context;
        this.cpuArchNameFromAssets = cpuArchNameFromAssets;
        this.ffmpegLoadBinaryResponseHandler = ffmpegLoadBinaryResponseHandler;
    }

    protected Boolean doInBackground(Void... params) {
        File ffmpegFile = new File(FileUtilsCustom.getFFmpeg(this.context));
        if(ffmpegFile.exists() && this.isDeviceFFmpegVersionOld() && !ffmpegFile.delete()) {
            return Boolean.valueOf(false);
        } else {
            if(!ffmpegFile.exists()) {
                boolean isFileCopied = FileUtilsCustom.copyBinaryFromAssetsToData(this.context, this.cpuArchNameFromAssets + File.separator + "ffmpeg", "ffmpeg");
                if(isFileCopied) {
                    if(ffmpegFile.canExecute()) {
                        Log.d("STREAM_AUDIO","FFmpeg is executable");
                        return Boolean.valueOf(true);
                    }

                    Log.d("STREAM_AUDIO","FFmpeg is not executable, trying to make it executable ...");
                    if(ffmpegFile.setExecutable(true)) {
                        return Boolean.valueOf(true);
                    }
                }
            }

            return Boolean.valueOf(ffmpegFile.exists() && ffmpegFile.canExecute());
        }
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if(this.ffmpegLoadBinaryResponseHandler != null) {
            if(isSuccess.booleanValue()) {
                this.ffmpegLoadBinaryResponseHandler.onSuccess();
            } else {
                this.ffmpegLoadBinaryResponseHandler.onFailure();
            }

            this.ffmpegLoadBinaryResponseHandler.onFinish();
        }

    }

    private boolean isDeviceFFmpegVersionOld() {
        return CpuArchCustom.fromString(FileUtilsCustom.SHA1(FileUtilsCustom.getFFmpeg(this.context))).equals(CpuArchCustom.NONE);
    }
}
