package com.r.bigconf.core.processing;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.processing.model.ConferenceProcessDataObject;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SingleThreadConferenceProcessTest {

    public static final DummyWavFilter FILTER = new DummyWavFilter();

    @Test
    public void run() throws Exception {
        BaseConferenceProcess confProcess = new BaseConferenceProcess();
        ConferenceProcessDataObject processData = new ConferenceProcessDataObject();
        addIncoming(processData, "D:\\workspace\\bigconf\\bigconf\\test\\resources\\1.wav", 1);
        addIncoming(processData, "D:\\workspace\\bigconf\\bigconf\\test\\resources\\2.wav", 2);

        long now = System.currentTimeMillis();
        confProcess.processInterval(processData);
        System.out.println(System.currentTimeMillis() - now);
        ByteBuffer forUser = processData.getForUser(7);
        FileChannel open = FileChannel.open(Paths.get("D:\\workspace\\bigconf\\bigconf\\target\\result.wav"),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        open.write(forUser);
    }

    private void addIncoming(ConferenceProcessDataObject processData, String path, int userId) throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(path));
        MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        processData.addIncoming(userId,byteBuffer, FILTER);
    }

}