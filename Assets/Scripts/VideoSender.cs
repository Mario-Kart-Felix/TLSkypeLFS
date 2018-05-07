using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Diagnostics;
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
using UnityEngine.Video;
using UnityEngine.Profiling;

public class VideoSender : MonoBehaviour
{
    Process senderProcess;

    Process waveOutTestProcess;

    private string ffmpegPath = "";

    public RawImage senderImage;

    public Vector2 textureSize;

    private StreamReceiver streamReceiver;

    private StreamSender streamSender;

    public void StartVideoSender()
    {
        Application.runInBackground = true;

        ffmpegPath = FFmpegConfig.BinaryPath;

        if (Application.platform == RuntimePlatform.Android)
        {
            SendVideo_Android();
        }
        else
        {
            SendVideo();
        }
    }

    void SendVideo()
    {
        if (UnityEngine.WebCamTexture.devices.Length == 0)
            return;

        string opt = "-y -f dshow -i video=\"" + UnityEngine.WebCamTexture.devices[0].name + "\":audio=\"" + UnityEngine.Microphone.devices[0] + "\""
                 + " -f flv rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live" + (SkypeManager.Instance.isCaller ? "/caller" : "/receiver")
                 + " -f image2pipe -vcodec mjpeg -";

        print(opt);

        var info = new ProcessStartInfo(ffmpegPath, opt);

        info.UseShellExecute = false;
        info.CreateNoWindow = true;
        info.RedirectStandardInput = false;
        info.RedirectStandardOutput = true;
        info.RedirectStandardError = false;

        senderProcess = new Process();
        senderProcess.StartInfo = info;
        senderProcess.EnableRaisingEvents = true;
        senderProcess.Exited += new EventHandler(ProcessExited);
        senderProcess.Disposed += new EventHandler(ProcessDisposed);
        senderProcess.OutputDataReceived += new DataReceivedEventHandler(ProcessOutputDataReceived);
        senderProcess.ErrorDataReceived += new DataReceivedEventHandler(ErrorDataReceived);
        senderProcess.Start();

        streamReceiver = new StreamReceiver(senderProcess.StandardOutput, senderImage, textureSize);
        streamReceiver.StartReceivingStream();
    }

    void SendVideo_Android()
    {
        streamSender = new StreamSender(null, senderImage, textureSize);
        streamSender.StartSendingStream();
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

    void OnDestroy()
    {
        if (streamReceiver != null)
            streamReceiver.AbortThread();

        if (streamSender != null)
            streamSender.AbortThread();

        if (senderProcess != null)
            senderProcess.Kill();

        if (waveOutTestProcess != null)
            waveOutTestProcess.Kill();
    }

    void OnPreRender()
    {
        if (streamReceiver != null)
            streamReceiver.DrawFrame();

        if (streamSender != null)
            streamSender.DrawFrame();
    }
}
