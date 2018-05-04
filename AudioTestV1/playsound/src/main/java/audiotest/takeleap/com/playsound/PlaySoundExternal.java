package audiotest.takeleap.com.playsound;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

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
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Madras Games on 27-Mar-18.
 */
public class PlaySoundExternal {

    private static final String TAG = "STREAM_AUDIO";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

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

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private TextureView textureView;
    private String cameraId;
    private Size imageDimension;
    private Context ourContext;

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here

            Log.d(TAG, "OH NO " + textureView.getWidth()  + " " +  textureView.getRight() + " " + textureView.getLeft() + " " + textureView.getTop() + " " + textureView.getBottom());

            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();

            new Thread(new Runnable() {
                public void run() {

                    while (true) {

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        textureView.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream.toByteArray();

//                       Log.d(TAG, "Video Output " + byteArray.length);

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
        public void onDisconnected(CameraDevice camera)
        {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    public  void CameraOpen(Context context)
    {
        ourContext = context;

        //        textureView = (TextureView) findViewById(R.id.textureView);

        textureView = new TextureView(context);
        textureView.setLeft(465);   textureView.setRight(1032);
        textureView.setTop(48);   textureView.setBottom(1644);
        SurfaceTexture mSurface = new SurfaceTexture(0);
        mSurface.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        textureView.setSurfaceTexture(mSurface);
        textureView.setSurfaceTextureListener(textureListener);

        imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1);

//        Log.d(TAG, "OH NO " + textureView.getWidth()  + " " +  textureView.getRight() + " " + textureView.getLeft() + " " + textureView.getTop() + " " + textureView.getBottom());

//        PlaySoundExternal playSoundExternal = new PlaySoundExternal();
//        playSoundExternal.RunProcessSend( 0, this.getApplicationContext());
    }

    protected void createCameraPreview() {
        Log.e(TAG, "createCameraPreview");

        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);
            List surfaces = new ArrayList<>();
            surfaces.add(surface);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(surface);

            Surface readerSurface = imageReader.getSurface();
            surfaces.add(readerSurface);
            captureRequestBuilder.addTarget(readerSurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.e(TAG, "onConfigureFailed");
//                    Toast.makeText(AndroidCameraApi.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {

        CameraManager manager = (CameraManager) ourContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera");
        try {
            cameraId = manager.getCameraIdList()[0];

            Log.e(TAG, "Camera ID " + cameraId);

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(ImageReader.class)[0];
            imageReader = ImageReader.newInstance(  imageDimension.getWidth(),
                    imageDimension.getHeight(),
                    ImageFormat.YUV_420_888, 30);

            ImageReader.OnImageAvailableListener mImageAvailable = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader)
                {
                    Image image = reader.acquireLatestImage();
                    if (image == null)
                        return;


//                    // RowStride of planes may differ from width set to image reader, depends
//                    // on device and camera hardware, for example on Nexus 6P the rowStride is
//                    // 384 and the image width is 352.
                    final Image.Plane[] planes = image.getPlanes();
                    ByteBuffer byteBuffer = planes[0].getBuffer();
                    Log.d(TAG, "Image Available " + byteBuffer.get(400));

//                    final int total = planes[0].getRowStride() * mHeight;
//                    if (mRgbBuffer == null || mRgbBuffer.length < total)
//                        mRgbBuffer = new int[total];
//
//                    getRGBIntFromPlanes(planes);

                    image.close();
                }
            };

            imageReader.setOnImageAvailableListener(mImageAvailable, mBackgroundHandler);

            Log.e(TAG, "Image Dimension " + imageDimension.getWidth() + " " + imageDimension.getHeight());

            // Add permission for camera and let user grant the permission
//            if (ActivityCompat.checkSelfPermission(ourContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ourContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(ourContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }

//            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        Log.d(TAG, "updatePreview");

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    private HandlerThread mBackgroundThread;

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
        Log.d(TAG, "GetInputStream " + (context == null));

        File file = new File(context.getFilesDir() + File.separator + "out.txt");

        Log.d(TAG, file.getAbsolutePath());

        if(file.exists())
        {
            Log.d(TAG, "File is Present " + file.canRead());
        }
        else
        {
            Log.d(TAG, "File is not present");

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Filey " + file.exists());
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

    int numBytesPerReadAudio = 5000;
    public int numBytesReadLastAudio = 0;
    public byte[] audioBuffer = new byte[numBytesPerReadAudio];

    int numBytesPerReadVideo = 15000;
    public int numBytesReadLastVideo = 0;
    public byte[] videoBuffer = new byte[numBytesPerReadVideo];

    public byte[] GetVideoBuffer()
    {
        numBytesReadLastVideo = 0;
        return  videoBuffer;
    }

    public int GetVideoNumReadLast()
    {
        return  numBytesReadLastVideo;
    }

    public InputStream  GetReceiveVideoProcessInputStream()
    {
       if(videoReceiveProcess == null)
           return  null;

       return videoReceiveProcess.getInputStream();
    }

    public InputStream  GetSendVideoProcessInputStream()
    {
        if(videoaudioSendProcess == null)
            return  null;

        return videoaudioSendProcess.getInputStream();
    }

    Process videoReceiveProcess;
    Process audioReceiveProcess;

    Process videoaudioSendProcess;

    ParcelFileDescriptor[] fdPair;

    ParcelFileDescriptor readFD;
    ParcelFileDescriptor writeFD;

    public  void CloseAllProcess()
    {
        if(videoReceiveProcess != null)
            videoReceiveProcess.destroy();

        if(audioReceiveProcess != null)
            audioReceiveProcess.destroy();

        if(videoaudioSendProcess != null)
            videoaudioSendProcess.destroy();
    }

    public void RequestRequiredPermissions(Context context, Activity currentActivity)
    {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET}, REQUEST_CAMERA_PERMISSION);
        }
    }

    public void SendVideoAudioProcess(int caller, Context context)
    {
        fdPair = new ParcelFileDescriptor[0];
        try {
            fdPair = ParcelFileDescriptor.createPipe();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readFD = fdPair[0];
        writeFD = fdPair[1];

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

        String input =  "-y -re -i -" + " -strict -2 -codec:v copy -codec:a aac -b:a 128k -f flv rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live/" +
                        (caller == 1 ? "caller" : "receiver");
        String[] cmds = input.split(" ");
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(context, null)};
        String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);

        videoaudioSendProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                try {
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(videoaudioSendProcess.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        Log.d(TAG, line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        final ParcelFileDescriptor finalreadFD = readFD;

        Thread readerThread = new Thread() {
            @Override
            public void run() {

                byte[] buffer = new byte[8192];
                int read = 0;

                OutputStream ffmpegInput = videoaudioSendProcess.getOutputStream();

                final FileInputStream reader = new FileInputStream(finalreadFD.getFileDescriptor());

                try {

                    while (true) {

                        if (reader.available()>0) {
                            read = reader.read(buffer);
                            ffmpegInput.write(buffer, 0, read);
                        } else {
                            sleep(10);
                        }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();

//                    onDestroy();
                }
            }
        };

        readerThread.start();
    }

    public void ReceiveVideoAudioProcess(int caller, Context context)
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

        audioReceiveProcess = shellCommandCustom.run(command);
//
//        new Thread(new Runnable() {
//            public void run() {
//
//                InitSound();
//
//                InputStream inputStream = audioReceiveProcess.getInputStream();
//
//                while (true) {
//
//                    try {
//
//                        numBytesReadLastAudio = inputStream.read(audioBuffer);
//
//                        SendData(audioBuffer, numBytesReadLastAudio);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

        input = "-y -i rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live" + (caller == 1 ? "/receiver" : "/caller") +
                " -f image2pipe -vcodec mjpeg -";;
        cmds = input.split(" ");
        command = (String[]) this.concatenate(ffmpegBinary, cmds);

        videoReceiveProcess = shellCommandCustom.run(command);
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
