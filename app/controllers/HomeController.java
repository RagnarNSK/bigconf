package controllers;

import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;

import static com.r.bigconf.core.model.Conference.DEFAULT_RECORD_INTERVAL;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Slf4j
public class HomeController extends UserIdSupportController {

    @Inject
    public HomeController(UserService userService, PlaySessionStore playSessionStore) {
        super(playSessionStore, userService);
    }

    @Secure
    public Result index() {
        if (!session().containsKey(USER_ID_KEY)) {
            registerAuthenticatedUser();
        }
        return ok(views.html.index.render());
    }


}
