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

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

public class MainActivity extends AppCompatActivity {

    AudioTrack audioTrack;

    Thread audioFetchThread;

    AssetManager assetMgr;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };


    Runnable AudioFetcher = new Runnable()
    {
        public void run()
        {
            byte[] data = new byte[1000];
            int i = 0;

            while(true)
            {
                audioTrack.play();

                try {
                    AssetFileDescriptor assetFileDescriptor = assetMgr.openFd("out.wav");
                    FileInputStream fileInputStream = assetFileDescriptor.createInputStream();
                    DataInputStream dis = new DataInputStream(fileInputStream);

                    audioTrack.play();
                    while((i = dis.read(data, 0, 1000)) > -1){
                        audioTrack.write(data, 0, i);
                    }

                    audioTrack.stop();
                    audioTrack.release();
                    dis.close();
                    fileInputStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    /**
     * Checks if the app has permission to write to device storage
     *
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(this);

        int outputBufferSize = AudioTrack.getMinBufferSize(41000,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 41000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, outputBufferSize, AudioTrack.MODE_STREAM);

        assetMgr = this.getAssets();


//        File wavFile = new File(Environment.getExternalStorageDirectory() + File.separator + "out.wav");
//        wavFile.mkdirs();
//
//        try {
//            if(wavFile.exists())
//            {
//                Log.d("STREAM_AUDIO", "FIle PResent");
//            }
//            else
//            {
//                Log.d("STREAM_AUDIO", "Creating File");
//                wavFile.createNewFile();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

        FFmpeg ffmpeg = FFmpeg.getInstance(this.getApplicationContext());
        try
        {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler()
            {
                public void onStart()
                {
                }

                public void onFinish()
                {
                }
            });
        }
        catch (Exception e)
        {
        }

        Log.d("STREAM_AUDIO", "BEGIN");

            String filePath = Environment.getExternalStorageDirectory() + File.separator + "icreated.wav";
//            File wavFile = new File(filePath);
//            wavFile.createNewFile();

            String input = "-y -i rtsp://13.126.154.86:5454/callerAudio.mp3 -f wav -";
            String[] cmd = input.split(" ");

            try
            {
                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
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
                });

            }
            catch (FFmpegCommandAlreadyRunningException exception)
            {
                Log.d("STREAM_AUDIO", exception.getMessage());
            }

            Log.d("STREAM_AUDIO", "MIDDLE");

            input = "-y -re -rtbufsize 1024M -i c:\\out.mp4 -an -f rtp rtp://13.126.154.86:5454/feed1.ffm";
            cmd = input.split(" ");

        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
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
            });
        }
        catch (FFmpegCommandAlreadyRunningException exception)
        {
            Log.d("STREAM_AUDIO", "U KNOW WHAT");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    Log.d("STREAM_AUDIO", "Permission: " + permissions[0] + " was "+ grantResults[0] + "  "+ ActivityCompat.checkSelfPermission(this, permissions[0]));



                    //Log.d("STREAM_AUDIO", "is read " + wavFile.canRead());

//                    audioFetchThread = new Thread(AudioFetcher);
//                    audioFetchThread.start();
                }
      }
}

