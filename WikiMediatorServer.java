package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.*;


/**
 * A WikiMediatorServer is a server application that wraps a WikiMediator
 * instance. The server receives requests over a network socket and returns
 * results appropriately. It is capable of handling more than one request
 * simultaneously.
 *
 * Abstraction Function:
 * - serverSocket represents the network socket that connects the server
 *   to the client
 * - wikiPort is the port number used to connect the server and the client
 * - numConc is the number of concurrent requests the server can handle
 * - WikiMediator is the WikiMediator instance to use for the server
 */
public class WikiMediatorServer extends Thread{
    /* default success message */
    private final static String SUCCESS = "success";

    /* default failure message */
    private final static String FAILED = "failed";

    private ServerSocket serverSocket;
    private final int WikiPort;
    private final int numConc;
    private final WikiMediator WikiMediator;

    /*
     * Representation Invariant:
     *  - serverSocket != null
     */

    /**
     * Check that the representation invariants for WikiMediatorServer hold true.
     * @throws RuntimeException if any representation invariants are violated.
     */
    private void checkRep() {
        if (serverSocket == null) {
            throw new RuntimeException("serverSocket must not be null");
        }
    }

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) {
        WikiPort = port;
        numConc = n;
        WikiMediator = wikiMediator;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            System.out.println("Could not start a WikiMediatorServer at the given port number.");
        }
        checkRep();
    }

    /**
     * Run the server, listening for connections and handling them.
     *
     * @throws RuntimeException if the main server socket is broken. Throwing
     * an IOException will not terminate serve(), but a stack trace will
     * be printed.
     */
    public void serve() throws RuntimeException {
        while (true) {
            try {
                final Socket socket = serverSocket.accept();

                Thread handler = new Thread(() -> {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                });

                handler.start();
            }
            catch (IOException ioe){
                throw new RuntimeException();
            }
        }
    }

    /**
     * Handle one client connection by having the server interpret and act upon requests.
     * Returns when client disconnects.
     * @param socket The socket where client is connected.
     * @throws RuntimeException If the connection encounters an error.
     */
    private void handle(Socket socket) throws RuntimeException {
        System.err.println("Client connected.");

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream()), true);

            try{
                Gson gson = new Gson();

                for (String line = in.readLine(); line != null; line = in.readLine()) {
                    Request request = gson.fromJson(line, Request.class);
                    String id = request.getId();
                    String type = request.getType();
                    String timeout = request.getTimeout();

                    Response responseObject;
                    String response;

                    try {
                        request.checkValidRequest();

                        if (timeout != null) {
                            this.executeTimeout(request, out);
                        }

                        else {
                            if (Objects.equals(type, "search")) {
                                String query = request.getQuery();
                                int limit = Integer.parseInt(request.getLimit());

                                response = String.valueOf(WikiMediator.search(query, limit));
                                responseObject = new Response(id, SUCCESS, response);
                                sendResponse(responseObject, out);
                            }

                            else if (Objects.equals(type, "getPage")) {
                                String pageTitle = request.getPageTitle();

                                response = WikiMediator.getPage(pageTitle);
                                responseObject = new Response(id, SUCCESS, response);
                                sendResponse(responseObject, out);
                            }

                            else if (Objects.equals(type, "zeitgeist")) {
                                int limit = Integer.parseInt(request.getLimit());

                                response = String.valueOf(WikiMediator.zeitgeist(limit));
                                responseObject = new Response(id, SUCCESS, response);
                                sendResponse(responseObject, out);
                            }

                            else if (Objects.equals(type, "trending")) {
                                int timeLimitInSeconds = Integer.parseInt(request.getTimeLimitInSeconds());
                                int maxItems = Integer.parseInt(request.getMaxItems());

                                response = String.valueOf(WikiMediator.trending(timeLimitInSeconds, maxItems));
                                responseObject = new Response(id, SUCCESS, response);
                                sendResponse(responseObject, out);
                            }

                            else if (Objects.equals(type, "windowedPeakLoad")) {
                                if (request.getTimeWindowInSeconds() == null) {
                                    response = String.valueOf(WikiMediator.windowedPeakLoad());
                                }
                                else {
                                    int timeWindowInSeconds = Integer.parseInt(request.getTimeWindowInSeconds());
                                    response = String.valueOf(WikiMediator.windowedPeakLoad(timeWindowInSeconds));
                                }
                                responseObject = new Response(id, SUCCESS, response);
                                sendResponse(responseObject, out);
                            }
                        }
                    }
                    catch (JsonFormatException jfe) {
                        responseObject = new Response(id, FAILED, "Invalid JSON request!");
                        sendResponse(responseObject, out);
                    }
                }
            } finally {
                out.close();
                in.close();
            }
        }
        catch (IOException ioe) {
            throw new RuntimeException();
        }
    }

    /**
     * Executes the operation specified by request and sends the response back to
     * the client.
     * @param request request to handle, must not be null and must have a timeout
     *                instance variable > 0
     * @param out communication manager that takes response obtained by handling
     *            request and sends it to client
     */
    private void executeTimeout(Request request, PrintWriter out) {
        String id = request.getId();
        String type = request.getType();
        long timeout = Long.parseLong(request.getTimeout());

        if (Objects.equals(type, "search")) {
            String query = request.getQuery();
            int limit = Integer.parseInt(request.getLimit());
            Callable<Object> task = () -> WikiMediator.search(query, limit);
            executeTimeoutTask(task, out, id, timeout);
        }

        else if (Objects.equals(type, "getPage")) {
            String pageTitle = request.getPageTitle();
            Callable<Object> task = () -> WikiMediator.getPage(pageTitle);
            executeTimeoutTask(task, out, id, timeout);
        }

        else if (Objects.equals(type, "zeitgeist")) {
            int limit = Integer.parseInt(request.getLimit());
            Callable<Object> task = () -> WikiMediator.zeitgeist(limit);
            executeTimeoutTask(task, out, id, timeout);
        }

        else if (Objects.equals(type, "trending")) {
            int timeLimitInSeconds = Integer.parseInt(request.getTimeLimitInSeconds());
            int maxItems = Integer.parseInt(request.getMaxItems());
            Callable<Object> task = () -> WikiMediator.trending(timeLimitInSeconds, maxItems);
            executeTimeoutTask(task, out, id, timeout);
        }

        else if (Objects.equals(type, "windowedPeakLoad")) {
            if (request.getTimeWindowInSeconds() != null) {
                int timeWindowInSeconds = Integer.parseInt(request.getTimeWindowInSeconds());
                Callable<Object> task = () -> WikiMediator.windowedPeakLoad(timeWindowInSeconds);
                executeTimeoutTask(task, out, id, timeout);
            }
            else {
                Callable<Object> task = WikiMediator::windowedPeakLoad;
                executeTimeoutTask(task, out, id, timeout);
            }
        }
    }

    /**
     * Executes the operation specified by task and sends the response back to the client.
     * Sends a failure response if the operation execution time exceeds
     * timeout. Otherwise, sends a success response containing the result of the
     * operation.
     * @param task task that specifies the operation to execute
     * @param out communication manager that takes response obtained by handling
     *            request and sends it to client
     * @param id id to include in the response
     * @param timeout timeout time in seconds that specifies how long the operation
     *                should execute before sending a failure response to client
     */
    private static void executeTimeoutTask(Callable<Object> task, PrintWriter out,
                                           String id, long timeout) {
        Response responseObject;
        String response;
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Object> future = executor.submit(task);

        try {
            Object result = future.get(timeout, TimeUnit.SECONDS);
            response = String.valueOf(result);
            responseObject = new Response(id, SUCCESS, response);
            sendResponse(responseObject, out);
        } catch (TimeoutException ex) {
            responseObject = new Response(id, FAILED, "Operation timed out!");
            sendResponse(responseObject, out);
        } catch (InterruptedException e) {
            responseObject = new Response(id, FAILED, "Operation interrupted unexpectedly!");
            sendResponse(responseObject, out);
        } catch (ExecutionException e) {
            responseObject = new Response(id, FAILED, "Could not retrieve result of operation!!");
            sendResponse(responseObject, out);
        } finally {
            future.cancel(true);
        }
    }

    /**
     * Sends a response from the WikiMediatorServer to the client.
     * @param response contains the information that is to be included in the
     *                 response sent to the client. response != null.
     * @param out communication manager that takes response obtained by handling
     *            request and sends it to client
     */
    private static void sendResponse(Response response, PrintWriter out) {
        Gson gson = new Gson();
        String JsonResponse = gson.toJson(response);
        out.println(JsonResponse + "\n");
    }

    public static void main(String[] args) {
        int capacity = 24;
        int stalenessInterval = 120;
        int port = 9696;
        int numClients = 10;

        WikiMediator wm = new WikiMediator(capacity, stalenessInterval);
        WikiMediatorServer wms = new WikiMediatorServer(port, numClients, wm);
        wms.serve();
    }

}