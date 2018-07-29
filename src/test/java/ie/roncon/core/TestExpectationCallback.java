package ie.roncon.core;

import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.HttpResponse.response;

public class TestExpectationCallback implements ExpectationCallback {
    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        System.out.println("In `TestExpectationCallback`");
        System.out.println("httpRequest.getPath().getValue(): " + httpRequest.getPath().getValue());

        if(httpRequest.getPath().getValue().endsWith("/callback")){
            return httpResponse;
        }
        else {
            return notFoundResponse();
        }
    }

    public static HttpResponse httpResponse = response()
        .withStatusCode(200);
}
