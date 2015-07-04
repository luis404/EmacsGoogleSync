package com.luis404.emacsgooglesync;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.luis404.emacsgooglesync.dropbox.DropboxAuth;
import com.luis404.emacsgooglesync.dropbox.DropboxFileOp;
import com.luis404.emacsgooglesync.sample.R;


public class MainActivity extends Activity implements View.OnClickListener{
    private Button mBtnAuth;
    private Button mBtnUploadFile;
    private Button mBtnDownloadFile;
    private Context mContext;
    private DropboxAuth mDropBoxAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        initView();
        registerHandler();
    }

    private void initView(){
        mBtnAuth = (Button)findViewById(R.id.auth);
        mBtnUploadFile = (Button)findViewById(R.id.upload_file);
        mBtnDownloadFile = (Button)findViewById(R.id.download_file);
        mDropBoxAuth = DropboxAuth.getInstance(mContext);
    }

    private void registerHandler(){
        mBtnAuth.setOnClickListener(this);
        mBtnUploadFile.setOnClickListener(this);
        mBtnDownloadFile.setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mDropBoxAuth.finishAuth()){
            mBtnAuth.setText(R.string.hello_world);
            mBtnAuth.setClickable(false);
            mBtnUploadFile.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Dropbox Authentication Failed", Toast.LENGTH_LONG)
                    .show();
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.auth:
                mDropBoxAuth.startAuth();
                break;
            case R.id.upload_file:
                DropboxFileOp op1 = new DropboxFileOp(this);
                op1.uploadFile("/sdcard/testDropboxApp.txt", "helo");
                break;
            case R.id.download_file:
                DropboxFileOp op2 = new DropboxFileOp(this);
                op2.downloadFile("/sdcard/emacs-diary", "/lx/emacs-diary");
                break;
        }
    }
}
