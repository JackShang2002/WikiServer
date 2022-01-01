package cpen221.mp3.server;

import java.io.*;
import java.net.Socket;

/**
 * A WikiClient is a client that sends requests to the WikiMediatorServer
 * and interprets its replies.
 * A new WikiClient is "open" until the close() method is called,
 * at which point it is "closed" and may not be used further.
 */
public class WikiClient {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    // Rep invariant: socket, in, out != null

    /**
     * Check that the representation invariants for this WikiClient hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private void checkRep() {
        if (socket == null) {
            throw new RuntimeException("socket must not be null");
        }
        else if (in == null) {
            throw new RuntimeException("in must not be null");
        }
        else if (out == null) {
            throw new RuntimeException("out must not be null");
        }
    }
    
    /**
     * Make a WikiClient and connect it to a server running a
     * hostname at the specified port.
     *
     * @throws IOException if the WikiClient cannot connect to the server
     */
    public WikiClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        checkRep();
    }

    /**
     * Send a request to the server. Requires this is "open".
     *
     * @param request Request to send to server, must be a one-line JSON-formatted String
     */
    public void sendRequest(String request) {
        out.print(request + "\n");
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     *
     * @return the response from the server in a JSON-formatted String
     * @throws IOException if network or server failure
     */
    public String getResponse() throws IOException {
        String response = in.readLine();

        if (response == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        return response;
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     *
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
