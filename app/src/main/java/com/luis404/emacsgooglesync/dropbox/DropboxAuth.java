package com.luis404.emacsgooglesync.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.luis404.emacsgooglesync.BuildConfig;

/**
 * Created by xzc on 15/7/4.
 */
public class DropboxAuth {
    public static final String TOKEN = "token";
    private static final String APP_KEY = BuildConfig.APPKEY;
    private static final String APP_SECRET = BuildConfig.APPSECRET;

    private static DropboxAPI<AndroidAuthSession> mApi;
    private static DropboxAuth sInstance;
    private AndroidAuthSession mSession;
    private String mToken;

    private Context mContext;

    public static DropboxAuth getInstance(Context context){
        if(sInstance == null){
            synchronized (DropboxAuth.class){
                if(sInstance == null){
                    sInstance = new DropboxAuth(context);
                }
            }
        }
        return sInstance;
    }

    //Sync dropbox account when create
    private DropboxAuth(Context context){
        mContext = context;
        AppKeyPair keyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        mSession = new AndroidAuthSession(keyPair);
        mToken = getToken();
        if(mToken.length() > 0){
            mSession.setOAuth2AccessToken(mToken);
        }
        mApi = new DropboxAPI<AndroidAuthSession>(mSession);
    }

    public void startAuth(){
        mApi.getSession().startOAuth2Authentication(mContext);
    }

    private void storeToken(String token) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TOKEN, token);
        editor.apply();
    }

    public String getToken(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String token = pref.getString(TOKEN, "");
        return token;
    }

    public boolean finishAuth(){
        if(mToken.length() > 0){
            return true;
        }

        if(mSession.authenticationSuccessful()){
            mSession.finishAuthentication();
            storeToken(mSession.getOAuth2AccessToken());
            return true;
        }

        return false;
    }

    public DropboxAPI<AndroidAuthSession> getApi(){
        return mApi;
    }
}
