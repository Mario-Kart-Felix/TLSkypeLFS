package audiotest.takeleap.com.audiotestv1;

import android.content.Context;
import android.os.AsyncTask;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.concurrent.TimeoutException;

import audiotest.takeleap.com.playsound.PlaySoundExternal;


/**
 * Created by TakeLeap05 on 28-03-2018.
 */

public class FFmpegExecuteAsyncTaskCustom extends AsyncTask<Void, String, CommandResultCustom> {


    class FetchThreadClass implements Runnable {

        PlaySoundExternal playSoundExternal;

        Process targetProcess;
        InputStream inputStream;

        String outputAdd = "";

        FetchThreadClass(Process targetProcess, String outputAdd) {
            this.targetProcess = targetProcess;
            inputStream = this.targetProcess.getInputStream();

            this.outputAdd = outputAdd;

            playSoundExternal = PlaySoundExternal.instance();
            playSoundExternal.InitSound();
        }

        public int unsignedToBytes(byte b) {
            return b & 0xFF;
        }

        public void run() {
            int numBytesPerRead = 5000;
            byte[] buffer = new byte[numBytesPerRead];

            while (true) {
//                Log.d("STREAM_AUDIO", "FetchProcess ");

                try {

                    int numRead = inputStream.read(buffer);

                    //  playSoundExternal.SendData(buffer, numRead);

                    Log.d("STREAM_AUDIO", "Audio Output " + outputAdd + " " + unsignedToBytes(buffer[0]) + " " + unsignedToBytes(buffer[1]) + " " + numRead);

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
    }

    private String[] cmd;
    private FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler;
    private ShellCommandCustom shellCommand;
    private long timeout;
    private long startTime;
    private Process process1;
    private Process process2;
    private String output = "";
    private String outputAdd = "";
    private Context context;


    FFmpegExecuteAsyncTaskCustom(String[] cmd, long timeout, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler, int type, Context context) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommandCustom();
        this.context = context;

        outputAdd = "FIRST";

        if (type == 2) {
            outputAdd = "SECOND";
        }
    }

    protected void onPreExecute() {
        this.startTime = System.currentTimeMillis();
        if (this.ffmpegExecuteResponseHandler != null) {
            this.ffmpegExecuteResponseHandler.onStart();
        }

    }

    public int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    protected CommandResultCustom doInBackground(Void... params) {
        Log.d("STREAM_AUDIO", "DO IN BACKGROUND " + this.cmd[0]);
        CommandResultCustom var2 = new CommandResultCustom(false, "Mss");

        try {

            String input = "-y -i rtsp://13.126.154.86:5454/callerAudio.mp3 -f wav -";
            String[] cmds = input.split(" ");
            String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(this.context, null, 1)};
            String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);
            this.process1 = this.shellCommand.run(command);
//            FetchThreadClass fetchThread1 = new FetchThreadClass(this.process1, "FIRST");
//            fetchThread1.run();

            new Thread(new Runnable() {
                public void run() {
                    int numBytesPerRead = 5000;
                    byte[] buffer = new byte[numBytesPerRead];

                    InputStream inputStreamP1 = process1.getInputStream();
                    while (true) {
                        try {

                            int numRead = inputStreamP1.read(buffer);

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

            input = "-y -i rtsp://13.126.154.86:5454/caller.mpeg4 -f image2pipe -vcodec mjpeg -";
            cmds = input.split(" ");
            ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(this.context, null, 1)};
            command = (String[]) this.concatenate(ffmpegBinary, cmds);
            this.process2 = this.shellCommand.run(command);
//            FetchThreadClass fetchThread2 = new FetchThreadClass(this.process2, "SECOND");
//            fetchThread2.run();

            new Thread(new Runnable() {
                public void run() {
                    int numBytesPerRead = 5000;
                    byte[] buffer = new byte[numBytesPerRead];

                    InputStream inputStreamP2 = process2.getInputStream();
                    while (true) {
                        try {

                            int numRead = inputStreamP2.read(buffer);

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


//            if (this.process1 != null) {
//                Log.d("STREAM_AUDIO", "Running publishing updates method");
//                this.checkAndUpdateProcess();
//            }
//
//            if (this.process2 != null) {
//                Log.d("STREAM_AUDIO", "Running publishing updates method");
//                this.checkAndUpdateProcess();
//            }

        }
//        catch (TimeoutException var8) {
//            Log.d("STREAM_AUDIO", "FFmpeg timed out", var8);
//            CommandResultCustom var3 = new CommandResultCustom(false, var8.getMessage());
//            return var3;
//        }
        catch (Exception var9) {
            Log.d("STREAM_AUDIO", "Error running FFmpeg", var9);
            return CommandResultCustom.getDummyFailureResponse();
        } finally {
           // UtilCustom.destroyProcess(this.process1);
        }

        return var2;
    }

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = ((String[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    protected void onProgressUpdate(String... values) {
        Log.d("STREAM_AUDIO", "onProgressUpdate");
        if (values != null && values[0] != null && this.ffmpegExecuteResponseHandler != null) {
            this.ffmpegExecuteResponseHandler.onProgress(values[0]);
        }

    }

    protected void onPostExecute(CommandResultCustom commandResult) {
        if (this.ffmpegExecuteResponseHandler != null) {
            this.output = this.output + commandResult.output;
            if (commandResult.success) {
                this.ffmpegExecuteResponseHandler.onSuccess(this.output);
            } else {
                this.ffmpegExecuteResponseHandler.onFailure(this.output);
            }

            this.ffmpegExecuteResponseHandler.onFinish();
        }

    }

    private void checkAndUpdateProcess() throws TimeoutException, InterruptedException {
        while (!UtilCustom.isProcessCompleted(this.process1)) {
            if (UtilCustom.isProcessCompleted(this.process1)) {
                return;
            }

            if (this.timeout != 9223372036854775807L && System.currentTimeMillis() > this.startTime + this.timeout) {
                throw new TimeoutException("FFmpeg timed out");
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.process1.getErrorStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (this.isCancelled()) {
                        return;
                    }

                    this.output = this.output + line + "\n";
                    this.publishProgress(new String[]{line});
                }

                Log.d("STREAM_AUDIO", "Updating " + line);
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

    }

    boolean isProcessCompleted() {
        return UtilCustom.isProcessCompleted(this.process1);
    }
}
