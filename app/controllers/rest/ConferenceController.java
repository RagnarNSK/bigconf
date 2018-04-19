package controllers.rest;

import com.r.bigconf.core.service.ConferenceService;
import controllers.UserIdSupportController;
import org.pac4j.play.java.Secure;
import play.mvc.Result;

import javax.inject.Inject;

public class ConferenceController extends UserIdSupportController {

    private final ConferenceService conferenceService;

    @Inject
    public ConferenceController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
    }

    @Secure
    public Result getConferenceList() {
        //TODO
        return null;
    }
}
