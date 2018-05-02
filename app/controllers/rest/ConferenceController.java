package controllers.rest;

import com.r.bigconf.core.service.ConferenceService;
import com.r.bigconf.core.service.UserService;
import controllers.UserIdSupportController;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlaySessionStore;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ConferenceController extends UserIdSupportController {

    private final ConferenceService conferenceService;
    private final UserService userService;

    @Inject
    public ConferenceController(ConferenceService conferenceService, UserService userService, PlaySessionStore playSessionStore) {
        super(playSessionStore, userService);
        this.conferenceService = conferenceService;
        this.userService = userService;
    }

    @Secure
    public CompletionStage<Result> list() {
        return userService.getUser(getUserId())
                .thenComposeAsync(conferenceService::listAvailableConferences)
                .thenApplyAsync(list-> ok(Json.toJson(list)));
    }

    @Secure
    public CompletionStage<Result> startNewConference() {
        return userService.getUser(getUserId())
                .thenComposeAsync(conferenceService::startConference)
                .thenApplyAsync(conf-> ok(Json.toJson(conf)));
    }
}
