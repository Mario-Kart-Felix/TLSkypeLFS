package audiotest.takeleap.com.audiotestv1;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission

        // We don't have permission so prompt the user
        ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        File ffmpegFile = new File("/data/user/0/com.takeleap.audiotestv1/files/ffmpeg2");

        if(ffmpegFile.exists())
        {
            Log.e("STREAM_AUDIO", "FFMPEG FILE EXITS " + ffmpegFile.canExecute());
        }
        else
        {
            Log.e("STREAM_AUDIO", "FFMPEG FILE DOES NOT EXITS");

            AssetManager assetManager = this.getAssets();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open("ffmpeg2");
                ffmpegFile.createNewFile();
                ffmpegFile.setExecutable(true);
                out = new FileOutputStream(ffmpegFile);
                copyFile(in, out);
                in.close();
                out.close();
            } catch(IOException e) {
                Log.e("STREAM_AUDIO", "Failed to copy asset file: " + "ffmpeg2", e);
            }
        }

        if(ffmpegFile.exists())
        {
            Log.e("STREAM_AUDIO", "FFMPEG FILE EXITS");
        }
        else
        {
            Log.e("STREAM_AUDIO", "FFMPEG FILE DOES NOT EXITS");
        }

        FFmpegCustom audioFFMPEG = FFmpegCustom.getInstance(this.getApplicationContext()); //new FFmpegCustom(this.getApplicationContext());
        try {
            audioFFMPEG.loadBinary(new LoadBinaryResponseHandler() {
                public void onStart() {
                }

                public void onFinish() {
                }
            });
        } catch (Exception e) {
        }

        Log.d("STREAM_AUDIO", "BEGIN");

        String input = "-y -i rtsp://13.126.154.86:5454/callerAudio.mp3 -f wav -";
        String[] cmd = input.split(" ");

        try {
            audioFFMPEG.execute(cmd, new ExecuteBinaryResponseHandler() {
                public void onStart() {
                    Log.d("STREAM_AUDIO", "ON START");
                }

                public void onProgress(String message) {
                    Log.d("STREAM_AUDIO", "ON PROGRESS " + message);
                }

                public void onFailure(String message) {
                    Log.d("STREAM_AUDIO", "ON FAILURE " + message);
                }

                public void onSuccess(String message) {
                    Log.d("STREAM_AUDIO", "ON SUCCESS " + message);
                }

                public void onFinish() {
                    Log.d("STREAM_AUDIO", "ON FINISH ");
                }
            }            );

        } catch (FFmpegCommandAlreadyRunningException exception) {
            Log.d("STREAM_AUDIO", exception.getMessage());
        }

        Log.d("STREAM_AUDIO", "MIDDLE");

//        FFmpegCustom videoFFMPEG = new FFmpegCustom(this.getApplicationContext());
//        try {
//            videoFFMPEG.loadBinary(new LoadBinaryResponseHandler() {
//                public void onStart() {
//                }
//
//                public void onFinish() {
//                }
//            });
//        } catch (Exception e) {
//        }

//        input = "-y -i rtsp://13.126.154.86:5454/caller.mpeg4 -f image2pipe -vcodec mjpeg -";
//        cmd = input.split(" ");
//
//        try {
//            videoFFMPEG.execute(cmd, new ExecuteBinaryResponseHandler() {
//                        public void onStart() {
//                            Log.d("STREAM_AUDIO", "ON START");
//                        }
//
//                        public void onProgress(String message) {
//                            Log.d("STREAM_AUDIO", "ON PROGRESS " + message);
//                        }
//
//                        public void onFailure(String message) {
//                            Log.d("STREAM_AUDIO", "ON FAILURE " + message);
//                        }
//
//                        public void onSuccess(String message) {
//                            Log.d("STREAM_AUDIO", "ON SUCCESS " + message);
//                        }
//
//                        public void onFinish() {
//                            Log.d("STREAM_AUDIO", "ON FINISH ");
//                        }
//                    }
//                    , "VIDEO FETCH"
//            );
//
//        } catch (FFmpegCommandAlreadyRunningException exception) {
//            Log.d("STREAM_AUDIO", exception.getMessage());
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("STREAM_AUDIO", "Permission: " + permissions[0] + " was " + grantResults[0] + "  " + ActivityCompat.checkSelfPermission(this, permissions[0]));

            //Log.d("STREAM_AUDIO", "is read " + wavFile.canRead());

//                    audioFetchThread = new Thread(AudioFetcher);
//                    audioFetchThread.start();
        }
    }
}

