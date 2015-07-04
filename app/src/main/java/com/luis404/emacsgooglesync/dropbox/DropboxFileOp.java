package com.luis404.emacsgooglesync.dropbox;

import android.content.Context;
import android.os.DropBoxManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xzc on 15/7/4.
 */
public class DropboxFileOp {
    private DropboxAPI<AndroidAuthSession> mApi;
    private DropboxAuth mAuth;

    public DropboxFileOp(Context context){
        mAuth = DropboxAuth.getInstance(context);
        mApi = mAuth.getApi();
    }

    public void uploadFile(final String fileName, final String remoteName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(fileName);
                    FileInputStream is = new FileInputStream(file);
                    DropboxAPI.Entry response = mApi.putFile(remoteName, is, file.length(), null, null);
                    Log.d("lxDB", response.toString());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void downloadFile(final String localName, final String remoteName){
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        File file = new File(localName);
                        if(!file.exists()){
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        DropboxAPI.DropboxFileInfo response = mApi.getFile(remoteName, null, fos, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
            }
        }).start();
    }
}
