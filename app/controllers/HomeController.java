package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.r.bigconf.model.Conference;
import com.r.bigconf.processing.ConfProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class HomeController extends Controller {

    public static final String USER_ID_KEY = "userId";
    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

    //private ByteBuffer bytes;

    private Conference testConference = new Conference(1000);
    private ConfProcess testConfProcess = new ConfProcess(testConference);

    @Inject
    public HomeController(ApplicationLifecycle lifecycle) {
        testConfProcess.isActive = true;
        final Thread confProcessThread = new Thread(testConfProcess);
        confProcessThread.start();
        lifecycle.addStopHook(() -> {
            testConfProcess.isActive = false;
            confProcessThread.interrupt();
            return CompletableFuture.completedFuture(null);
        });
    }
    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        if(!session().containsKey(USER_ID_KEY)){
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
            testConfProcess.addIncoming(userId, byteString.asByteBuffer());
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

    private int getUserId() {
        String idString = session().get(USER_ID_KEY);
        if(idString != null){
            return Integer.parseInt(idString);
        } else {
            throw new IllegalStateException("unauthorized");
        }
    }
}
