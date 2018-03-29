package audiotest.takeleap.com.playsound;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
                fileOutputStream.write(TestPluginArrayNonStatic(), 0, 100);
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
        byte[] nums = new byte[100];
        for(int i = 0; i < 100; i++)
            nums[i] = (byte)(i + 1);

        return  nums;
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
}
