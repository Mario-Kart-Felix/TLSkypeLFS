package audiotest.takeleap.com.playsound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Madras Games on 27-Mar-18.
 */
public class PlaySoundExternal {

    AudioTrack audioTrack;

    private static PlaySoundExternal m_instance;

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
