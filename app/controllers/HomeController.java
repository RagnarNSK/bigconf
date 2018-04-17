package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import com.r.bigconf.core.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.config.Config;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.http.client.indirect.FormClient;
import org.pac4j.play.java.Secure;
import play.mvc.*;

import javax.inject.Inject;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.r.bigconf.core.model.Conference.DEFAULT_RECORD_INTERVAL;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Slf4j
public class HomeController extends Controller {

    private static final String USER_ID_KEY = "userId";

    private final ConferenceService conferenceService;
    private final UserService userService;
    private final Config config;

    @Inject
    public HomeController(ConferenceService conferenceService, UserService userService, Config config) {
        this.conferenceService = conferenceService;
        this.userService = userService;
        this.config = config;
    }

    public Result loginForm() throws TechnicalException {
        final FormClient formClient = (FormClient) config.getClients().findClient("FormClient");
        return ok(views.html.security.loginForm.render(formClient.getCallbackUrl()));
    }

    @Secure
    public Result index() {
        if (!session().containsKey(USER_ID_KEY)) {
            try {
                String mockUserId = "testUser"+Integer.toString(SecureRandom.getInstance("SHA1PRNG").nextInt());
                userService.registerUser(new User(mockUserId, mockUserId));
                session().put(USER_ID_KEY, mockUserId);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
        return ok(views.html.index.render(DEFAULT_RECORD_INTERVAL));
    }

    @Secure
    public Result upload() {
        ByteString byteString = request().body().asRaw().asBytes();
        if (byteString != null) {
            conferenceService.addIncoming(getUserId(), byteString.asByteBuffer());
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    @Secure
    public Result conference() throws IOException {
        ByteBuffer bytes = conferenceService.getForUser(getUserId());
        if (bytes != null) {
            return ok(new ByteBufferBackedInputStream(bytes));
        } else {
            return status(204);
        }
    }

    @Secure
    public Result getConferenceList()  {
        //TODO
        return null;
    }

    private String getUserId() {
        String idString = session().get(USER_ID_KEY);
        if (idString != null) {
            return idString;
        } else {
            throw new IllegalStateException("unauthorized");
        }
    }
}
