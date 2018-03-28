package audiotest.takeleap.com.audiotestv1;

import android.content.Context;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Map;

import android.util.*;

/**
 * Created by TakeLeap05 on 28-03-2018.
 */

class FileUtilsCustom {
    static final String ffmpegFileName = "ffmpeg";
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    private static final int EOF = -1;

    FileUtilsCustom() {
    }

    static boolean copyBinaryFromAssetsToData(Context context, String fileNameFromAssets, String outputFileName) {
        File filesDirectory = getFilesDirectory(context);

        try {
            InputStream is = context.getAssets().open(fileNameFromAssets);
            FileOutputStream os = new FileOutputStream(new File(filesDirectory, outputFileName));
            byte[] buffer = new byte[4096];

            int n;
            while(-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
            }

            UtilCustom.close(os);
            UtilCustom.close(is);
            return true;
        } catch (IOException var8) {
            Log.d("STREAM_AUDIO","issue in coping binary from assets to data. ", var8);
            return false;
        }
    }

    static File getFilesDirectory(Context context) {
        return context.getFilesDir();
    }

    static String getFFmpeg(Context context) {

        return getFilesDirectory(context).getAbsolutePath() + File.separator + "ffmpeg";
    }

    static String getFFmpeg2(Context context) {

        return getFilesDirectory(context).getAbsolutePath() + File.separator + "ffmpeg2";
    }

    static String getFFmpeg(Context context, Map<String, String> environmentVars) {
        String ffmpegCommand = "";
        Map.Entry var;
        if(environmentVars != null) {
            for(Iterator var3 = environmentVars.entrySet().iterator(); var3.hasNext(); ffmpegCommand = ffmpegCommand + (String)var.getKey() + "=" + (String)var.getValue() + " ") {
                var = (Map.Entry)var3.next();
            }
        }

        ffmpegCommand = ffmpegCommand + getFFmpeg(context);
        return ffmpegCommand;
    }

    static String SHA1(String file) {
        BufferedInputStream is = null;

        try {
            is = new BufferedInputStream(new FileInputStream(file));
            String var2 = SHA1((InputStream)is);
            return var2;
        } catch (IOException var6) {
          Log.d("STREAM_AUDIO", var6.getMessage());
        } finally {
            UtilCustom.close(is);
        }

        return null;
    }

    static String SHA1(InputStream is) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            byte[] buffer = new byte[4096];

            int read;
            while((read = is.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, read);
            }

            Formatter formatter = new Formatter();
            byte[] var4 = messageDigest.digest();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                byte b = var4[var6];
                formatter.format("%02x", new Object[]{Byte.valueOf(b)});
            }

            String var16 = formatter.toString();
            return var16;
        } catch (NoSuchAlgorithmException var12) {
            Log.d("STREAM_AUDIO", var12.getMessage());
        } catch (IOException var13) {
            Log.d("STREAM_AUDIO", var13.getMessage());
        } finally {
            UtilCustom.close(is);
        }

        return null;
    }
}