using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Threading;
using System.IO;
using System;
using System.Runtime.InteropServices;


public class TestAndroidPlugin : MonoBehaviour
{
    Thread audioPlayThread;

    byte[] dataRead;
    int totalDataRead = 0;

    AndroidJavaClass androidJavaClass;
    AndroidJavaObject innerInstance;

    private bool dataPresent = false;
    private bool dataStopped = false;

    private bool stopAudio = true;

    private int counter = 0;

    private byte[] buffer;
    private int bufferSize = 5000;

    public AndroidJavaObject inputStream;

    public void InputStreamTest()
    {
        if (Application.platform == RuntimePlatform.Android)
        {
            string strName = "audiotest/takeleap/com/playsound/PlaySoundExternal";
            IntPtr localRef = AndroidJNI.FindClass(strName);

            IntPtr unityClass = AndroidJNI.FindClass("com/unity3d/player/UnityPlayer");
            IntPtr inputStreamClass = AndroidJNI.FindClass("java/io/InputStream");

            if (localRef != IntPtr.Zero)
            {
                print("NOT NULL");

                IntPtr instancePtr = AndroidJNI.CallStaticObjectMethod(localRef, AndroidJNI.GetStaticMethodID(localRef, "instance",
                                                                        "()Laudiotest/takeleap/com/playsound/PlaySoundExternal;"), new jvalue[] { });

                if (instancePtr != null)
                {
                    print("Instance PTR NOT NULL BRO");
                }
                else
                {
                    print("Instance PTR NULL BRO");
                }

                print(AndroidJNI.CallIntMethod(instancePtr, AndroidJNI.GetMethodID(localRef, "TestPluginNonStatic",
                                                                        "()I"), new jvalue[] { }));

                IntPtr currentActivityPtr = AndroidJNI.GetStaticObjectField(unityClass, AndroidJNI.GetStaticFieldID(unityClass, "currentActivity", "Landroid/app/Activity;"));

                IntPtr inputStreamPtr = AndroidJNI.CallObjectMethod(instancePtr, AndroidJNI.GetMethodID(localRef, "GetInputStream",
                                                                        "(Landroid/content/Context;)Ljava/io/InputStream;"),
                                                                        AndroidJNIHelper.CreateJNIArgArray(new object[] { currentActivityPtr }));

                // if (inputStreamPtr != null)
                // {
                //     print("INPUT STREAM NOT NULL");

                //     byte[] buffer = new byte[300];
                //     jvalue[] args = AndroidJNIHelper.CreateJNIArgArray(new object[] { buffer });

                //     int numRead = AndroidJNI.CallIntMethod(inputStreamPtr, AndroidJNI.GetMethodID(inputStreamClass, "read", "([B)I"), args);

                //     print(numRead);
                // }
                // else
                // {
                //     print("INPUT STREAM IS NULL");
                // }

                print("END");
            }
            else
            {
                print("IS NULL");
            }
        }
    }

    void AndroidPluginStart()
    {
        AndroidJavaClass unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaClass pluginClass = new AndroidJavaClass("audiotest.takeleap.com.playsound.PlaySoundExternal");
        AndroidJavaObject pluginObject = pluginClass.CallStatic<AndroidJavaObject>("instance");

        pluginObject.Call(
            "RunProcess",
            1,
            unityClass.GetStatic<AndroidJavaObject>("currentActivity"));
    }

    void Start()
    {
        // InputStreamTest();

        if (Application.platform == RuntimePlatform.Android)
        {
            AndroidPluginStart();
        }

        return;

        audioPlayThread = new Thread(new ThreadStart(AudioPlayUpdate));

        buffer = new byte[bufferSize];

        if (Application.platform == RuntimePlatform.Android)
        {
            androidJavaClass = new AndroidJavaClass("audiotest.takeleap.com.playsound.PlaySoundExternal");
            innerInstance = androidJavaClass.CallStatic<AndroidJavaObject>("instance");

            byte[] nums;// = innerInstance.Call<byte[]>("TestPluginArrayNonStatic");

            // print(nums.Length);

            // for(int i = 0; i < nums.Length; i++)
            // {
            //     print(nums[i]);
            // }


            AndroidJavaClass unityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");

            inputStream = innerInstance.Call<AndroidJavaObject>("GetInputStream", unityClass.GetStatic<AndroidJavaObject>("currentActivity"));

            nums = new byte[200];

            // IntPtr numPtr = AndroidJNI.ToByteArray(nums);

            int numRead = inputStream.Call<int>("read", nums, 0, 200);

            // nums = AndroidJNI.FromByteArray(numPtr);

            print("NUM READ " + numRead + " " + nums[22] + " " + nums[87]);

            innerInstance.Call("InitSound");

            StartCoroutine(TestFile());
        }
    }

    IEnumerator TestFile()
    {
        string filePath = "jar:file://" + Application.dataPath + "!/assets/" + "out.wav";

        WWW www = new WWW(filePath);
        yield return www;

        if (!string.IsNullOrEmpty(www.error))
        {
            print("Can't read");
        }
        else
        {
            totalDataRead = www.bytes.Length;
            dataRead = new byte[totalDataRead];

            print("Exists File " + totalDataRead + " " + www.bytesDownloaded);

            Buffer.BlockCopy(www.bytes, 0, dataRead, 0, totalDataRead);

            stopAudio = false;

            audioPlayThread.Start();
        }
    }

    void AudioPlayUpdate()
    {
        while (true)
        {
            if (dataPresent)
            {
                Thread.Sleep(10);
                continue;
            }

            Buffer.BlockCopy(dataRead, counter, buffer, 0, bufferSize);

            dataPresent = true;
            counter += bufferSize;

            if (counter > totalDataRead)
            {
                dataStopped = true;
                break;
            }
        }
    }

    void Update()
    {
        if (stopAudio)
            return;

        if (dataPresent)
        {
            innerInstance.Call("SendData", new object[] { buffer, bufferSize });
            dataPresent = false;
        }

        if (dataStopped)
        {
            innerInstance.Call("StopSound");

            stopAudio = true;
        }
    }
}
