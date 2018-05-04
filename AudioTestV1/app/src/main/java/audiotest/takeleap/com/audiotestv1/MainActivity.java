package audiotest.takeleap.com.audiotestv1;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import audiotest.takeleap.com.playsound.PlaySoundExternal;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "STREAM_AUDIO";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private TextureView textureView;
    private String cameraId;
    private Size imageDimension;

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

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public void RunProcess()
    {
        File ffmpegFile = new File(  applicationContext.getFilesDir() + File.separator + "ffmpeg");

        if(ffmpegFile.exists())
        {
            Log.d(TAG, "FFMPEG EXISTS");
        }
        else
        {
            Log.d(TAG, "FFMPEG NOT THERE, CREATING " + ffmpegFile.getAbsolutePath());

            AssetManager assetManager = applicationContext.getAssets();
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
                Log.d(TAG, "Failed to copy asset file: " + "ffmpeg", e);
            }
        }

        if(!ffmpegFile.exists())
        {
            return;
        }

        Log.d(TAG, "HERER HERE HERER");

        ShellCommandCustom shellCommandCustom = new ShellCommandCustom();

//        String input = "-y -re -loop 1 -i " + filePath + "/SavedImages/testimage.jpg -t 50 -pix_fmt yuv420p http://13.126.154.86:8090/feed3.ffm";

        String input = "-y -re -i -" + " -strict -2 -codec:v copy -codec:a aac -b:a 128k -f flv rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live/receiver";

//        String input = "-y -re -i -" + " -strict -2 -codec:v copy -codec:a aac -b:a 128k -f flv rtmp://a.rtmp.youtube.com/live2/u79f-7195-97vk-9qe9";

        Log.d(TAG, input);

        String[] cmds = input.split(" ");
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(applicationContext, null)};
        String[] command = (String[]) this.concatenate(ffmpegBinary, cmds);

        final Process videoProcess = shellCommandCustom.run(command);

        new Thread(new Runnable() {
            public void run() {
                try {
                    String line;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(videoProcess.getErrorStream()));
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

                OutputStream ffmpegInput = videoProcess.getOutputStream();

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

        Log.d(TAG, "NOW WHAT");
    }

    public String[] concatenate(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = ((String[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen));
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
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

    private HandlerThread mBackgroundThread;

    private  String filePath;

    private     Context applicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filePath = Environment.getExternalStorageDirectory().getPath();  //this.getApplicationContext().getex.getPath().toString();

        File ourDir = new File(filePath + "/SavedImages");
        if(!ourDir.exists())
        {
            ourDir.mkdir();
        }

        applicationContext = this.getApplicationContext();

        CameraOpen();
    }

    public  void CameraOpen()
    {
    //        textureView = (TextureView) findViewById(R.id.textureView);
        textureView = new TextureView(this.getApplicationContext());
        textureView.setLeft(0);   textureView.setRight(1280);
        textureView.setTop(0);   textureView.setBottom(720);
        SurfaceTexture mSurface = new SurfaceTexture(0);
        mSurface.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        textureView.setSurfaceTexture(mSurface);
        textureView.setSurfaceTextureListener(textureListener);

//        Log.d(TAG, "OH NO " + textureView.getWidth()  + " " +  textureView.getRight() + " " + textureView.getLeft() + " " + textureView.getTop() + " " + textureView.getBottom());

//        PlaySoundExternal playSoundExternal = new PlaySoundExternal();
//        playSoundExternal.RunProcessSend( 0, this.getApplicationContext());
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        Log.e(TAG, "createCameraPreview");

        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface surface = new Surface(texture);
            List surfaces = new ArrayList<>();
            surfaces.add(surface);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            captureRequestBuilder.addTarget(surface);

            Surface readerSurface = mMediaRecorder.getSurface();
            surfaces.add(readerSurface);
            captureRequestBuilder.addTarget(readerSurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();

                    runOnUiThread (new Thread(new Runnable() {
                        public void run() {
                                try {
                                    Thread.sleep(100);

                                    mMediaRecorder.start();
                                }
                                catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                    }));
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    Activity activity = getActivity();
                    Log.d(TAG, "onConfigureFailed");
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private static Size chooseVideoSize(Size[] choices) {

        return  new Size(1280, 720);

//        for (Size size : choices) {
//            Log.d(TAG, "V " +  size.getWidth() + " " + size.getHeight());
//
//            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
//                return size;
//            }
//        }
//        Log.e(TAG, "Couldn't find any suitable video size");
//        return choices[choices.length - 1];
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {

        return  new Size(1280, 720);

        // Collect the supported resolutions that are at least as big as the preview Surface
//        List<Size> bigEnough = new ArrayList<>();
//        int w = aspectRatio.getWidth();
//        int h = aspectRatio.getHeight();
//        for (Size option : choices) {
//            Log.d(TAG, "P " +  option.getWidth() + " " + option.getHeight());
//            if (option.getHeight() == option.getWidth() * h / w &&
//                    option.getWidth() >= width && option.getHeight() >= height) {
//                bigEnough.add(option);
//            }
//        }
//
//        // Pick the smallest of those, assuming we found any
//        if (bigEnough.size() > 0) {
//            return Collections.min(bigEnough, new CompareSizesByArea());
//        } else {
//            Log.e(TAG, "Couldn't find any suitable preview size");
//            return choices[0];
//        }
    }

    int counter = 0;
    boolean startedSending = false;
    private MediaRecorder mMediaRecorder;
    private Size mPreviewSize;
    private Size mVideoSize;

    private void openCamera()
    {
        PlaySoundExternal playSoundExternal = new PlaySoundExternal();
        playSoundExternal.SendVideoAudioProcess(1, this.getApplicationContext());

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera");
        try {
            cameraId = manager.getCameraIdList()[0];

            Log.d(TAG, "Camera ID " + cameraId);

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(ImageReader.class)[0];

            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    textureView.getWidth(), textureView.getHeight(), mVideoSize);

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mMediaRecorder.setOutputFormat(8);

            mMediaRecorder.setOutputFile(writeFD.getFileDescriptor());
            mMediaRecorder.setVideoEncodingBitRate(4500);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(640, 480);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            mMediaRecorder.prepare();

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        Log.d(TAG, "updatePreview");
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
//                Toast.makeText(AndroidCameraApi.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        playSoundExternal.CloseProcess();
//    }
}
