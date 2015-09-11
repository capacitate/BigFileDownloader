package com.example.junhong.big_file_downloader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    public Thread LoadTxtParsing = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO parsing the result with received data from the URL
        //TODO wouldn't it be better to make text box and able to enter the URL then loading the page
        //TODO But the default work should be loading the designated URL then parsing

        int number = 0;

        LoadTxtParsing = new LoadTxt();

        /*
        LoadTxtParsing = new Thread(new Runnable(){
            public void run(){
                URL url = null;
                InputStream in = null;

                try{    //It is the default work
                    url = new URL("http://nmsl.kaist.ac.kr/2015fall/cs492c/hw1/input.txt");
                    URLConnection urlConnection = url.openConnection();
                    in = new BufferedInputStream(urlConnection.getInputStream());
                }catch (MalformedURLException e){
                    //do whatever
                }catch(IOException e){
                    //do whatever
                }
                try {
                    final String returned = readStream(in);
                    final TextView tView = (TextView) findViewById(R.id.text);
                    tView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),
                                    "Web page downloaded successfully", Toast.LENGTH_SHORT)
                                    .show();
                            tView.setText(returned);
                        }
                    });

                    //from below code what we can obtain how many of items are stored in the txt
                    //TODO parse each line with "\t", then can obtain the
                    final String[] separated = returned.split("\n");

                    tView.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getBaseContext(),
                                    "Parsing", Toast.LENGTH_SHORT)
                                    .show();
                            tView.setText(separated[1]);
                        }
                    });

                }finally {
                    try{
                        in.close();
                    }catch(IOException e){
                        //do whatever
                    }
                }
            }
        });
        */

        LoadTxtParsing.start();



    }it
    private boolean isNetworkAvailable() {
        boolean available = false;
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable())
            available = true;
        return available;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String s = null;
        byte[] buffer = new byte[1000];
        InputStream iStream = null;
        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            iStream.read(buffer);
            s = new String(buffer);
        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
        }
        return s;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {
        String s = null;

        protected String doInBackground(String... url) {
            try {
                s = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return s;
        }

        protected void onPostExecute(String result) {
            TextView tView = (TextView) findViewById(R.id.text);
            tView.setText(result);
            Toast.makeText(getBaseContext(),
                    "Web page downloaded successfully", Toast.LENGTH_SHORT)
                    .show();
            Toast.makeText(getBaseContext(),
                    result, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadTxt extends Thread{

        public String returned = null;
        public int number = 0;

        @Override
        public void run(){
            super.run();
            URL url = null;
            InputStream in = null;

            try{    //It is the default work
                url = new URL("http://nmsl.kaist.ac.kr/2015fall/cs492c/hw1/input.txt");
                URLConnection urlConnection = url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            }catch (MalformedURLException e){
                //do whatever
            }catch(IOException e){
                //do whatever
            }
            try {
                //final String returned = readStream(in);
                returned = readStream(in);
                final TextView tView = (TextView) findViewById(R.id.text);
                tView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),
                                "Web page downloaded successfully", Toast.LENGTH_SHORT)
                                .show();
                        tView.setText(returned);
                    }
                });

                //from below code what we can obtain how many of items are stored in the txt
                //TODO parse each line with "\t", then can obtain the
                final String[] separated = returned.split("\n");
                number = separated.length;

                tView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(),
                                "Parsing", Toast.LENGTH_SHORT)
                                .show();
                        tView.setText(separated[1]);
                    }
                });

            }finally {
                try{
                    in.close();
                }catch(IOException e){
                    //do whatever
                }
            }
        }

        public int getNumber(){
            return number;
        }
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
