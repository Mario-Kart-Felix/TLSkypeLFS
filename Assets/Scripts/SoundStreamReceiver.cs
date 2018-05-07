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
using UnityEngine.Profiling;
using System.Media;

public class SoundStreamReceiver
{
    int numDataPerRead = 5000;
    byte[] newData;

    Process audioProcess;

    private string ffmpegPath = "";

    private BinaryReader stdout;

    Thread audioFetchThread;
    Thread audioPlayThread;

    private bool firstTime = true;

    woLib woLibObject = new woLib();

    private bool audioPresent = false;

    private int bytesRead = 0;


    public void StartReceivingAudio()
    {
        // Application.runInBackground = true;

        // ffmpegPath = FFmpegConfig.BinaryPath;

        // string opt = "-y -i rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live" + (SkypeManager.Instance.isCaller ? "/receiver" : "/caller")  + " -f wav -fflags +bitexact -flags:v +bitexact -flags:a +bitexact -map_metadata -1 -";

        // ProcessStartInfo info = new ProcessStartInfo(Application.streamingAssetsPath + "/FFmpegOut/Windows/WaveOutTestCSharp.exe", opt);

        // UnityEngine.Debug.Log(opt);

        // info.UseShellExecute = false;
        // info.CreateNoWindow = true;
        // info.RedirectStandardInput = false;
        // info.RedirectStandardOutput = false;
        // info.RedirectStandardError = false;

        // audioProcess = new Process();
        // audioProcess.StartInfo = info;
        // audioProcess.EnableRaisingEvents = false;
        // audioProcess.Start();

        // FileStream  fileStream = new FileStream("c:\\out.wav", FileMode.Open);

        // stdout = new BinaryReader(fileStream);  //new BinaryReader(audioProcess.StandardOutput.BaseStream);

        // audioFetchThread = new Thread(new ThreadStart(AudioFetchUpdate));
        // audioFetchThread.Priority = System.Threading.ThreadPriority.Highest;
        // audioFetchThread.Start();

        // audioPlayThread = new Thread(new ThreadStart(AudioPlayUpdate));
        // audioPlayThread.Priority = System.Threading.ThreadPriority.Highest;
        // audioPlayThread.Start();

        // woLibObject.InitWODevice(44100, 2, 16, false);
    }

    public void AudioFetchUpdate()
    {
        newData = new byte[numDataPerRead];

        while (true)
        {
            if (audioPresent)
            {
                Thread.Sleep(10);
                continue;
            }

            bytesRead = stdout.Read(newData, 0, numDataPerRead);

            if (firstTime)
            {
                firstTime = false;
                continue;
            }

            if (bytesRead > 0)
                audioPresent = true;
        }
    }

    public unsafe void AudioPlayUpdate()
    {
        newData = new byte[numDataPerRead];

        while (true)
        {
            if (!audioPresent)
            {
                Thread.Sleep(10);
                continue;
            }

            fixed (byte* p = newData)
            {
                IntPtr pPCM = (IntPtr)p;
                woLibObject.SendWODevice(pPCM, (uint)bytesRead);
            }

            audioPresent = false;
        }
    }

    // void ErrorDataReceived(object sender, DataReceivedEventArgs e)
    // {
    //     UnityEngine.Debug.Log("error " + e.Data);
    // }

    // void ProcessOutputDataReceived(object sender, DataReceivedEventArgs e)
    // {
    //     UnityEngine.Debug.Log(e.Data);
    // }
    // void ProcessExited(object sender, EventArgs e)
    // {
    //     UnityEngine.Debug.Log("exited");
    // }

    // void ProcessDisposed(object sender, EventArgs e)
    // {
    //     UnityEngine.Debug.Log("disposed");
    // }

    public void Destroy()
    {
        // UnityEngine.Debug.Log("YOYOYO");

        // woLibObject.ResetWODevice();
        woLibObject.CloseWODevice();

        // woLibObject.Dispose();

        // if (audioFetchThread != null)
        //     audioFetchThread.Abort();

        // if (audioPlayThread != null)
        //     audioPlayThread.Abort();

        // if (audioProcess != null)
        //     audioProcess.Kill();
    }
}
