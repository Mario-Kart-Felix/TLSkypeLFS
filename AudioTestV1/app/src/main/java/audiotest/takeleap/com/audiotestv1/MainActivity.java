package audiotest.takeleap.com.audiotestv1;

import android.Manifest;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
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
import java.util.List;


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

    public void RunProcess(Context context)
    {
        File ffmpegFile = new File(  context.getFilesDir() + File.separator + "ffmpeg");

        if(ffmpegFile.exists())
        {
            Log.d(TAG, "FFMPEG EXISTS");
        }
        else
        {
            Log.d(TAG, "FFMPEG NOT THERE, CREATING " + ffmpegFile.getAbsolutePath());

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
                Log.d(TAG, "Failed to copy asset file: " + "ffmpeg", e);
            }
        }

        if(!ffmpegFile.exists())
        {
            return;
        }

        Log.d(TAG, "HERER HERE HERER");

        ShellCommandCustom shellCommandCustom = new ShellCommandCustom();

        String input = "-y -re -loop 1 -i " + filePath + "/SavedImages/testimage.jpg -t 50 -pix_fmt yuv420p http://13.126.154.86:8090/feed3.ffm";

//        String input = "-formats";

        Log.d(TAG, input);

        String[] cmds = input.split(" ");
        String[] ffmpegBinary = new String[]{FileUtilsCustom.getFFmpeg(context, null)};
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

//        playSoundExternal = new PlaySoundExternal();
//        playSoundExternal.RunProcess(0, getApplicationContext());

        applicationContext = this.getApplicationContext();

        CameraOpen();
    }

    public  void CameraOpen()
    {
    //        textureView = (TextureView) findViewById(R.id.textureView);
        textureView = new TextureView(this.getApplicationContext());
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

    int counter = 0;
    boolean startedSending = false;

    private void openCamera() {


        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera");
        try {
            cameraId = manager.getCameraIdList()[0];

            Log.d(TAG, "Camera ID " + cameraId);


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

                    byte[] jpegData = ImageUtils.imageToByteArray(image);

                    FileManager.writeFrame( filePath + "/SavedImages/testimage.jpg", jpegData);
                    image.close();

                    counter += 1;
                    Log.d(TAG, "One Written " + counter);

                    if(!startedSending)
                    {
                        RunProcess(applicationContext);
                        startedSending = true;
                    }
                }
            };

            imageReader.setOnImageAvailableListener(mImageAvailable, mBackgroundHandler);

            Log.e(TAG, "Image Dimension " + imageDimension.getWidth() + " " + imageDimension.getHeight());

            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            manager.openCamera(cameraId, stateCallback, null);

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
