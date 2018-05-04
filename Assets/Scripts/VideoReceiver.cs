using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Diagnostics;
using System.IO;
using System;
using FFmpegOut;
using UnityEngine.UI;
using System.Threading;
using System.Runtime.InteropServices;
using System.Net.Sockets;
using System.Collections.Specialized;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using UnityEngine.Video;

public class VideoReceiver : MonoBehaviour
{
    Process receiveProcess;

    private string ffmpegPath = "";

    public RawImage receiverImage;
    public Vector2 textureSize;


    private StreamReceiver streamReceiver;

    private SoundStreamReceiver soundStreamReceiver;

    public void StartVideoReceiver()
    {
        Application.runInBackground = true;

        ffmpegPath = FFmpegConfig.BinaryPath;

        if (Application.platform == RuntimePlatform.Android)
        {
            ReceiveVideoStream_Android();
        }
        else
        {
            ReceiveVideoStream();
        }
    }

    unsafe void ReceiveVideoStream_Android()
    {
        streamReceiver = new StreamReceiver(null, receiverImage, textureSize, false);
        streamReceiver.StartReceivingStream();

        return;

        string strName = "audiotest/takeleap/com/playsound/PlaySoundExternal";
        IntPtr localRefPtr = AndroidJNI.FindClass(strName);

        IntPtr unityClassPtr = AndroidJNI.FindClass("com/unity3d/player/UnityPlayer");
        IntPtr inputStreamClassPtr = AndroidJNI.FindClass("java/io/InputStream");

        if (localRefPtr != IntPtr.Zero)
        {
            print("NOT NULL");

            IntPtr instancePtr = AndroidJNI.CallStaticObjectMethod(localRefPtr, AndroidJNI.GetStaticMethodID(localRefPtr, "instance",
                                                                    "()Laudiotest/takeleap/com/playsound/PlaySoundExternal;"), new jvalue[] { });

            if (instancePtr != null)
            {
                print("Instance PTR NOT NULL BRO");
            }
            else
            {
                print("Instance PTR NULL BRO");
            }

            // IntPtr inputStreamPtr = AndroidJNI.CallObjectMethod(instancePtr, AndroidJNI.GetMethodID(localRefPtr, "TestPluginArrayNonStatic",
            //                                                         "()[B"),
            //                                                         new jvalue[] { });

            // byte[] num = new byte[100];

            // Marshal.Copy(inputStreamPtr, num, 0, 100);

            // print(num[22] + " " + num[73]);
        }
    }

    void ReceiveVideoStream()
    {
        // string opt = "-y -i rtsp://13.126.154.86:5454/" + (SkypeManager.Instance.isCaller ? "caller.mpeg4" : "caller.mpeg4") + " -f image2pipe -vcodec mjpeg -";

        string opt =    "-y -i rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live" + (SkypeManager.Instance.isCaller ? "/receiver" : "/caller") + 
                        " -f image2pipe -vcodec mjpeg -";

        print(opt);

        ProcessStartInfo info = new ProcessStartInfo(ffmpegPath, opt);

        info.UseShellExecute = false;
        info.CreateNoWindow = true;
        info.RedirectStandardInput = false;
        info.RedirectStandardOutput = true;
        info.RedirectStandardError = false;

        receiveProcess = new Process();
        receiveProcess.StartInfo = info;
        receiveProcess.EnableRaisingEvents = true;
        receiveProcess.Exited += new EventHandler(ProcessExited);
        receiveProcess.Disposed += new EventHandler(ProcessDisposed);
        receiveProcess.OutputDataReceived += new DataReceivedEventHandler(ProcessOutputDataReceived);
        receiveProcess.ErrorDataReceived += new DataReceivedEventHandler(ErrorDataReceived);

        receiveProcess.Start();

        streamReceiver = new StreamReceiver(receiveProcess.StandardOutput, receiverImage, textureSize);
        streamReceiver.StartReceivingStream();

        // soundStreamReceiver = new SoundStreamReceiver();
        // soundStreamReceiver.StartReceivingAudio();
    }

    void ErrorDataReceived(object sender, DataReceivedEventArgs e)
    {
        print("error " + e.Data);
    }

    void ProcessOutputDataReceived(object sender, DataReceivedEventArgs e)
    {
        print(e.Data);
    }
    void ProcessExited(object sender, EventArgs e)
    {
        print("exited");
    }

    void ProcessDisposed(object sender, EventArgs e)
    {
        print("disposed");
    }

    public void VideoCompleted(VideoPlayer source)
    {
        print("Video Completed " + source.url);

        // File.Delete(source.url.Replace("file://", ""));
    }

    void OnDestroy()
    {
        if (soundStreamReceiver != null)
            soundStreamReceiver.Destroy();

        if (streamReceiver != null)
            streamReceiver.AbortThread();

        if (receiveProcess != null)
            receiveProcess.Kill();

        // print("Printing this");
    }

    void OnPreRender()
    {
        if (streamReceiver != null)
            streamReceiver.DrawFrame();
    }
}
