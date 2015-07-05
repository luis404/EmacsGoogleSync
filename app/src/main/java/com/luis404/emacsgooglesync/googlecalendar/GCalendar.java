package com.luis404.emacsgooglesync.googlecalendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xzc on 15/7/5.
 */
public class GCalendar {
    private Calendar mCalendar;
    private GoogleAccountCredential mCredential;
    private Activity mActivity;
    private HttpTransport mTransport;
    private JsonFactory mJsonFactory;

    private List<DataListener> listeners;

    private static GCalendar sInstance;

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_GOOGLE_ACCOUNT = "google_acount";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};

    public static GCalendar getInstance(final Activity activity){
        if(sInstance == null){
            synchronized (GCalendar.class){
                if(sInstance == null){
                    sInstance = new GCalendar(activity);
                }
            }
        }

        return sInstance;
    }

    private GCalendar(Activity activity){
        mActivity = activity;

        mTransport = AndroidHttp.newCompatibleTransport();
        mJsonFactory = GsonFactory.getDefaultInstance();

        mCredential = GoogleAccountCredential.usingOAuth2(
                        mActivity, Arrays.asList(SCOPES))
                        .setBackOff(new ExponentialBackOff())
                        .setSelectedAccountName(getAccountName());
        mCalendar = new Calendar.Builder(mTransport, mJsonFactory, mCredential)
                        .setApplicationName("EmacsGoogleSync")
                        .build();
    }

    public String getAccountName(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String account = pref.getString(PREF_GOOGLE_ACCOUNT,null);
        return account;
    }

    public void saveAccountName(String name){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_GOOGLE_ACCOUNT, name);
        editor.apply();
    }

    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        mActivity,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    public void chooseAccount(){
        mActivity.startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER
        );
    }

    public void addListener(DataListener listener){
        if(listeners == null){
            listeners = new ArrayList<DataListener>();
        }
        listeners.add(listener);
    }

    public void refresh(){
        if(getAccountName() == null){
            chooseAccount();
        } else {
            CalendarSyncTask task = new CalendarSyncTask();
            task.execute();
        }
    }

    class CalendarSyncTask extends AsyncTask<Void, Void, Events>{

        @Override
        protected Events doInBackground(Void... params) {
            try {
                return getCalendarData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private Events getCalendarData() throws IOException{
            DateTime now = new DateTime(System.currentTimeMillis());

            List<String> eventStrings = new ArrayList<String>();
            Events events = mCalendar.events().list("primary")
                            .setMaxResults(20)
                            .setTimeMin(now)
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();

            return events;
        }

        @Override
        protected void onPostExecute(Events events){
            DataListener listener;
            for(int i=0,size=listeners.size(); i<size; i++){
                listener = listeners.get(i);
                listener.handleResult(events);
            }
        }
    }

    public interface DataListener{
        void handleResult(Events events);
    }
}
