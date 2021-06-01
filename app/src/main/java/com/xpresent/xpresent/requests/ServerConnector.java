/**
 * Company: Xpresent
 * Creator: Alex Fedotov
 * date: 09.06.20
 */

package com.xpresent.xpresent.requests;

import android.content.Context;
import android.os.AsyncTask;
import com.xpresent.xpresent.config.config;
import com.xpresent.xpresent.ui.LoadingDialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import android.os.SystemClock;

public class ServerConnector extends AsyncTask<HashMap<String,String>,Void, AsyncTaskResult<String>> {
    private WeakReference<Context> contextRef;
    private LoadingDialog progressDialog;
    private boolean showLoading;
    private String host = config.HOST_NAME;
    private String appUrl = config.APP_URL;
    private final String ENCODING = "UTF-8";
    private AsyncResponse delegate = null;

    // interface for result from onPostExecute
    public interface AsyncResponse {
        void processFinish(boolean success, String output);
    }

    public ServerConnector(Context ctx, AsyncResponse delegate, boolean showLoading) {
        this.contextRef = new WeakReference<>(ctx);
        this.delegate = delegate;
        this.showLoading = showLoading;
    }

    @SafeVarargs
    @Override
    public final AsyncTaskResult<String> doInBackground(HashMap<String, String>... params) {
        //SystemClock.sleep(5000);
        Map<String, String> PostMap = params[0];
        int sizePost = PostMap.size();
        String action = PostMap.get("action");
        String url = host + appUrl + action;
        StringBuilder post_data = new StringBuilder();
        try {
            int i = 0;
            for (Map.Entry entry : PostMap.entrySet()) {
                post_data.append(URLEncoder.encode((String) entry.getKey(), ENCODING)).append("=")
                        .append(URLEncoder.encode((String) entry.getValue(), ENCODING));
                if (++i != sizePost) {
                    post_data.append("&");
                }
            }
        } catch (IOException error) {
            error.printStackTrace();
            return new AsyncTaskResult<>(error);
        }
        return sendPostRequest(url, post_data.toString());
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        Context context = contextRef.get();
        if (showLoading) {
            progressDialog = new LoadingDialog(context, false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }
    }

    @Override
    public void onPostExecute(AsyncTaskResult<String> result) {
        if (showLoading)
            progressDialog.dismiss();
        if (result.getError() != null || isCancelled()) {
            delegate.processFinish(false, result.getError().getMessage());
        } else {
            delegate.processFinish(true, result.getResult());
        }
    }

    @Override
    public void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    private AsyncTaskResult<String> sendPostRequest(String req_url, String post_data) {
        try {
            URL url = new URL(req_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream OutputStream = connection.getOutputStream();
            // send
            BufferedWriter BufferedWriter = new BufferedWriter(new OutputStreamWriter(OutputStream, ENCODING));
            BufferedWriter.write(post_data);
            BufferedWriter.flush();
            BufferedWriter.close();
            OutputStream.close();
            // receive
            InputStream InputStream = connection.getInputStream();
            BufferedReader BufferedReader = new BufferedReader(new InputStreamReader(InputStream, ENCODING));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = BufferedReader.readLine()) != null) {
                result.append(line);
            }
            BufferedReader.close();
            InputStream.close();
            connection.disconnect();
            return new AsyncTaskResult<>(result.toString());
        } catch (Exception error) {
            error.printStackTrace();
            return new AsyncTaskResult<>(error);
        }
    }
}