package ie.roncon.core;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.JUnit4;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpForward;
import org.mockserver.verify.VerificationTimes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpForward.forward;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

// The tutorial followed for this project is located here:
// http://www.baeldung.com/mockserver

public class TestMockServer {
    private static ClientAndServer mockServer;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterClass
    public static void stopServer(){
        mockServer.stop();
    }

    // Stub for a POST request
    private void createExpectationForInvalidAuth() {
        new MockServerClient("127.0.0.1", 1080)
            .when(
                request()
                .withMethod("POST")
                .withPath("/validate")
                    .withHeader("\"ContentType\", \"application/json\"")
                    .withBody(exact("{username: 'foo', password: 'bar'}")), exactly(1))
                .respond(
                    response()
                        .withStatusCode(401)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody("{ message: 'incorrect username and password combination' }")
                    .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    // forwarding request (Here we can forward to a real server)
    private void createEpxectionForForward() {
        new MockServerClient("127.0.0.1", 1080)
            .when(
                request()
                    .withMethod("GET")
                    .withPath("/index.html"), exactly(1))
                .forward(
                    forward()
                        .withHost("www.mock-server.com")
                        .withPort(80)
                        .withScheme(HttpForward.Scheme.HTTP)
                );
    }

    // callback execution
    private void createExecutionForCallBack() {
        // the outer callback() specifies the callback action
        // and the inner callback() method specifies the instance of the callback method class.

        // when MockServer receives a request to "/callback", then the callback handle method implemented in the
        // class specific will be executed

        mockServer
                .when(
                        request().withPath("/callback"))
                .callback(
                        callback()
                            .withCallbackClass("ie.roncon.core.TestExpectationCallback")
                );
    }

    // check if the system under test sent a request (verify)
    private void verifyPostRequest() {
        new MockServerClient("localhost", 1080)
            .verify(
                    request()
                        .withMethod("POST")
                        .withPath("/validate")
                        .withBody(exact("{username: 'foo', password: 'bar'}")),
                    VerificationTimes.exactly(1)
            );
    }

    private void createBasicGetStub() {
        System.out.println("createBasicGetStub()");

        new MockServerClient("127.0.0.1", 1080)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/login")
//                                .withHeader("\"ContentType\", \"application/json\"")
//                                .withBody(exact("{username: 'foo', password: 'bar'}")), exactly(1))
                )
                .respond(
                        response()
                                .withStatusCode(200)
//                                .withHeaders(
//                                        new Header("Content-Type", "application/json; charset=utf-8"),
//                                        new Header("Cache-Control", "public, max-age=86400"))
                                .withBody("{ message: 'logged in' }")
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    @Test
    public void loginTest() throws IOException {
        System.out.println("loginTest()");
        createBasicGetStub();
        createHttpRequest();
    }

    // java http request:
    // http://www.baeldung.com/java-http-request
    private void createHttpRequest() throws IOException {
        System.out.println("createHttpRequest()");
        // Create request
//        URL url = new URL("127.0.0.1/login:1080");
//        URL url = new URL("HTTP", "127.0.0.1", 1080);
        URL url = new URL("HTTP", "127.0.0.1", 1080, "/login");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        // Add request parameters

        // set request headers

        // configure timeout

        // handle cookies

        // handle redirects

        // reading the response
        int status = con.getResponseCode();
//        String responseMessage = con.getResponseMessage();
//        InputStream body = con.getInputStream();

        System.out.println("status: " + status);
        assertEquals(status, 200);

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            System.out.println(line);
            assertEquals(line, "{ message: 'logged in' }");
        }

//        System.out.println("responseMessage: " + responseMessage);

        con.disconnect();
    }
}
