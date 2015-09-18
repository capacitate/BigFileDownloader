package com.example.junhong.big_file_downloader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.LayoutInflater;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


/**
 * Created by KAIST on 2015-09-19.
 */
public class ReDownloadAsync extends AsyncTask<String, String, String> {
    private Context context;
    private LayoutInflater inflater;

    //CONSTANTs
    private final double KBUNIT = 1024;
    private final int ROUNDUP = 100;
    private final String DEFAULT_PATH = "/sdcard/Download/";
    private final int RETRY_COUNT = 5;

    private Download file;
    private Holder view_holder;
    private long total;
    private boolean download_result = false;

    public ReDownloadAsync(Context pContext, LayoutInflater pInflater, Download pfile, Holder pholder)
    {
        context = pContext;
        inflater = pInflater;
        file = pfile;
        view_holder = pholder;
    }

    public void viewFile(String filePath, String fileName){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        //File openFile = new File(filePath, fileName);
        File openFile = new File(filePath);
        String fileExtension = getExtension(fileName);
        Uri uri = Uri.fromFile(openFile);

        //TODO use absolute value

        if(fileExtension.equalsIgnoreCase("jpg")) {
            i.setDataAndType(uri, "image/*");
        } else if(fileExtension.equalsIgnoreCase("mp4")){
            i.setDataAndType(uri, "video/*");
        }

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list_resolve = pm.queryIntentActivities(i, PackageManager.GET_META_DATA);

        if(list_resolve.size() == 0){
            Toast.makeText(context, fileName + " 을 확인할 수 있는 앱이 설치되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {
            context.startActivity(i);
        }
    }

    public String getExtension(String filename){
        return filename.substring(filename.lastIndexOf(".") + 1, filename.length());
    }

    @Override
    protected void onPreExecute(){
        ProgressBar bar = file.getProgressBar();
        bar.setMax(10);
        bar.setProgress(0);
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        MainActivity.setStatus("Retry Downloading..");
        file.setmProgress(Integer.parseInt(progress[0]));
        ProgressBar bar = file.getProgressBar();

        if( bar != null){
            bar.setProgress( file.getmProgress() );
            bar.invalidate();
        }

        view_holder.total.setText(String.valueOf( (double)Math.round( ROUNDUP * file.getFilesize() / KBUNIT ) / ROUNDUP ) );
        view_holder.receive.setText( String.valueOf( (double)Math.round( ROUNDUP * total / KBUNIT ) / ROUNDUP ) );
    }

    @Override
    protected String doInBackground(String... params) {
        for(int i = 0; i < RETRY_COUNT; i++) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(download_result)
                break;

            int count;

            try {
                URL url = new URL(file.getUrl());
                URLConnection conexion = url.openConnection();
                conexion.connect();

                long filesize = conexion.getContentLength();
                file.setFilesize(filesize);

                Log.d("FILESIZE", file.getFilename() + " size :" + filesize);

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(DEFAULT_PATH + file.getFilename());

                byte data[] = new byte[1024];

                total = 0;

                while ((count = input.read(data)) != -1) {
                    total = (total + count);
                    publishProgress("" + (int) (total * 100) / (filesize));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                Log.d("FILESIZE", file.getFilename() + " done" + filesize);
                download_result = true;
            } catch (MalformedURLException e) {
                download_result = false;
                e.printStackTrace();
            } catch (IOException e) {
                download_result = false;
                File downloaded = new File(DEFAULT_PATH + file.getFilename());
                downloaded.delete();
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        MainActivity.setStatus("finish the download");
        view_holder.retry_btn.setEnabled(true);
        view_holder.open_btn.setEnabled(true);

        //TODO open with default luncher
        Toast.makeText(context, file.getFilename() + " 다운로드 완료 open!", Toast.LENGTH_SHORT).show();
        viewFile(DEFAULT_PATH + file.getFilename(), file.getFilename());
    }
}
