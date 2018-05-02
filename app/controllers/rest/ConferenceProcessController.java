package controllers.rest;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.model.ConferenceUsers;
import com.r.bigconf.core.service.ConferenceService;
import com.r.bigconf.core.service.UserService;
import controllers.UserIdSupportController;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlaySessionStore;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConferenceProcessController extends UserIdSupportController {

    private final UserService userService;
    private final ConferenceService conferenceService;

    @Inject
    public ConferenceProcessController(UserService userService, ConferenceService conferenceService, PlaySessionStore playSessionStore) {
        super(playSessionStore, userService);
        this.userService = userService;
        this.conferenceService = conferenceService;
    }

    @Secure
    public CompletionStage<Result> upload() {
        ByteString byteString = request().body().asRaw().asBytes();
        if (byteString != null) {
            return withConferenceId((confId) -> conferenceService.addIncoming(confId, getUserId(), byteString.asByteBuffer())
                    .thenApplyAsync((nothing) -> ok("Sound uploaded")));
        } else {
            flash("error", "Missing file");
            return CompletableFuture.completedFuture(badRequest());
        }
    }

    @Secure
    public CompletionStage<Result> conference() {
        Http.Response response = response();
        return withConferenceId((conferenceId) -> conferenceService.getForUser(conferenceId, getUserId())
                .thenCombineAsync(conferenceService.getConferenceUsers(conferenceId), (bytes, confUsers) -> {
                    if(confUsers != null) {
                        response.setHeader("confUsers", mapConfUsers(confUsers));
                        if (bytes != null) {
                            return ok(new ByteBufferBackedInputStream(bytes));
                        } else {
                            return status(204);
                        }
                    } else {
                        return notFound();
                    }
                }));
    }

    private String mapConfUsers(ConferenceUsers confUsers) {
        List<ConfUsersDTO> dtoList = confUsers.getUsersData().stream()
                .map(data -> new ConfUsersDTO(data.getUserId(),
                        booleanToInt(data.isMuted()),
                        booleanToInt(data.isSpeaking())))
                .collect(Collectors.toList());
        return Json.toJson(dtoList).toString();
    }

    private int booleanToInt(boolean value) {
        return value ?1:0;
    }

    @Secure
    public CompletionStage<Result> stopConference() {
        return withConferenceId(confId -> userService.getUser(getUserId())
                .thenComposeAsync(user -> conferenceService.stopConference(user, confId))
                .thenApplyAsync((nothing) -> ok("Stopped")));
    }

    @Secure
    public CompletionStage<Result> joinConference() {
        return withConferenceId(confId -> userService.getUser(getUserId())
                .thenComposeAsync(user -> conferenceService.joinToConference(confId, user))
                .thenApplyAsync((conference) -> ok(Json.toJson(conference))));
    }

    @Secure
    public CompletionStage<Result> leaveConference(){
        return withConferenceId(confId -> userService.getUser(getUserId())
                .thenComposeAsync(user -> conferenceService.leaveConference(confId, user))
                .thenApplyAsync((conference) -> ok(Json.toJson(conference))));
    }

    private CompletionStage<Result> withConferenceId(Function<UUID, CompletionStage<Result>> command) {
        UUID confId = null;
        try {
            String queryString = request().getQueryString("confId");
            if (queryString != null) {
                confId = UUID.fromString(queryString);
            } else {
                return CompletableFuture.completedFuture(badRequest("No conference id"));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(badRequest(Optional.ofNullable(e.getMessage()).orElse("null")));
        }
        return command.apply(confId);
    }

    @Data
    @AllArgsConstructor
    public static class ConfUsersDTO {
        //userId
        private String u;
        //muted
        private int m;
        //speaking
        private int s;
    }
}
