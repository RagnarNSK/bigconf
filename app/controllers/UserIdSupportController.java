package controllers;

import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import lombok.SneakyThrows;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.play.PlayWebContext;
import org.pac4j.play.store.PlaySessionStore;
import play.mvc.Controller;

import java.util.Optional;

public class UserIdSupportController extends Controller {

    protected static final String USER_ID_KEY = "userId";
    protected final UserService userService;
    protected final PlaySessionStore playSessionStore;

    public UserIdSupportController(PlaySessionStore playSessionStore, UserService userService) {
        this.playSessionStore = playSessionStore;
        this.userService = userService;
    }

    protected String getUserId() {
        String idString = session().get(USER_ID_KEY);
        if (idString != null) {
            return idString;
        } else {
            return registerAuthenticatedUser();
        }
    }

    @SneakyThrows
    protected String registerAuthenticatedUser() {
        PlayWebContext webContext = new PlayWebContext(ctx(), playSessionStore);
        ProfileManager<CommonProfile> profileManager = new ProfileManager<>(webContext);
        Optional<CommonProfile> profile = profileManager.get(true);
        if (profile.isPresent()) {
            CommonProfile commonProfile = profile.get();
            String userId = commonProfile.getId();
            userService.registerUser(new User(userId, commonProfile.getUsername()));
            session().put(USER_ID_KEY, userId);
            return userId;
        } else {
            throw HttpAction.unauthorized("unathorized", webContext,"main");
        }
    }
}
