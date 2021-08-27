package com.example.certificatepinningtest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.certificatepinningtest.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {
    private MutableLiveData<String> data;
    ActivityMainBinding binding;

    public MutableLiveData<String> getData() {
        if (data == null) {
            data = new MutableLiveData<String>();
        }
        return data;
    }

    final Observer<String> observer = new Observer<String>() {
        @Override
        public void onChanged(@Nullable final String data) {
            binding.webview.loadDataWithBaseURL(null, data, "text/html; charset=utf-8", "UTF-8", null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.webview.getSettings().setJavaScriptEnabled(true);
        getData().observe(this, observer);
//        new SendRequest(this).execute();
        new SendRequestWithOkHttp(this).execute();
    }

    private static class SendRequest extends AsyncTask<Void,Void,Void> {
        WeakReference<Activity> mWeakActivity;

        public SendRequest(Activity activity) {
            mWeakActivity = new WeakReference<Activity>(activity);
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL("https://wikipedia.org");
                String response = "";
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestMethod("POST");
                InputStreamReader in = new InputStreamReader(conn.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String line= "";
                while ((line= br.readLine()) != null) {
                    response += line;
                }
                conn.disconnect();
                Log.d("AAA", response);
                ((MainActivity)mWeakActivity.get()).data.postValue(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static class SendRequestWithOkHttp extends AsyncTask<Void,Void,Void> {
        WeakReference<Activity> mWeakActivity;

        public SendRequestWithOkHttp(Activity activity) {
            mWeakActivity = new WeakReference<Activity>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("AAA", "SendRequestWithOkHttp");
            try {
                URL url = new URL("https://wikipedia.org");
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                String response = "";
                CertificatePinner certificatePinner = new CertificatePinner.Builder()
                        .add("wikipedia.org", "sha256/ikocd6xWf/wVZnOoeTONMD0v2N8TTr7N1u67gQ+ZxbI=")
                        .build();
                OkHttpClient okHttpClient = new OkHttpClient.Builder()
                        .certificatePinner(certificatePinner)
                        .build();
//                OkHttpClient okHttpClient = new OkHttpClient();
                InputStreamReader in = new InputStreamReader( okHttpClient.newCall(request).execute().body().byteStream());
                BufferedReader br = new BufferedReader(in);
                String line= "";
                while ((line= br.readLine()) != null) {
                    response += line;
                }
                Log.d("AAA", response);
                ((MainActivity)mWeakActivity.get()).data.postValue(response);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("AAA", e.getMessage());
            }

            return null;
        }
    }
}