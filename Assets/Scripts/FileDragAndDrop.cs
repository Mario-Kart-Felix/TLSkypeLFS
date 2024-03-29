﻿using System.Collections.Generic;
using UnityEngine;
using System.Linq;
using B83.Win32;


public class FileDragAndDrop : MonoBehaviour
{
    // important to keep the instance alive while the hook is active.
    UnityDragAndDropHook hook;
    void OnEnable ()
    {
        print("Enable");
        // must be created on the main thread to get the right thread id.
        hook = new UnityDragAndDropHook();
        hook.InstallHook();
        hook.OnDroppedFiles += OnFiles;
    }
    void OnDisable()
    {
        hook.UninstallHook();
    }

    void OnFiles(List<string> aFiles, POINT aPos)
    {
        // do something with the dropped file names. aPos will contain the 
        // mouse position within the window where the files has been dropped.
        print("Dropped "+aFiles.Count+" files at: " + aPos + "\n"+
            aFiles.Aggregate((a, b) => a + "\n" + b));

        int numFiles = aFiles.Count;

        for(int i = 0; i < numFiles; i++)
        {
            SkypeManager.Instance.UploadFile(aFiles[i]);
        }
    }

    // void Update()
    // {
    //     print("?Yessaasd");
    // }
}
