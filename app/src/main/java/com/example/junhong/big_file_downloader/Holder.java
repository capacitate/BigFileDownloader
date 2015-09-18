package com.example.junhong.big_file_downloader;

import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by KAIST on 2015-09-19.
 */
public class Holder {
    Button start_btn;
    Button retry_btn;
    Button open_btn;

    TextView filename;
    TextView receive;
    TextView total;

    ProgressBar progress;

    Download item;
}
