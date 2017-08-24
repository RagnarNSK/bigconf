package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);

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
            System.out.println(getInfo(bytes));
            return ok("File uploaded");
        } else {
            flash("error", "Missing file");
            return badRequest();
        }
    }

    public Result conference() throws IOException {
        if (bytes != null) {
            ByteBuffer ret = this.bytes;
            this.bytes = null;
            return ok(new ByteBufferBackedInputStream(ret));
        } else {
            return status(204);
        }
    }

    private String getInfo(ByteBuffer byteBuffer) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer duplicate = byteBuffer.duplicate();
        byte[] array = new byte[duplicate.remaining()];
        duplicate.get(array);
        sb.append("ChunkId:");
        appendString(array, sb, 0, 3);
        //.append(getStr(array[0])).append(getStr(array[1])).append(getStr(array[2])).append(getStr(array[3]));
        sb.append(" chunkSize:").append(toInt(array, 4));
        sb.append(" format:");
        appendString(array, sb, 8, 11);
        sb.append(" subchunk1Id:");
        appendString(array, sb, 12, 15);
        sb.append(" subchunk1Size:").append(toInt(array, 16));
        sb.append(" audioFormat:").append(array[20]).append(array[21]);
        sb.append(" numChannels:").append(array[22]).append(array[23]);
        sb.append(" sampleRate:").append(toInt(array,24));
        sb.append(" byteRate:").append(toInt(array,28));
        sb.append(" blockAlign:").append(array[32]).append(array[33]);
        sb.append(" bitsPerSample:").append(array[34]).append(array[35]);
        sb.append(" subchunk2Id:");
        appendString(array, sb, 36, 39);
        sb.append(" subchunk2Size:").append(toInt(array,40));

        //.append(Character.LINE_SEPARATOR)
        return sb.toString();
    }

    private void appendString(byte[] array, StringBuilder sb, int start, int end) {
        for (int i = start; i <= end; i++) {
            sb.append(getStr(array[i]));
        }
    }

    private String getStr(byte b) {
        return Character.toString((char) b);
    }

    private int toInt(byte[] array, int start) {
        byte[] arr = new byte[4];
        arr[0] = array[start];
        arr[1] = array[start+1];
        arr[2] = array[start+2];
        arr[3] = array[start+3];
        ByteBuffer bb = ByteBuffer.wrap(arr);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }
}
