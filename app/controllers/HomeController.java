package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.ignite.IgniteConferenceManager;
import com.r.bigconf.core.manager.ConferenceManager;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.local.SingleThreadConferenceProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import play.inject.ApplicationLifecycle;
import play.mvc.*;

import javax.inject.Inject;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Slf4j
public class HomeController extends Controller {

    public static final String USER_ID_KEY = "userId";
    public static final DummyWavFilter FILTER = new DummyWavFilter();

    private Conference testConference = new Conference(1000);
    private SingleThreadConferenceProcess testConfProcess = new SingleThreadConferenceProcess(testConference);

    private final ConferenceManager conferenceManager;

    @Inject
    public HomeController(ApplicationLifecycle lifecycle) {
        Ignite ignite = Ignition.start();
        log.info("Ignite {} started", ignite.name());
        conferenceManager = new IgniteConferenceManager(ignite);

        testConfProcess.isActive = true;
        final Thread confProcessThread = new Thread(testConfProcess);
        confProcessThread.start();
        lifecycle.addStopHook(() -> {
            log.info("Clearing app data");
            ignite.close();
            testConfProcess.isActive = false;
            confProcessThread.interrupt();
            log.info("shutdown executing");
            return CompletableFuture.completedFuture(null);
        });
    }

    public Result index() {
        if (!session().containsKey(USER_ID_KEY)) {
            try {
                session().put(USER_ID_KEY, Integer.toString(SecureRandom.getInstance("SHA1PRNG").nextInt()));
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
        return ok(views.html.index.render(testConference.getRecordInterval()));
    }

    public Result upload() {
        ByteString byteString = request().body().asRaw().asBytes();
        if (byteString != null) {
            int userId = getUserId();
            testConfProcess.addIncoming(userId, byteString.asByteBuffer(), FILTER);
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    public Result conference() throws IOException {
        ByteBuffer bytes = testConfProcess.getForUser(getUserId());
        if (bytes != null) {
            return ok(new ByteBufferBackedInputStream(bytes));
        } else {
            return status(204);
        }
    }

    public Result getConferenceList()  {
        //TODO
        return null;
    }

    private int getUserId() {
        String idString = session().get(USER_ID_KEY);
        if (idString != null) {
            return Integer.parseInt(idString);
        } else {
            throw new IllegalStateException("unauthorized");
        }
    }
}
