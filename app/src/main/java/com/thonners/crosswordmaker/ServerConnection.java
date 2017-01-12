package com.thonners.crosswordmaker;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

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

    /**
     * @return Whether the server can be reached.
     */
    public boolean testServerConnection() {
        Log.d(LOG_TAG, "Testing connection...") ;
        return transferData(SocketIdentifier.CONNECTION_TEST) == SocketIdentifier.CONNECTION_TEST_SUCCESSFUL ;
    }

    /**
     * Method to manage the interaction with the server
     * @param requestIdentifier
     * @return
     */
    private SocketIdentifier transferData(SocketIdentifier requestIdentifier) {
        try {
            Log.d(LOG_TAG, "Creating socket...") ;
            Socket socket = new Socket(serverURL, serverPort);

            // Streams
            DataOutputStream dOut = null ;
            DataInputStream dIn = null ;

            Log.d(LOG_TAG, "Socket connection created successfully. Sending data...") ;
                    try {   // Try with resources requires API 19 (min = 17)
                        dOut = new DataOutputStream(socket.getOutputStream()) ;
                        dOut.writeByte(requestIdentifier.id());
                        dOut.flush();

                        dIn = new DataInputStream(socket.getInputStream()) ;
                        byte responseIdentifier = dIn.readByte() ;

                        return SocketIdentifier.getSocketIdentifierFromByte(responseIdentifier) ;

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
}
