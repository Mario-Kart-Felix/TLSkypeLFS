package com.example.openfilelibtest;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    static String TAG = "FileRead";

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            Toast message = Toast.makeText(getApplicationContext(), "Hello World", Toast.LENGTH_LONG);
            message.show();

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
//        String[] perms = {"android.permission.FINE_LOCATION", "android.permission.CAMERA", "android"};

//        int permsRequestCode = 200;
//        requestPermissions(perms, permsRequestCode);
            Button testButton = findViewById(R.id.testButton);

            testButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Toast message2 = Toast.makeText(getApplicationContext(), "Button Test", Toast.LENGTH_LONG);
                    message2.show();

                    try
                    {
                        OpenFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + File.separator +  "brand.jpg");
                    }
                    catch(RuntimeException e)
                    {
                        Log.e(TAG, "RuntimeException = " + e.getMessage());
                    }

                }
            });
        }

        public void OpenFile(String fileName)
        {
            File file = new File(fileName);
            Log.d(TAG, "Reached Line");
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", file);
            Log.d(TAG, photoURI.getPath() + "Path");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(photoURI, getMimeType(Uri.fromFile(file), getApplicationContext()));
            startActivity(intent);
        }

        private String getMimeType(Uri uri, Context unityContext)
        {
            String mimeType;
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT))
            {
                ContentResolver cr = unityContext.getContentResolver();
                mimeType = cr.getType(uri);
            }
            else
            {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }

            return mimeType;
        }
}
