using System.IO;
using System;
using UnityEngine.UI;
using System.Threading;
using System.Runtime.InteropServices;
using System.Net.Sockets;
using System.Collections.Specialized;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using System.Collections;
using UnityEngine.Video;
using UnityEngine.Profiling;
using UnityEngine;

public class StreamSender
{
    int numDataPerRead = 100000;

    byte[] tempData = new byte[100000];
    byte[] data;

    int count = 0;

    byte[] pattern;

    byte[] newData;

    public RawImage targetImage;
    private Texture2D targetTexture;

    [DllImport("msvcrt.dll", SetLastError = true, CharSet = CharSet.Auto)]
    public static extern int memcmp(byte[] b1, byte[] b2, long count);

    AndroidJavaClass unityClass;
    AndroidJavaClass pluginClass;
    AndroidJavaObject pluginObject;
    AndroidJavaClass inputStreamClass;

    Thread renderThread;

    public StreamSender(StreamReader stream, RawImage targetImage, Vector2 textureSize)
    {
        this.targetImage = targetImage;

        targetTexture = new Texture2D((int)textureSize.x, (int)textureSize.y, TextureFormat.RGB24, false);

        newData = new byte[numDataPerRead];

        unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        pluginClass = new AndroidJavaClass("audiotest.takeleap.com.playsound.PlaySoundExternal");
        pluginObject = pluginClass.CallStatic<AndroidJavaObject>("instance");
        inputStreamClass = new AndroidJavaClass("java.io.InputStream");
    }

    public void StartSendingStream()
    {
        pattern = new byte[2];
        pattern[0] = 255;
        pattern[1] = 217;

        renderThread = new Thread(new ThreadStart(AndroidSendUpdate));

        renderThread.Priority = System.Threading.ThreadPriority.Highest;
        renderThread.Start();
    }

    public void AndroidSendUpdate()
    {
        AndroidJNI.AttachCurrentThread();

        IntPtr instancePtr = AndroidJNI.CallStaticObjectMethod(pluginClass.GetRawClass(), AndroidJNI.GetStaticMethodID(pluginClass.GetRawClass(), "instance",
                                                                    "()Laudiotest/takeleap/com/playsound/PlaySoundExternal;"), new jvalue[] { });

        IntPtr currentActivityPtr = AndroidJNI.GetStaticObjectField(unityClass.GetRawClass(), AndroidJNI.GetStaticFieldID(unityClass.GetRawClass(), "currentActivity", "Landroid/app/Activity;"));

        AndroidJNI.CallVoidMethod(instancePtr, AndroidJNI.GetMethodID(pluginClass.GetRawClass(), "SendVideoAudioProcess",
                                                                "(ILandroid/content/Context;)V"),
                                                                new jvalue[] { new jvalue() { i = SkypeManager.Instance.isCaller ? 1 : 2 }, new jvalue() { l = currentActivityPtr } });

        IntPtr inputStreamPtr = AndroidJNI.CallObjectMethod(instancePtr, AndroidJNI.GetMethodID(pluginClass.GetRawClass(), "GetSendVideoProcessInputStream",
                                                                "()Ljava/io/InputStream;"),
                                                                new jvalue[] { });

        IntPtr numsPtr = AndroidJNI.ToByteArray(newData);

        while (true)
        {
            int bytesRead = numDataPerRead;

            bytesRead = AndroidJNI.CallIntMethod(inputStreamPtr, AndroidJNI.GetMethodID(inputStreamClass.GetRawClass(), "read",
                                                                "([BII)I"),
                                                                new jvalue[] { new jvalue() { l = numsPtr }, new jvalue() { i = 0 }, new jvalue() { i = numDataPerRead } });

            newData = AndroidJNIHelper.ConvertFromJNIArray<byte[]>(numsPtr);

            int index = SearchBytePatternAndroid();

			Debug.Log("NNN " + bytesRead + " " + index);

            if (index != -1)
            {
                Buffer.BlockCopy(newData, 0, tempData, count, index);
                count += index;

                data = new byte[count];
                Buffer.BlockCopy(tempData, 0, data, 0, count);

                index += 2;

                Buffer.BlockCopy(newData, index, tempData, 0, bytesRead - index);
                count = bytesRead - index;
            }
            else
            {
                Buffer.BlockCopy(newData, 0, tempData, count, bytesRead);
                count += bytesRead;
            }
        }

        AndroidJNI.DeleteLocalRef(numsPtr);
    }


    public int SearchBytePatternAndroid()
    {
        int patternLength = pattern.Length;
        int totalLength = newData.Length;
        byte firstMatchByte = pattern[0];

        // Debug.Log(newData[0] + " " + newData[1]);

        for (int i = 0; i < totalLength; i++)
        {
            if (firstMatchByte == newData[i] && totalLength - i >= patternLength)
            {
                byte[] match = new byte[patternLength];
                Buffer.BlockCopy(newData, i, match, 0, patternLength);
                if (StructuralComparisons.StructuralEqualityComparer.Equals(match, pattern))
                {
                    return i;
                }
            }
        }

        return -1;
    }

    public void DrawFrame()
    {
        if (data != null)
        {
            targetTexture.LoadImage(data);
            targetTexture.Apply();

            targetImage.texture = targetTexture;

            data = null;
        }
    }

    public void AbortThread()
    {
        if (renderThread != null)
            renderThread.Abort();
    }
}
