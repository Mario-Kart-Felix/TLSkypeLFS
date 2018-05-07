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

public class VideoReceiver : MonoBehaviour
{
    public  UniversalMediaPlayer    universalMediaPlayer;

    public void StartVideoReceiver()
    {
        Application.runInBackground = true;

        universalMediaPlayer.Path = "rtmp://ec2-13-126-154-86.ap-south-1.compute.amazonaws.com/live/" + (SkypeManager.Instance.isCaller ? "receiver" : "caller");

        if (Application.platform == RuntimePlatform.Android)
        {
            ReceiveVideoStream_Android();
        }
        else
        {
            ReceiveVideoStream();
        }
    }

    void ReceiveVideoStream_Android()
    {
        universalMediaPlayer.Play();
    }

    void ReceiveVideoStream()
    {
        universalMediaPlayer.Play();
    }

    void OnDestroy()
    {
        // print("Printing this");
    }
}
