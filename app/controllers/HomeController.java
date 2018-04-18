package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import com.r.bigconf.core.service.ConferenceService;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

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
    private final PlaySessionStore playSessionStore;

    @Inject
    public HomeController(ConferenceService conferenceService, UserService userService, PlaySessionStore playSessionStore) {
        this.conferenceService = conferenceService;
        this.userService = userService;
        this.playSessionStore = playSessionStore;
    }

    @Secure
    public Result index() {
        if (!session().containsKey(USER_ID_KEY)) {
            registerAuthenticatedUser();
        }
        return ok(views.html.index.render(DEFAULT_RECORD_INTERVAL));
    }

    private void registerAuthenticatedUser() {
        PlayWebContext webContext = new PlayWebContext(ctx(), playSessionStore);
        ProfileManager<CommonProfile> profileManager = new ProfileManager<>(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);
        if (profile.isPresent()) {
            CommonProfile commonProfile = profile.get();
            String userId = commonProfile.getId();
            userService.registerUser(new User(userId, commonProfile.getUsername()));
            session().put(USER_ID_KEY, userId);
        }
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
    public Result getConferenceList() {
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
