package controllers;

import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.http.client.indirect.FormClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

public class SecurityController extends Controller {

    private final Config config;

    @Inject
    public SecurityController(Config config) {
        this.config = config;
    }

    public Result loginForm() throws TechnicalException {
        final FormClient formClient = (FormClient) config.getClients().findClient("FormClient");
        return ok(views.html.security.loginForm.render(formClient.getCallbackUrl()));
    }
}
