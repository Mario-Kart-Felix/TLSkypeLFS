package audiotest.takeleap.com.audiotestv1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by TakeLeap05 on 30-04-2018.
 */

public final class FileManager {
    public static void writeFrame(String fileName, byte[] data) {
        try {
            File f = new File(fileName);

            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
            bos.write(data);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}