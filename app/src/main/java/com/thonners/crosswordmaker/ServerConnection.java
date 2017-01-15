package com.thonners.crosswordmaker;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Class to manage conections to the Crossword Toolkit Server
 *
 * @author M Thomas
 * @since 12/01/17.
 */

public class ServerConnection {

    private final String LOG_TAG = "ServerConnection" ;

    private final int serverPort = 28496 ;
    private final String serverURL = "thonners.ddns.net" ;

    public ServerConnection() {

            Log.d(LOG_TAG, "ServerConnection instance created.") ;
    }

    public interface ServerConnectionListener {
        void serverConnectionResponse() ;
        void setServerAvailable() ;
    }

    /**
     * @return Whether the server can be reached.
     */
    public void testServerConnection(final ServerConnectionListener serverConnectionListener) {
        Log.d(LOG_TAG, "Testing connection...") ;
        DataTransfer.DataTransferListener listener = new DataTransfer.DataTransferListener() {
            @Override
            public void serverCallback(Connection resultConnection) {
                if (resultConnection.getResultIdentifier() == SocketIdentifier.CONNECTION_TEST_SUCCESSFUL) {
                    serverConnectionListener.setServerAvailable();
                }
            }
        } ;
        // Create a dataTransfer instance with the appropriate inputs
        DataTransfer dataTransfer = new DataTransfer(new Connection(SocketIdentifier.CONNECTION_TEST, ""), listener);
        dataTransfer.execute() ;
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

        public Connection(SocketIdentifier socketIdentifier, String input) {
            this.requestIdentifier = socketIdentifier ;
            this.input = input ;
        }

        public ArrayList<String> getResult() {
            return result ;
        }

        public SocketIdentifier getRequestIdentifier() {
            return requestIdentifier;
        }

        public SocketIdentifier getResultIdentifier() {
            return resultIdentifier;
        }

        public void setResultIdentifier(SocketIdentifier resultIdentifier) {
            this.resultIdentifier = resultIdentifier;
        }
    }

    /**
     * ASyncTask to communicate with the CrosswordToolkitServer.
     */
    private static class DataTransfer extends AsyncTask<Void, Integer, Connection> {

        private final String LOG_TAG = "DataTransfer" ;
        private final int serverPort = 28496 ;
        private final String serverURL = "thonners.ddns.net" ;

        private Connection returnConnection ;
        private DataTransferListener listener ;

        public DataTransfer(Connection inputConnection, DataTransferListener listener) {
            this.returnConnection = inputConnection ;
            this.listener = listener ;
        }

        public interface DataTransferListener {
            void serverCallback(Connection returnConnection) ;
        }

        protected Connection doInBackground(Void... params) {
            try {
                Log.d(LOG_TAG, "Creating socket...") ;
                Socket socket = new Socket(serverURL, serverPort);

                // Streams
                DataOutputStream dOut = null ;
                DataInputStream dIn = null ;

                Log.d(LOG_TAG, "Socket connection created successfully. Sending data...") ;
                try {   // Try with resources requires API 19 (min = 17)
                    dOut = new DataOutputStream(socket.getOutputStream()) ;
                    dOut.writeByte(returnConnection.getRequestIdentifier().id());
                    dOut.flush();

                    dIn = new DataInputStream(socket.getInputStream()) ;
                    byte responseIdentifier = dIn.readByte() ;

                    // Set the response
                    returnConnection.setResultIdentifier(SocketIdentifier.getSocketIdentifierFromByte(responseIdentifier)) ;

                    if(returnConnection.getResultIdentifier() == SocketIdentifier.ANAGRAM_SOLUTIONS_SUCCESS) {
                        // TODO: add stuff to save results
                    }

                    return returnConnection ;

                } catch (Exception e) {

                } finally {
                    // Close the streams
                    if (dOut != null) dOut.close();
                    if (dIn != null) dIn.close();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG,"Error creating connection to server. " + e.getLocalizedMessage()) ;
                e.printStackTrace();
            }
            // If we get this far, something's gone wrong.
            return null ;
        }

        @Override
        protected void onPostExecute(Connection returnConnection) {
            super.onPostExecute(returnConnection);


            if (returnConnection.getResultIdentifier() == SocketIdentifier.CONNECTION_TEST_SUCCESSFUL) {
                Log.d(LOG_TAG, "Server connection test successful!") ;
            } else {
                Log.d(LOG_TAG, "Server connection test unsuccessful!") ;
            }
        }
    }
}
