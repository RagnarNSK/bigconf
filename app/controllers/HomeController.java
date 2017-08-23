package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import play.mvc.*;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private ByteBuffer bytes;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    public Result upload() {
        ByteString byteString = request().body().asRaw().asBytes();
        if (byteString != null) {
            bytes = byteString.asByteBuffer();
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    public Result conference() throws IOException {
        if(bytes != null){
            ByteBuffer ret = this.bytes;
            this.bytes = null;
            return ok(new ByteBufferBackedInputStream(ret));
        } else {
            return status(204);
        }
    }
}
