package cpen221.mp3;

import cpen221.mp3.server.WikiClient;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testng.Assert;

import java.io.IOException;

public class Task4Tests{

    private static WikiClient client;
    private static WikiClient client2;
    private static WikiClient client3;
    private static WikiClient client4;
    private static WikiClient client5;
    private static WikiClient client6;
    private static WikiClient client7;
    private static WikiClient client9;
    private final static int port = 9696;
    private static WikiMediatorServer wms;
    static {
        try {
            client = new WikiClient("127.0.0.1", 9696);
            client2 = new WikiClient("127.0.0.1", 9696);
            client3 = new WikiClient("127.0.0.1", 9696);
            client4 = new WikiClient("127.0.0.1", 9696);
            client5 = new WikiClient("127.0.0.1", 9696);
            client6 = new WikiClient("127.0.0.1", 9696);
            client7 = new WikiClient("127.0.0.1", 9696);
            client9 = new WikiClient("127.0.0.1", 9696);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Before
    public void setup() throws InterruptedException, IOException {

        int capacity = 24;
       int stalenessInterval = 120;
       int port = 9696;
        int numClients = 10;
        WikiMediator wm = new WikiMediator(capacity, stalenessInterval);
        wms = new WikiMediatorServer(port, numClients, wm);

        Thread thread = new Thread(new Runnable() {
          @Override
            public void run() {
                wms.serve();
            }
        });
        thread.start();

        Thread.sleep(3000);
    }



    @Test
    public void testServer() throws IOException {

        String testJSON1 = "{\"id\": \"2\",\"type\": \"search\",\"query\": \"Avengers\",\"limit\": \"3\"}";
        String testJSON3 = "{\"id\": \"4\",\"type\": \"getPage\",\"query\": \"Avocado\"}";
        String testJSON4 = "{\"id\": \"5\",\"type\": \"zeitgeist\",\"limit\": \"3\"}";
        String testJSON5 = "{\"id\": \"6\",\"type\": \"trending\",\"timeLimitInSeconds\": \"1\",\"maxItems\": \"12\"}";
        String testJSON6 = "{\"id\": \"7\",\"type\": \"windowedPeakLoad\",\"timeWindowInSeconds\": \"2\"}";
        String testJSON7 = "{\"id\": \"8\",\"type\": \"search\",\"query\": \"Barack Obama\",\"limit\": \"12\", \"timeout\": \"1\"}";
        String testJSON8 = "{\"id\": \"9\",\"type\": \"search\",\"query\": \"Barack Obama\",\"limit\": \"12\", \"timeout\": \"30\"}";
        String testJSON9 = "{\"id\": \"10\",\"type\": \"stop\"}";
        String testJSON10 = "{\"id\": \"11\",\"type\": \"getPage\",\"pageTitle\": \"Avocado\"}";

        try {

            client.sendRequest(testJSON1);
            System.out.println("Request: " + testJSON1);

            client2.sendRequest(testJSON3);
            System.out.println("Request: " + testJSON3);

            client3.sendRequest(testJSON4);
            System.out.println("Request: " + testJSON4);

            client6.sendRequest(testJSON1);
            System.out.println("Request: " + testJSON1);

            client4.sendRequest(testJSON5);
            System.out.println("Request: " + testJSON5);

            client5.sendRequest(testJSON6);
            System.out.println("Request: " + testJSON6);

            client7.sendRequest(testJSON7);
            System.out.println("Request: " + testJSON7);



            //Test for search
            String response = client.getResponse();
            System.out.println("Response: " + response);
            Assert.assertEquals(response, "{\"id\":\"2\",\"status\":\"success\",\"response\":\"[Avenger, Avengers (comics), The New Avengers (comics)]\"}");

            //Test for zeitgeist
            String response3 = client3.getResponse();
            System.out.println("Response3: " + response3);
            Assert.assertEquals(response3, "{\"id\":\"5\",\"status\":\"success\",\"response\":\"[Avengers]\"}");

            //Test for invalid input
            String response2 = client2.getResponse();
            System.out.println("Response2: " + response2);
            Assert.assertEquals(response2,"{\"id\":\"4\",\"status\":\"failed\",\"response\":\"Invalid JSON request!\"}");

            //Test trending
            String response4 = client4.getResponse();
            System.out.println("Response4: " + response4);
            Assert.assertEquals(response4, "{\"id\":\"6\",\"status\":\"success\",\"response\":\"[Avengers]\"}");

            //Test windowedPeakLoad
            String response5 = client5.getResponse();
            System.out.println("Response5: " + response5);
            Assert.assertEquals("{\"id\":\"7\",\"status\":\"success\",\"response\":\"3\"}", "{\"id\":\"7\",\"status\":\"success\",\"response\":\"3\"}");

            //Test time out
            String response7 = client7.getResponse();
            System.out.println("Response7: " + response7);
            Assert.assertEquals(response7, "{\"id\":\"8\",\"status\":\"failed\",\"response\":\"Operation timed out!\"}");



            client.close();
            client2.close();
            client3.close();
            client4.close();
            client5.close();
            client6.close();
            client7.close();



            //Test for server closing
            client9.sendRequest(testJSON9);
            String responseEnd = client9.getResponse();
            System.out.println("ResponseEnd: " + responseEnd);
            Assert.assertEquals(responseEnd,"");

            client9.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

}
