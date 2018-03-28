package audiotest.takeleap.com.audiotestv1;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

import android.os.ParcelUuid;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import audiotest.takeleap.com.playsound.PlaySoundExternal;


/**
 * Created by TakeLeap05 on 28-03-2018.
 */

public class FFmpegExecuteAsyncTaskCustom extends AsyncTask<Void, String, CommandResultCustom> {


    class FetchThreadClass extends Thread {

        PlaySoundExternal playSoundExternal;

        Process targetProcess;
        InputStream  inputStream;

        String outputAdd = "";

        FetchThreadClass(Process targetProcess, String outputAdd) {
            this.targetProcess = targetProcess;
            inputStream = this.targetProcess.getInputStream();

            this.outputAdd = outputAdd;

            playSoundExternal = PlaySoundExternal.instance();
            playSoundExternal.InitSound();
        }

        public  int unsignedToBytes(byte b) {
            return b & 0xFF;
        }

        public void run()
        {
            int numBytesPerRead = 5000;
            byte[] buffer = new byte[numBytesPerRead];

            while (true)
            {
//                Log.d("STREAM_AUDIO", "FetchProcess ");

                try {

                    int numRead = inputStream.read(buffer);

                  //  playSoundExternal.SendData(buffer, numRead);

                    Log.d("STREAM_AUDIO", "Audio Output "  + outputAdd + " " + unsignedToBytes(buffer[0]) + " " + unsignedToBytes(buffer[1]) + " " + numRead);

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

    private  String[] cmd;
    private  FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler;
    private  ShellCommandCustom shellCommand;
    private  long timeout;
    private long startTime;
    private Process process;
    private String output = "";
    private String outputAdd = "";


    FFmpegExecuteAsyncTaskCustom(String[] cmd, long timeout, FFmpegExecuteResponseHandler ffmpegExecuteResponseHandler, int type) {
        this.cmd = cmd;
        this.timeout = timeout;
        this.ffmpegExecuteResponseHandler = ffmpegExecuteResponseHandler;
        this.shellCommand = new ShellCommandCustom();

        outputAdd = "FIRST";

        if(type == 2)
        {
            outputAdd = "SECOND";
        }
    }

    protected void onPreExecute() {
        this.startTime = System.currentTimeMillis();
        if(this.ffmpegExecuteResponseHandler != null) {
            this.ffmpegExecuteResponseHandler.onStart();
        }

    }

    protected CommandResultCustom doInBackground(Void... params) {
        Log.d("STREAM_AUDIO", "DO IN BACKGROUND");

                CommandResultCustom var2;
        try {

            this.process = this.shellCommand.run(this.cmd);

            FetchThreadClass fetchThread = new FetchThreadClass(this.process, outputAdd);
            fetchThread.run();

            if(this.process != null) {
                Log.d("STREAM_AUDIO", "Running publishing updates method");
                this.checkAndUpdateProcess();
                var2 = CommandResultCustom.getOutputFromProcess(this.process);
                return var2;
            }

            var2 = CommandResultCustom.getDummyFailureResponse();
        } catch (TimeoutException var8) {
            Log.d("STREAM_AUDIO","FFmpeg timed out", var8);
            CommandResultCustom var3 = new CommandResultCustom(false, var8.getMessage());
            return var3;
        } catch (Exception var9) {
            Log.d("STREAM_AUDIO","Error running FFmpeg", var9);
            return CommandResultCustom.getDummyFailureResponse();
        } finally {
            UtilCustom.destroyProcess(this.process);
        }

        return var2;
    }

    protected void onProgressUpdate(String... values) {
        Log.d("STREAM_AUDIO","onProgressUpdate");
        if(values != null && values[0] != null && this.ffmpegExecuteResponseHandler != null) {
            this.ffmpegExecuteResponseHandler.onProgress(values[0]);
        }

    }

    protected void onPostExecute(CommandResultCustom commandResult) {
        if(this.ffmpegExecuteResponseHandler != null) {
            this.output = this.output + commandResult.output;
            if(commandResult.success) {
                this.ffmpegExecuteResponseHandler.onSuccess(this.output);
            } else {
                this.ffmpegExecuteResponseHandler.onFailure(this.output);
            }

            this.ffmpegExecuteResponseHandler.onFinish();
        }

    }

    private void checkAndUpdateProcess() throws TimeoutException, InterruptedException {
        while(!UtilCustom.isProcessCompleted(this.process)) {
            if(UtilCustom.isProcessCompleted(this.process)) {
                return;
            }

            if(this.timeout != 9223372036854775807L && System.currentTimeMillis() > this.startTime + this.timeout) {
                throw new TimeoutException("FFmpeg timed out");
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));

                String line;
                while((line = reader.readLine()) != null) {
                    if(this.isCancelled()) {
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
        return UtilCustom.isProcessCompleted(this.process);
    }
}
