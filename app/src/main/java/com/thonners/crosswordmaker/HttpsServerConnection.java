package com.thonners.crosswordmaker;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

abstract class ServerConnectionTask {
    protected final String urlRoot = "https://crosswordtoolkit.mathonwythomas.com/api";
}

public class HttpsServerConnection {
    private final String LOG_TAG = "ServerConnection";

    private final ServerConnectionListener serverConnectionListener;
    private final int TIMEOUT = 10000; // Set timeout to 10s.


    /**
     * The interface through which responses from the server will be passed back to the initiating fragment/activity.
     */
    public interface ServerConnectionListener {
        void serverConnectionResponse(ServerConnection.SocketIdentifier requestSuccess, ArrayList<String> answers);

        void setServerAvailable(boolean serverAvailable);

        void callShowLoadingSpinner();

        void callHideLoadingSpinner();
    }

    /**
     * Constructor
     *
     * @param serverConnectionListener The interface through which responses from the server will be passed back to the initiating fragment/activity.
     */
    public HttpsServerConnection(ServerConnectionListener serverConnectionListener) {
        this.serverConnectionListener = serverConnectionListener;
        Log.d(LOG_TAG, "ServerConnection instance created.");
    }

    /**
     * Method to test the connection to the server, and if successful, to use the listener to call the appropriate method
     */
    public void testServerConnection() {
        Log.d(LOG_TAG, "Testing connection...");
        ServerConnectionTaskRunner runner = new ServerConnectionTaskRunner();
        runner.executeAsync(new ServerConnectionTestTask(), (testResult) -> {
            serverConnectionListener.setServerAvailable(testResult);
        });
    }

}

class ServerConnectionTaskRunner {

    private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
    }

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> {
            final R result;
            try {
                result = callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            handler.post(() -> {
                callback.onComplete(result);
            });
        });
    }

}

class ServerConnectionTestTask extends ServerConnectionTask implements Callable<Boolean> {

    String LOG_TAG = "ServerConnectionTestTask";

    @Override
    public Boolean call() throws Exception {

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlRoot);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.getInputStream().close();
            int responseCode = urlConnection.getResponseCode();
            Log.d(LOG_TAG, "Connection response code: " + responseCode);
            return responseCode == 200;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + urlRoot);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException during server communication: \n" + e.getLocalizedMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
    }
}

class ServerConnectionGETTask implements Callable<Boolean> {
    String targetURL;
    String LOG_TAG = "ServerConnectionGETTask";

    public ServerConnectionGETTask(String targetURL) {
        this.targetURL = targetURL;
    }

    @Override
    public Boolean call() throws Exception {

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(targetURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.getInputStream().close();
            int responseCode = urlConnection.getResponseCode();
            Log.d(LOG_TAG, "Connection response code: " + responseCode);
            return responseCode == 200;
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Malformed URL: " + targetURL);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException during server communication: \n" + e.getLocalizedMessage());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return false;
    }
}
