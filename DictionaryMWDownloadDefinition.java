package com.thonners.crosswordmaker;

import android.content.Context;
import android.os.AsyncTask;
import android.app.Activity;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by mat on 02/02/15.
 */
public class DictionaryMWDownloadDefinition extends AsyncTask<Void,Void,String> {

    private static final String LOG_TAG = "DictionaryMWDownloadDefinition" ;

    public static interface DictionaryMWDownloadDefinitionListener {
        public abstract void completionCallBack(String definition);
    }
    public DictionaryMWDownloadDefinitionListener listener;
    private Context context ;
    public String searchTerm;
    public String definition ;

    private String urlPrefix = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
    private String urlSuffix = "?key=";
    private String url ;

    public DictionaryMWDownloadDefinition (Context appContext, String aSearchTerm, DictionaryMWDownloadDefinitionListener aListener) {
        context = appContext ;
        listener = aListener;
        searchTerm = aSearchTerm;

        url = urlPrefix + searchTerm + urlSuffix + context.getString(R.string.dictionary_mw_api_key);
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);
        String xmlRaw = "";
        try {
            HttpResponse response = client.execute(request);
            InputStream in;
            in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                str.append(line);
            }
            in.close();
            xmlRaw = str.toString();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            Log.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(LOG_TAG, e.getMessage());
        }
        return xmlRaw;
    }

    @Override
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if (!isCancelled()) {
            listener.completionCallBack(result);
        }
    }
}
