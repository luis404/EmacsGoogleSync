package com.luis404.emacsgooglesync;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.luis404.emacsgooglesync.dropbox.DropboxAuth;
import com.luis404.emacsgooglesync.dropbox.DropboxFileOp;
import com.luis404.emacsgooglesync.googlecalendar.GCalendar;


public class MainActivity extends Activity implements View.OnClickListener{
    private Button mBtnDropbox;
    private Button mBtnGoogle;
    private Button mBtnSync;
    private Context mContext;

    private DropboxAuth mDropBoxAuth;
    private GCalendar mGoogleCalendar;

    private boolean mGoogleEnabled;
    private boolean mDropboxEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        initView();
        registerHandler();

        mDropBoxAuth = DropboxAuth.getInstance(mContext);
        mDropboxEnabled = mDropBoxAuth.finishAuth();
        mGoogleCalendar = GCalendar.getInstance(this);
        mGoogleEnabled = mGoogleCalendar.getAccountName() == null ? false : true;

        if(mDropboxEnabled) {
            mBtnDropbox.setText(R.string.dropbox_enabled);
            mBtnDropbox.setClickable(false);
        }

        if(mGoogleEnabled){
            mBtnGoogle.setText(R.string.google_calendar_enabled);
            mBtnGoogle.setClickable(false);
        }
    }

    private void initView(){
        mBtnDropbox = (Button)findViewById(R.id.dropbox);
        mBtnGoogle = (Button)findViewById(R.id.google);
        mBtnSync = (Button)findViewById(R.id.sync);
    }

    private void registerHandler(){
        mBtnDropbox.setOnClickListener(this);
        mBtnGoogle.setOnClickListener(this);
        mBtnSync.setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        ifSetDropbox();
    }

    private void ifSetDropbox(){
        if(!mDropboxEnabled) {
            if (mDropBoxAuth.finishAuth()) {
                mDropboxEnabled = true;
                mBtnDropbox.setText(R.string.dropbox_enabled);
                mBtnDropbox.setClickable(false);
            } else {
                Toast.makeText(this, "Dropbox Authentication Failed", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCoe, Intent data){
        super.onActivityResult(requestCode, resultCoe, data);

        switch (requestCode){
            case GCalendar.REQUEST_ACCOUNT_PICKER:
                if(resultCoe == RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if(accountName != null){
                        mBtnGoogle.setText(R.string.google_calendar_enabled);
                        mBtnGoogle.setClickable(false);
                        mGoogleCalendar.saveAccountName(accountName);
                    }
                } else {
                    Toast.makeText(this, "Choose google account failed", Toast.LENGTH_LONG)
                            .show();
                }
                break;
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.dropbox:
                mDropBoxAuth.startAuth();
                break;
            case R.id.google:
                mGoogleCalendar.chooseAccount();
                break;
            case R.id.sync:
                if(mDropboxEnabled && mGoogleEnabled){
                    //dosync
                } else {

                }

                break;
        }
    }
}
