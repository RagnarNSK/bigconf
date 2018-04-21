package controllers.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import static com.r.bigconf.core.model.Conference.DEFAULT_RECORD_INTERVAL;

public class SettingsController extends Controller {

    @Data
    @AllArgsConstructor
    public static final class ApplicationSettings {
        private Integer conferenceListUpdateIntervalMs;
        private Integer defaultRecordIntervalMs;
    }

    private static final ApplicationSettings instance = new ApplicationSettings(
            5000,
            DEFAULT_RECORD_INTERVAL
    );
    private static final JsonNode JSON = Json.toJson(instance);

    public Result settings(){
        return ok(JSON);
    }
}
