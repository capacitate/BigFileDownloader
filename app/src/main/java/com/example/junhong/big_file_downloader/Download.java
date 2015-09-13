package com.example.junhong.big_file_downloader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Junhong on 2015-09-13.
 */
public class Download {
    //file
    private String download_URL;
    private String filename;
    private Long filesize;

    //progress bar
    private Integer mProgress;
    private ProgressBar progressBar;

    public Download(String name, String url){
        download_URL = url;
        filename = name;

        mProgress = 0;
        progressBar = null;

    }

    public String getFilename() { return filename; }
    public String getUrl() { return download_URL; }
    public void setmProgress(Integer progress) { mProgress = progress; }
    public void setProgressBar(ProgressBar progressB){ progressBar = progressB; }
    public ProgressBar getProgressBar() { return progressBar; }
    public Integer getmProgress(){ return mProgress; }
    public void setFilesize(Long pfilesize) { filesize = pfilesize; }
    public Long getFilesize(){ return filesize; }
}
