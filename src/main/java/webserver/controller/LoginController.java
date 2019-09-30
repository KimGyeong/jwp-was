package webserver.controller;

import http.request.Request;
import http.response.Response;
import http.session.Session;
import model.LoginService;
import model.exception.LoginFailException;

public class LoginController extends HttpController {
    private LoginController() {
    }

    public static LoginController getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    protected void doPost(Request request, Response response) {
        Session session = request.getSession();
        try {
            LoginService.getInstance().login(request.extractFormData());
            session.setAttribute("logined", "true");
            response.redirect("/index.html");
        } catch (LoginFailException e) {
            session.setAttribute("logined", "false");
            response.redirect("/user/login_failed.html");
        }
    }

    private static class LazyHolder {
        private static final LoginController INSTANCE = new LoginController();
    }
}
