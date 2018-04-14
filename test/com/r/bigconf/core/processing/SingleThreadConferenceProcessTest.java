package com.r.bigconf.core.processing;

import com.r.bigconf.core.filter.DummyWavFilter;
import com.r.bigconf.core.model.Conference;
import com.r.bigconf.local.SingleThreadConferenceProcess;
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
        SingleThreadConferenceProcess confProcess = new SingleThreadConferenceProcess(new Conference(1000));
        addIncoming(confProcess, "D:\\workspace\\bigconf\\bigconf\\test\\resources\\1.wav", 1);
        addIncoming(confProcess, "D:\\workspace\\bigconf\\bigconf\\test\\resources\\2.wav", 2);

        long now = System.currentTimeMillis();
        confProcess.processInterval();
        System.out.println(System.currentTimeMillis() - now);
        ByteBuffer forUser = confProcess.getForUser(7);
        FileChannel open = FileChannel.open(Paths.get("D:\\workspace\\bigconf\\bigconf\\target\\result.wav"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        open.write(forUser);
    }

    private void addIncoming(SingleThreadConferenceProcess confProcess, String path, int userId) throws IOException {
        FileChannel fileChannel = FileChannel.open(Paths.get(path));
        MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        confProcess.addIncoming(userId,byteBuffer, FILTER);
    }

}