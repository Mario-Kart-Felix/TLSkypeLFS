package com.TL.openfile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

public class FileOpener
{
    public static String TAG = "FileRead";
    public static String downloadDirectory;
    public static FileOpener m_instance;

    public static FileOpener instance()
    {
        Log.d(TAG, "Instance Called");

//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());


        downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +  "ReceivedFiles" + File.separator;

        if(m_instance == null)
            m_instance = new FileOpener();
        return m_instance;
    }

    public static  String GetDownloadDirectory()
    {
        downloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +  "ReceivedFiles" + File.separator;
        Log.d(TAG, "DownloadDirectory is " + downloadDirectory);
        return downloadDirectory;
    }

    public void OpenFile(String fileName, Context unityContext)
    {
        Toast message2 = Toast.makeText(unityContext, fileName, Toast.LENGTH_LONG);
        message2.show();

        File file = new File(fileName);
        if(file.exists())
        {
            Log.d(TAG, "File exists");
        }
        else
        {
            Log.d(TAG, "File doesn't exist in " + file.getAbsolutePath());
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(Uri.fromFile(file), getMimeType(Uri.fromFile(file), unityContext));

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        unityContext.startActivity(intent);
    }

    private void ShowSystemUI (Context unityContext)
    {

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