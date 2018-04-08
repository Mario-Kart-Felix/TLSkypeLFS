package audiotest.takeleap.com.playsound;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Madras Games on 27-Mar-18.
 */
public class PlaySoundExternal {

    AudioTrack audioTrack;

    private static PlaySoundExternal m_instance;

    public InputStream  inputStream;

    public PlaySoundExternal() {
        m_instance = this;
    }

    public static PlaySoundExternal instance() {
        if (m_instance == null) {
            m_instance = new PlaySoundExternal();
        }
        return m_instance;
    }

    public static int TestPlugin()
    {
        return 123;
    }

    public int TestPluginNonStatic()
    {
        return 123;
    }

    public  InputStream GetInputStream(Context context)
    {
        File file = new File(context.getFilesDir() + File.separator + "out.txt");

        Log.d("Unity", file.getAbsolutePath());

        if(file.exists())
        {
            Log.d("Unity", "File is Present " + file.canRead());
        }
        else
        {
            Log.d("Unity", "File is not present");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("Unity", "Filey " + file.exists());
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(TestPluginArrayNonStatic(), 0, 123);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return  inputStream;
    }

    public byte[] TestPluginArrayNonStatic()
    {
        byte[] nums = new byte[123];
        for(int i = 0; i < 123; i++)
            nums[i] = (byte)(i + 1);

        return  nums;
    }

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = ((String[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void RunProcess(int caller, Context context)
    {
        File ffmpegFile = new File(  context.getFilesDir() + File.separator + "ffmpeg");

        if(ffmpegFile.exists())
        {
            Log.d("STREAM_AUDIO", "FFMPEG EXISTS");
        }
        else
        {
            Log.d("STREAM_AUDIO", "FFMPEG NOT THERE, CREATING " + ffmpegFile.getAbsolutePath());

            AssetManager assetManager = context.getAssets();
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
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(context, null)};
        String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);

        final Process audioProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                int numBytesPerRead = 5000;
                byte[] buffer = new byte[numBytesPerRead];

                InitSound();

                InputStream inputStream = audioProcess.getInputStream();

                while (true) {
                    try {

                        int numRead = inputStream.read(buffer);

                        SendData(buffer, numRead);

                        Log.d("STREAM_AUDIO", "Audio Output " + " " + unsignedToBytes(buffer[0]) + " " + unsignedToBytes(buffer[1]) + " " + numRead);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();

        input = "-y -i rtsp://13.126.154.86:5454/"  + (caller == 1 ? "caller.mpeg4" : "caller.mpeg4") + " -f image2pipe -vcodec mjpeg -";
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

//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();
    }

    public void InitSound()
    {
        int outputBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, outputBufferSize, AudioTrack.MODE_STREAM);

        audioTrack.play();
    }

    public void SendData(byte[] data, int length)
    {
        audioTrack.write(data, 0, length);
    }

    public void StopSound()
    {
        audioTrack.stop();
        audioTrack.release();
    }

    public void RunProcessSend(int caller, Context context)
    {
        File ffmpegFile = new File(  context.getFilesDir() + File.separator + "ffmpeg");

        if(ffmpegFile.exists())
        {
            Log.d("STREAM_AUDIO", "FFMPEG EXISTS");
        }
        else
        {
            Log.d("STREAM_AUDIO", "FFMPEG NOT THERE, CREATING " + ffmpegFile.getAbsolutePath());

            AssetManager assetManager = context.getAssets();
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

        String input = "-y -re -rtbufsize 100M -f dv1394 -i /dev/dv1394/0 http://13.126.154.86:8090/feed1.ffm";

//        input = "-devices";
        String[] cmds = input.split(" ");
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(context, null)};
        String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);

        final Process sendProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                try {
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(sendProcess.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        Log.d("STREAM_AUDIO", line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
