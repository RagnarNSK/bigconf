package controllers.rest;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.service.ConferenceService;
import controllers.UserIdSupportController;
import org.pac4j.play.java.Secure;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class ConferenceProcessController extends UserIdSupportController {

    private final ConferenceService conferenceService;

    @Inject
    public ConferenceProcessController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
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
    public CompletionStage<Result> conference() throws IOException {
        return conferenceService.getForUser(getUserId()).thenApplyAsync(bytes -> {
            if (bytes != null) {
                return ok(new ByteBufferBackedInputStream(bytes));
            } else {
                return status(204);
            }
        });
    }
}
