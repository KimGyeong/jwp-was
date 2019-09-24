package webserver.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpRequest {
    private final RequestLine requestLine;
    private final RequestHeader requestHeader;
    private final RequestBody requestBody;

    private HttpRequest(RequestLine requestLine, RequestHeader requestHeader, RequestBody requestBody) {
        this.requestLine = requestLine;
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;
    }

    public static HttpRequest of(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        RequestLine requestLine = RequestLine.of(br);
        RequestHeader requestHeader = RequestHeader.of(br);
        RequestBody requestBody = RequestBody.of(br, requestHeader.getContentLength());
        return new HttpRequest(requestLine, requestHeader, requestBody);
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public boolean isGet() {
        return requestLine.isGet();
    }

    public boolean hasParameters() {
        return requestLine.hasParameters();
    }

    public Map<String, String> getParameters() {
        return requestLine.getParameters();
    }

    public String getHttpVersion() {
        return requestLine.getHttpVersion();
    }

    public String getSource() {
        return requestLine.getPath();
    }
}
