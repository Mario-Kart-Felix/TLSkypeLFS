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
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };

    File ffmpegFile = new File( getFilesDir() + File.separator + "ffmpeg");


    /**
     * Checks if the app has permission to write to device storage
     * <p/>
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

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = ((String[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);
    }

    public void RunProcess(int caller)
    {
        if(ffmpegFile.exists())
        {
            Log.d("STREAM_AUDIO", "FFMPEG EXISTS");
        }
        else
        {
            Log.d("STREAM_AUDIO", "FFMPEG NOT THERE, CREATING " + ffmpegFile.getAbsolutePath());

            AssetManager assetManager = this.getAssets();
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = assetManager.open("ffmpeg");
                ffmpegFile.createNewFile();
                ffmpegFile.setExecutable(true);
                out = new FileOutputStream(ffmpegFile);
                copyFile(in, out);
                in.close();
                out.close();
            }
            catch (IOException e)
            {
                Log.e("STREAM_AUDIO", "Failed to copy asset file: " + "ffmpeg", e);
            }
        }

        if(!ffmpegFile.exists())
        {
            return;
        }

        ShellCommandCustom shellCommandCustom = new ShellCommandCustom();

        String input = "-y -i rtsp://13.126.154.86:5454/" + (caller == 1 ? "callerAudio.mp3" : "callerAudio.mp3") + " -f wav -";
        String[] cmds = input.split(" ");
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(this.getApplicationContext(), null)};
        String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);

        final Process audioProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                int numBytesPerRead = 5000;
                byte[] buffer = new byte[numBytesPerRead];

                InputStream inputStream = audioProcess.getInputStream();

                while (true) {
                    try {

                        int numRead = inputStream.read(buffer);

                        Log.d("STREAM_AUDIO", "Audio Output " + " " + unsignedToBytes(buffer[0]) + " " + unsignedToBytes(buffer[1]) + " " + numRead);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        input = "-y -i rtsp://13.126.154.86:5454/"  + (caller == 1 ? "caller.mp4" : "caller.mp4") + " -f image2pipe -vcodec mjpeg -";
        cmds = input.split(" ");
        command = (String[]) this.concatenate(ffmpegBinary, cmds);

        final Process videoProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                int numBytesPerRead = 5000;
                byte[] buffer = new byte[numBytesPerRead];

                InputStream inputStream = videoProcess.getInputStream();

                while (true) {
                    try {

                        int numRead = inputStream.read(buffer);

                        Log.d("STREAM_AUDIO", "Video Output " + " " + unsignedToBytes(buffer[0]) + " " + unsignedToBytes(buffer[1]) + " " + numRead);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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

