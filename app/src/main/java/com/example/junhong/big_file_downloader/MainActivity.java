package com.example.junhong.big_file_downloader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    //DATA
    private String[][] items;
    private URL url;

    //ListView
    private ArrayList<Download> list;
    private ListAdapter myAdapter;
    private Context selfContext = this;

    //UI
    private ListView download_list;
    static private TextView status;
    private Button connect;
    private EditText input_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO parsing the result with received data from the URL
        //TODO wouldn't it be better to make text box and able to enter the URL then loading the page
        //TODO But the default work should be loading the designated URL then parsing

        //TODO employing Button, When click the Button simultaneous downloading are started
        //TODO several approaches are possible


        //TODO with AsyncTask finish loading and update the listview
        //TODO select download path

        list = new ArrayList<Download>();

        status = (TextView) findViewById(R.id.statusTxt);
        connect = (Button) findViewById(R.id.connectBtn);
        input_url = (EditText) findViewById(R.id.download_url);

        connect.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                list.clear();
                if(isNetworkAvailable()){
                    try {
                        url = new URL(input_url.getText().toString());
                        Load parse = new Load();
                        parse.execute(url);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //parse from the URL
        try {
            if(isNetworkAvailable()) {
                url = new URL("http://nmsl.kaist.ac.kr/2015fall/cs492c/hw1/input.txt");
                Load parse = new Load();
                parse.execute(url);
            } else {
                status.setText("Please turn on the network");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


    }

    private boolean isNetworkAvailable() {
        boolean available = false;
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo data = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (networkInfo != null && networkInfo.isAvailable())
            available = true;

        if(available){
            if(wifi.isConnectedOrConnecting()){
                status.setText("WIFI network is connected");
            }
            if(data.isConnectedOrConnecting()){
                status.setText("DATA network is connected");
            }
        }
        return available;
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


    //Load contents from designated URL
    private class Load extends AsyncTask<URL, Integer, Long> {

        //file download
        private URL url;
        private InputStream in;

        //data
        private String returned;

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

        protected Long doInBackground(URL... urls) {
            try{    //It is the default work
                url = (URL)urls[0];
                URLConnection urlConnection = url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());

                //final String returned = readStream(in);
                returned = readStream(in);

                //from below code what we can obtain how many of items are stored in the txt
                String[] separated = returned.split("\n");

                items = new String[2][separated.length];


                for(int i = 0; i < separated.length; i++){
                    String[] temp = separated[i].split("\t");
                    items[0][i] = temp[0].trim();   //file name
                    items[1][i] = temp[1].trim();   //url
                    list.add(new Download(items[0][i], items[1][i]));
                }

                for(int i = 0; i < items[0].length; i++)
                    Log.d("NAME", items[0][i]);

                for(int i = 0; i < items[1].length; i++)
                    Log.d("URL", items[1][i]);

                //TODO parse each line with "\t", then can obtain the
                in.close();
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {

            myAdapter = new ListAdapter(list, selfContext);
            download_list = (ListView)findViewById(R.id.list);
            download_list.setAdapter(myAdapter);
        }
    }

    static public void setStatus(String txt){
        status.setText(txt);
    }
}
