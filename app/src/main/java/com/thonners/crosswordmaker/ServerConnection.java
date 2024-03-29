package com.thonners.crosswordmaker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

/**
 * Class to manage conections to the Crossword Toolkit Server
 *
 * @author M Thomas
 * @since 12/01/17.
 */

public class ServerConnection {

    private final String LOG_TAG = "ServerConnection" ;

    private final ServerConnectionListener serverConnectionListener ;

    /**
     * Constructor
     * @param serverConnectionListener The interface through which responses from the server will be passed back to the initiating fragment/activity.
     */
    public ServerConnection(ServerConnectionListener serverConnectionListener) {
            this.serverConnectionListener = serverConnectionListener ;
            Log.d(LOG_TAG, "ServerConnection instance created.") ;
    }

    /**
     * The interface through which responses from the server will be passed back to the initiating fragment/activity.
     */
    public interface ServerConnectionListener {
        void serverConnectionResponse(SocketIdentifier requestSuccess, ArrayList<String> answers) ;
        void setServerAvailable(boolean serverAvailable) ;
        void callShowLoadingSpinner();
        void callHideLoadingSpinner();
    }

    /**
     * Method to test the connection to the server, and if successful, to use the listener to call the appropriate method
     */
    public void testServerConnection() {
        Log.d(LOG_TAG, "Testing connection...") ;
        DataTransfer.DataTransferListener listener = new DataTransfer.DataTransferListener() {
            @Override
            public void serverCallback(Connection resultConnection) {
                Log.d(LOG_TAG,"ServerCallback called (inside testServerConnection).");
                if (resultConnection != null && resultConnection.getResultIdentifier() == SocketIdentifier.CONNECTION_TEST_SUCCESSFUL) {
                    serverConnectionListener.setServerAvailable(true);
                } else {
                    serverConnectionListener.setServerAvailable(false);
                }
            }
        } ;
        // Create a dataTransfer instance with the appropriate inputs
        DataTransfer dataTransfer = new DataTransfer(new Connection(SocketIdentifier.CONNECTION_TEST, ""), listener);
        //dataTransfer.execute() ;
        dataTransfer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Method to solicit the answers from the server for the word-fit given.
     * Results will be passed back by the ServerConnectionListener.
     *
     * @param input The String describing the word-fit request, with '.'s in place of unknown letters.
     */
    public void getWordFitResults(String input) {
        Log.d(LOG_TAG, "Getting word-fit solutions for: " + input) ;
        DataTransfer.DataTransferListener listener = new DataTransfer.DataTransferListener() {
            @Override
            public void serverCallback(Connection resultConnection) {
                Log.d(LOG_TAG,"ServerCallback called (inside getWordFitResults).");
                serverConnectionListener.callHideLoadingSpinner();
                if (resultConnection != null && resultConnection.getResultIdentifier() == SocketIdentifier.WORD_FIT_SOLUTIONS_SUCCESS) {
                    serverConnectionListener.serverConnectionResponse(SocketIdentifier.WORD_FIT_SOLUTIONS_SUCCESS, resultConnection.getResult());
                } else {
                    serverConnectionListener.serverConnectionResponse(SocketIdentifier.WORD_FIT_SOLUTIONS_EMPTY, null);
                }

            }
        } ;
        // Create a dataTransfer instance with the appropriate inputs
        DataTransfer dataTransfer = new DataTransfer(new Connection(SocketIdentifier.WORD_FIT, input), listener);
        serverConnectionListener.callShowLoadingSpinner();
        dataTransfer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Method to solicit the answers from the server for the anagram given.
     * Results will be passed back by the ServerConnectionListener.
     *
     * @param input The String of the letters in the anagram.
     */
    public void getAnagramResults(String input) {
        Log.d(LOG_TAG, "Getting anagram solutions for: " + input) ;
        DataTransfer.DataTransferListener listener = new DataTransfer.DataTransferListener() {
            @Override
            public void serverCallback(Connection resultConnection) {
                Log.d(LOG_TAG,"ServerCallback called (inside getAnagramResults).");
                serverConnectionListener.callHideLoadingSpinner();
                if (resultConnection != null && resultConnection.getResultIdentifier() == SocketIdentifier.ANAGRAM_SOLUTIONS_SUCCESS) {
                    Log.d(LOG_TAG,"Results for anagram: " + resultConnection.getInput() + " = " + resultConnection.getResult().toString()) ;
                    serverConnectionListener.serverConnectionResponse(SocketIdentifier.ANAGRAM_SOLUTIONS_SUCCESS, resultConnection.getResult());
                } else {
                    Log.d(LOG_TAG,"No results for anagram: " + resultConnection.getInput()) ;
                    serverConnectionListener.serverConnectionResponse(SocketIdentifier.ANAGRAM_SOLUTIONS_EMPTY, null);
                }

            }
        } ;
        // Create a dataTransfer instance with the appropriate inputs
        DataTransfer dataTransfer = new DataTransfer(new Connection(SocketIdentifier.ANAGRAM, input), listener);
        //dataTransfer.execute() ;
        serverConnectionListener.callShowLoadingSpinner();
        dataTransfer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    /**
     * Enum to identify the type of connection requested from a client, when connecting to the server.
     *
     * Master enum file in CrosswordToolkitServer project. This is just a copy.
     */
    public enum SocketIdentifier {
        // Connection tests
        CONNECTION_TEST((byte) 1),
        CONNECTION_TEST_SUCCESSFUL((byte) 2),
        // New crosswords
        NEW_CROSSWORD_CHECK((byte) 10),
        DOWNLOAD_CROSSWORD_GRID((byte) 20),
        SAVE_NEW_CROSSWORD((byte) 30),
        // Saving progress
        SAVE_PROGRESS((byte) 40),
        SAVE_PROGRESS_SUCCESS((byte) 41),
        // Anagrams
        ANAGRAM((byte) 100),
        ANAGRAM_SOLUTIONS_EMPTY((byte) 101),
        ANAGRAM_SOLUTIONS_SUCCESS((byte) 102),
        // Word fit
        WORD_FIT((byte) 110),
        WORD_FIT_SOLUTIONS_EMPTY((byte) 111),
        WORD_FIT_SOLUTIONS_SUCCESS((byte) 112);

        private byte id ;

        SocketIdentifier(byte id) {
            this.id = id ;
        }

        public byte id() {
            return id ;
        }

        /**
         * Method to turn a byte into a SocketIdentifier
         * @param input The byte received from the connected client
         * @return The SocketIdentifier related to the received byte
         */
        public static SocketIdentifier getSocketIdentifierFromByte(byte input) {
            for (SocketIdentifier si : SocketIdentifier.values()) {
                if (si.id == input) {
                    return si ;
                }
            }
            // If it doesn't match...
            throw new IllegalArgumentException();
        }
    }


    /**
     * Class to hold the connection request details and the response
     */
    private class Connection {
        private SocketIdentifier requestIdentifier;
        private SocketIdentifier resultIdentifier;
        private String input ;
        private ArrayList<String> result = null;

        /**
         * Constructor
         * @param socketIdentifier The SocketIdentifier enum to identify the type of request being made of the server
         * @param input The String to go with the request, if the request is to solve an anagram or word-fit.
         */
        public Connection(SocketIdentifier socketIdentifier, String input) {
            this.requestIdentifier = socketIdentifier ;
            this.input = input ;
        }

        /**
         * @return The resuts received from the server
         */
        public ArrayList<String> getResult() {
            return result ;
        }

        /**
         * @return The 'input' to the solver request, i.e. the search string
         */
        public String getInput() {
            return input;
        }

        /**
         * @return The SocketIdentifier enum of the request
         */
        public SocketIdentifier getRequestIdentifier() {
            return requestIdentifier;
        }

        /**
         * @return The SocketIdentifier enum of the result - received from the server
         */
        public SocketIdentifier getResultIdentifier() {
            return resultIdentifier;
        }

        /**
         * @param resultIdentifier The SocketIdentifier enum of the result - received from the server
         */
        public void setResultIdentifier(SocketIdentifier resultIdentifier) {
            this.resultIdentifier = resultIdentifier;
        }

        /**
         * @param result The results received from the server
         */
        public void setResult(ArrayList<String> result) {
            this.result = result;
        }
    }

    /**
     * ASyncTask to communicate with the CrosswordToolkitServer.
     */
    private static class DataTransfer extends AsyncTask<Void, Integer, Connection> {

        private final String LOG_TAG = "DataTransfer" ;
        private final int TIMEOUT = 10000 ; // Set timeout to 10s.
        private final int serverPort = 28496 ;
        private final String serverURL = "www.mathonwythomas.com" ;

        private Connection returnConnection ;
        private DataTransferListener listener ;

        /**
         * Constructor
         * @param inputConnection The Connection instance which holds the request information - the type of request and any inputs.
         * @param listener The DataTransferListener which will be used to interact with the activity/fragment once the ASyncTask returns.
         */
        public DataTransfer(Connection inputConnection, DataTransferListener listener) {
            this.returnConnection = inputConnection ;
            this.listener = listener ;
        }

        /**
         * The listener which will be used to pass the results back to the vcalling activity.
         */
        public interface DataTransferListener {
            void serverCallback(Connection returnConnection) ;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(LOG_TAG, "onPreExecuteCalled");
        }

        /**
         * The method which is actually run in a background thread. This creates the connection,
         * sends the request and input data, and received any results/responses from the server.
         * @param params Required field, but this is always null
         * @return The Connection instance with results data populated from the server's response.
         */
        @Override
        protected Connection doInBackground(Void... params) {
            try {
                Log.d(LOG_TAG, "Creating socket...") ;
                Socket socket = new Socket(serverURL, serverPort);
                // Set a timeout so that it won't hang indefinitely if the server can't be reached
                socket.setSoTimeout(TIMEOUT);

                // Streams
                DataOutputStream dOut = null ;
                DataInputStream dIn = null ;

                Log.d(LOG_TAG, "Socket connection created successfully. Sending data...") ;
                try {   // Try with resources requires API 19 (min = 17)
                    dOut = new DataOutputStream(socket.getOutputStream()) ;
                    dOut.writeByte(returnConnection.getRequestIdentifier().id());
                    if (returnConnection.getRequestIdentifier() == SocketIdentifier.ANAGRAM || returnConnection.getRequestIdentifier() == SocketIdentifier.WORD_FIT) {
                        dOut.writeUTF(returnConnection.getInput());
                    }
                    dOut.flush();

                    dIn = new DataInputStream(socket.getInputStream()) ;
                    byte responseIdentifier = dIn.readByte() ;
                    Log.d(LOG_TAG,"Received byte: " + responseIdentifier) ;
                    // Set the response
                    returnConnection.setResultIdentifier(SocketIdentifier.getSocketIdentifierFromByte(responseIdentifier)) ;

                    if(returnConnection.getResultIdentifier() == SocketIdentifier.ANAGRAM_SOLUTIONS_SUCCESS || returnConnection.getResultIdentifier() == SocketIdentifier.WORD_FIT_SOLUTIONS_SUCCESS) {
                        ArrayList<String> answers = new ArrayList<>();
                        // Wait until EOF is received, to signify end of data transmission
                        while(true) {
                            try {
                                String answer = dIn.readUTF() ;
                                answers.add(answer) ;
                                Log.d(LOG_TAG,"Answer received = " + answer) ;
                            } catch(EOFException e) {
                                Log.d(LOG_TAG,"EOFException caught - must be end of answers") ;
                                break;
                            }
                        }
                        returnConnection.setResult(answers) ;
                    }

                    return returnConnection ;

                } catch (SocketTimeoutException sTO) {

                } catch (Exception e) {

                } finally {
                    // Close the streams
                    if (dOut != null) dOut.close();
                    if (dIn != null) dIn.close();
                }
            } catch (Exception e) {
                Log.d(LOG_TAG,"Error creating connection to server. " + e.getLocalizedMessage()) ;
            }
            // If we get this far, something's gone wrong.
            return null ;
        }

        /**
         * Executed on the UI thread once the doInBackground method has returned.
         * @param returnConnection The Connection instance with results data populated from the server's response.
         */
        @Override
        protected void onPostExecute(Connection returnConnection) {
            super.onPostExecute(returnConnection);
            if (returnConnection != null) {
                Log.d(LOG_TAG, "Server connection successful! ASyncTask complete.") ;
            } else {
                Log.d(LOG_TAG, "Server connection failed! ASyncTask complete.") ;
            }
            listener.serverCallback(returnConnection);
        }
    }
}
