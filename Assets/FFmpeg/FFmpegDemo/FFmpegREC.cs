using UnityEngine;
using System;
using System.IO;
using System.Text;
using System.Collections;
using System.Collections.Generic;

namespace FFmpeg.Demo.REC
{
    public class FFmpegREC : MonoBehaviour, IFFmpegHandler
    {
        //Data
        [Header("Targeted FPS")]
        public int FPS = 30;
        float actualFPS;
        [Header("0 for max resolution")]
        public int width = 854;
        public int height = 480;

        //Paths
        const string FILE_NAME_SUFIX = "_Frame.png";
        const string VIDEO_NAME = "ScreenCapture.mp4";
        string cashDir { get; set; }
        string firstImgFilePathEnclosed, outputVideoPath;

        //Variables
        Queue<Texture2D> frames = new Queue<Texture2D>();
        int f { get; set; } //Current frame index
        float updateRate;
        float startTime;
        int framesCount;
        WaitForSeconds delay;
        WaitForEndOfFrame frame = new WaitForEndOfFrame();

        public bool isREC { get; private set; }
        public bool isProducing { get; private set; }
        public bool isReady { get { return !isREC && !isProducing; } }
#if !UNITY_EDITOR
        int initialWidth, initialHeight;
        public bool overrideResolution { get { return width > 0 || height > 0; } }
#endif

        //References
        Action<string> onOutput, onFinish;

        //PUBLIC INTERFACE
        //------------------------------

        public void Init(Action<string> _onOutput, Action<string> _onFinish)
        {
            FFmpegParser.Handler = this;

            cashDir = Path.Combine(Application.temporaryCachePath, "Frames");
            firstImgFilePathEnclosed = Path.Combine(cashDir, "%0d" + FILE_NAME_SUFIX);
            outputVideoPath = Path.Combine(cashDir, VIDEO_NAME);

#if !UNITY_EDITOR
            initialWidth = Screen.width;
            initialHeight = Screen.height;
#endif

            onOutput = _onOutput;
            onFinish = _onFinish;
        }

        public void StartREC()
        {
            if (isReady)
            {
                if (Directory.Exists(cashDir))
                    Directory.Delete(cashDir, true);
                Directory.CreateDirectory(cashDir);
                Debug.Log("CashDir created: " + cashDir);

#if !UNITY_EDITOR
                if (overrideResolution)
                {
                    //Low level operation of resolution change
                    Screen.SetResolution(width, height, Screen.fullScreen);
                }
                else 
#endif
                {
                    width = Screen.width;
                    height = Screen.height;
                }

                frames.Clear();
                f = 0;
                startTime = Time.time;
                framesCount = 0;
                delay = new WaitForSeconds(1.0f / FPS);

                isREC = true;

                StartCoroutine(CaptureScreenshots());
            }
        }

        public void StopREC()
        {
            isREC = false;

            actualFPS = framesCount / (Time.time - startTime);

            StartCoroutine(SaveImages());

#if !UNITY_EDITOR
            if (overrideResolution)
                //Return to initial screen resolution
                Screen.SetResolution(initialWidth, initialHeight, Screen.fullScreen);
#endif
        }

        //INTERNAL IMPLEMENTATION
        //------------------------------

        IEnumerator CaptureScreenshots()
        {
            while (isREC)
            {
                ++framesCount;
                frames.Enqueue(ScreenCapture.CaptureScreenshotAsTexture());
                yield return delay;
            }
        }

        IEnumerator SaveImages()
        {
            onOutput("Captured resolution: " + width + " - " + height + "\n");
            onOutput("Actual FPS: " + actualFPS.ToString("00.00") + "\n");
            onOutput("Screenshots saving...\n");
            while(frames.Count > 0)
            {
                File.WriteAllBytes(
                    Path.Combine(cashDir, NextImgFileName()), 
                    frames.Dequeue().EncodeToPNG());
                yield return frame;
            }

            CreateVideo();
        }

        string NextImgFileName()
        {
            return f++ + FILE_NAME_SUFIX;
        }

        void CreateVideo()
        {
            StringBuilder command = new StringBuilder();

            command.
                   Append("-y -framerate ").
                   Append(actualFPS.ToString()).
                   Append(" -f image2 -i ").
                   Append(firstImgFilePathEnclosed).
                   Append(" -vcodec libx264 -crf 25 -pix_fmt yuv420p ").
                   Append(outputVideoPath);

            Debug.Log(command.ToString());

            FFmpegCommands.DirectInput(command.ToString());
        }

        //FFmpeg processing callbacks
        //------------------------------

        //Begining of video processing
        public void OnStart()
        {
            onOutput("VideoProducing Started\n");
        }

        //You can make custom progress bar here (parse msg)
        public void OnProgress(string msg)
        {
            onOutput(msg);
        }

        //Notify user about failure here
        public void OnFailure(string msg)
        {
            onOutput(msg);
        }

        //Notify user about success here
        public void OnSuccess(string msg)
        {
            onOutput(msg);
        }

        //Last callback - do whatever you need next
        public void OnFinish()
        {
            onFinish(outputVideoPath);
        }
    }
}