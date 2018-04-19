package controllers;

import play.mvc.Controller;

public class UserIdSupportController extends Controller {

    protected static final String USER_ID_KEY = "userId";

    protected String getUserId() {
        String idString = session().get(USER_ID_KEY);
        if (idString != null) {
            return idString;
        } else {
            throw new IllegalStateException("unauthorized");
        }
    }
}
