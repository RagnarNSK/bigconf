package controllers.rest;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.service.ConferenceService;
import controllers.UserIdSupportController;
import org.pac4j.play.java.Secure;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ConferenceProcessController extends UserIdSupportController {

    private final ConferenceService conferenceService;

    @Inject
    public ConferenceProcessController(ConferenceService conferenceService) {
        this.conferenceService = conferenceService;
    }

    @Secure
    public CompletionStage<Result> upload() {
        ByteString byteString = request().body().asRaw().asBytes();
        if (byteString != null) {
            return withConferenceId((confId)-> conferenceService.addIncoming(confId, getUserId(), byteString.asByteBuffer())
                    .thenApplyAsync((nothing)-> ok("Sound uploaded")));
        } else {
            flash("error", "Missing file");
            return CompletableFuture.completedFuture(badRequest());
        }
    }

    @Secure
    public CompletionStage<Result> conference() {
        return withConferenceId((conferenceId)-> conferenceService.getForUser(conferenceId, getUserId()).thenApplyAsync(bytes -> {
            if (bytes != null) {
                return ok(new ByteBufferBackedInputStream(bytes));
            } else {
                return status(204);
            }
        }));
    }

    private CompletionStage<Result> withConferenceId(Function<UUID,CompletionStage<Result>> command) {
        UUID confId = null;
        try {
            String queryString = request().getQueryString("confId");
            if(queryString != null) {
                confId = UUID.fromString(queryString);
            } else {
                return CompletableFuture.completedFuture(badRequest("No conference id"));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(badRequest(Optional.ofNullable(e.getMessage()).orElse("null")));
        }
        return command.apply(confId);
    }
}
