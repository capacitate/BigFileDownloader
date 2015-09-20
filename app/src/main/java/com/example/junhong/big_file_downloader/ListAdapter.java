package com.example.junhong.big_file_downloader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by Junhong on 2015-09-13.
 */
public class ListAdapter extends BaseAdapter implements android.widget.ListAdapter{
    private ArrayList<Download> list = new ArrayList<Download>();
    private Context context;
    private LayoutInflater inflater;

    //CONSTANTs
    private final double KBUNIT = 1024;
    private final int ROUNDUP = 100;
    private final String DEFAULT_PATH = "/sdcard/Download/";

    public ListAdapter(ArrayList<Download> plist, Context pcontext){
        list = plist;
        context = pcontext;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return (list == null)? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        final Download file = (Download) getItem(position);

        if(null == convertView){
            convertView = inflater.inflate(R.layout.list_item, null);

            holder.start_btn = (Button)convertView.findViewById(R.id.start);
            holder.retry_btn = (Button)convertView.findViewById(R.id.retry);
            holder.open_btn = (Button)convertView.findViewById(R.id.open);

            holder.filename = (TextView)convertView.findViewById(R.id.filename);
            holder.receive = (TextView)convertView.findViewById(R.id.receive);
            holder.total = (TextView)convertView.findViewById(R.id.total);
            holder.progress = (ProgressBar)convertView.findViewById(R.id.file_progress);

            holder.filename.setText(((Download) getItem(position)).getFilename());

            holder.open_btn.setEnabled(false);
            holder.retry_btn.setEnabled(false);
            holder.item = file;

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();

            holder.item.setProgressBar(null);
            holder.item = file;
            holder.item.setProgressBar(holder.progress);
        }

        holder.progress.setProgress(file.getmProgress());
        file.setProgressBar(holder.progress);
        holder.progress.setMax(10);

        final Holder holderT = holder;  //in order to handover subclass
        final Button retry_btn = holder.retry_btn;
        final Button open_btn = holder.open_btn;
        final Button start_btn = holder.start_btn;

        holder.start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_btn.setEnabled(false);
                start_btn.invalidate();

                open_btn.setEnabled(false);
                retry_btn.setEnabled(false);

                startMultiDown(file, holderT);
            }
        });

        holder.retry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry_btn.setEnabled(false);
                retry_btn.invalidate();

                start_btn.setEnabled(false);
                retry_btn.setEnabled(false);
                open_btn.setEnabled(false);

                startMultiDown(file, holderT);
            }
        });

        holder.open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO open with default luncher
                viewFile(DEFAULT_PATH + file.getFilename(), file.getFilename());
            }
        });

        return convertView;
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

    public void startMultiDown(Download pfile, Holder pholder){
        DownloadFileAsync task = new DownloadFileAsync(pfile, pholder);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    //TODO update the total amount of download size

    private class DownloadFileAsync extends AsyncTask<String, String, String> {
        private Download file;
        private Holder view_holder;
        private long total;
        private long filesize;
        private boolean connected = false;
        public DownloadFileAsync(Download pfile, Holder pholder){ file = pfile; view_holder = pholder; }

        @Override
        protected void onPreExecute(){
            ProgressBar bar = file.getProgressBar();
            file.setmProgress(0);
            bar.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            MainActivity.setStatus("Start Downloading..");
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
            Log.d("DOWN", "Start Download" + file.getFilename());

            int count;

            try{
                URL url = new URL(file.getUrl());
                URLConnection conexion = url.openConnection();
                conexion.connect();

                filesize = conexion.getContentLength();
                file.setFilesize(filesize);

                Log.d("FILESIZE", file.getFilename() + " size :" + filesize);

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(DEFAULT_PATH + file.getFilename());

                byte data[] = new byte[1024];

                total = 0;

                while((count = input.read(data)) != -1){
                    total = (total + count);
                    publishProgress("" + (int)(total * 10) / (filesize));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                connected = true;
                Log.d("FILESIZE", file.getFilename() + " done" + filesize);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                File downloaded = new File(DEFAULT_PATH + file.getFilename());
                downloaded.delete();
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            MainActivity.setStatus("finish the download");
            if(total != filesize) {
                ReDownloadAsync retry = new ReDownloadAsync(context, inflater, file, view_holder);
                retry.execute();

                view_holder.start_btn.setEnabled(true);
            } else if( !connected ){
                MainActivity.setStatus("please connect on the Internet");
                view_holder.start_btn.setEnabled(true);
            } else {
                view_holder.retry_btn.setEnabled(true);
                view_holder.open_btn.setEnabled(true);

                //TODO open with default luncher
                Toast.makeText(context, file.getFilename() + " 다운로드 완료 open!", Toast.LENGTH_SHORT).show();
                viewFile(DEFAULT_PATH + file.getFilename(), file.getFilename());
            }
        }
    }
}
