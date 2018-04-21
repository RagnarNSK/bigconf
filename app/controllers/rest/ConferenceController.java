package controllers.rest;

import com.r.bigconf.core.service.ConferenceService;
import com.r.bigconf.core.service.UserService;
import controllers.UserIdSupportController;
import org.pac4j.play.java.Secure;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ConferenceController extends UserIdSupportController {

    private final ConferenceService conferenceService;
    private final UserService userService;

    @Inject
    public ConferenceController(ConferenceService conferenceService, UserService userService) {
        this.conferenceService = conferenceService;
        this.userService = userService;
    }

    @Secure
    public CompletionStage<Result> list() {
        return userService.getUser(getUserId())
                .thenApplyAsync(conferenceService::listAvailableConferences)
                .thenApplyAsync(list-> ok(Json.toJson(list)));
    }
}
