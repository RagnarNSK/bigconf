package controllers.rest;

import com.r.bigconf.core.model.User;
import com.r.bigconf.core.service.UserService;
import controllers.UserIdSupportController;
import play.libs.Json;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class UserController extends UserIdSupportController {

    private static final Function<User, Result> USER_TO_JSON = user -> ok(Json.toJson(user));

    private final UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }

    public CompletionStage<Result> list(){
        return userService.getUsers().thenApplyAsync(list->ok(Json.toJson(list)));
    }

    public CompletionStage<Result> one(String id) {
        return userService.getUser(id).thenApplyAsync(USER_TO_JSON);
    }

    public CompletionStage<Result> me() {
        return userService.getUser(getUserId()).thenApplyAsync(USER_TO_JSON);
    }

    public CompletionStage<Result> register(){
        User user = Json.fromJson(request().body().asJson(), User.class);
        return userService.registerUser(user).thenApplyAsync((T)->ok("Registered"));
    }

    public CompletionStage<Result> update(){
        //TODO check if needed
        return null;
    }
}
