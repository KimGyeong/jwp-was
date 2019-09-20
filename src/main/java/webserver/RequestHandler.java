package webserver;

import db.DataBase;
import http.request.HttpMethod;
import http.request.HttpRequest;
import http.request.HttpRequestFactory;
import http.request.QueryParams;
import http.response.HttpResponse;
import http.response.HttpResponseBody;
import http.response.HttpResponseFactory;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String DEFAULT_PATH = "./templates";
    private static final String STATIC_PATH = "./static";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = HttpRequestFactory.makeHttpRequest(in);
            logger.debug(request.toString());

            DataOutputStream dos = new DataOutputStream(out);

            if (HttpMethod.GET.match(request.getMethod())) {
                byte[] body;
                try {
                    body = FileIoUtils.loadFileFromClasspath(DEFAULT_PATH + request.getUrl().getPath());
                } catch (NullPointerException e) {
                    body = FileIoUtils.loadFileFromClasspath(STATIC_PATH + request.getUrl().getPath());
                }

                if (request.getHeaders().getHeader("Accept").contains("text/css")) {
                    HttpResponseBody responseBody = new HttpResponseBody(body, "text/css");
                    HttpResponse response = HttpResponseFactory.makeHttp200Response(responseBody);
                    dos.writeBytes(response.getHeaderMessage());
                    dos.write(response.getBody());
                } else {
                    HttpResponseBody responseBody = new HttpResponseBody(body, "text/html;charset=utf-8");
                    HttpResponse response = HttpResponseFactory.makeHttp200Response(responseBody);
                    dos.writeBytes(response.getHeaderMessage());
                    dos.write(response.getBody());
                }
            }

            if (HttpMethod.POST.match(request.getMethod()) && request.getUrl().getPath().equals("/user/create")) {
                createUser(request.getQueryParams());
                HttpResponse response = HttpResponseFactory.makeHttp302Response("http://localhost:8080/index.html");
                dos.writeBytes(response.getHeaderMessage());
            }
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private void createUser(QueryParams queryParams) {
        User user = new User(queryParams.getParam("userId"), queryParams.getParam("password"),
                queryParams.getParam("name"), queryParams.getParam("email"));
        DataBase.addUser(user);
        logger.debug(DataBase.findUserById(user.getUserId()).toString());
    }
}
