package com.r.bigconf.processing;

import com.r.bigconf.model.Conference;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.*;

public class ConfProcessTest {
    @Test
    public void run() throws Exception {

        FileChannel fileChannel = FileChannel.open(Paths.get("D:\\workspace\\bigconf\\bigconf\\test\\resources\\1.wav"));
        MappedByteBuffer byteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

        FileChannel fileChannel2 = FileChannel.open(Paths.get("D:\\workspace\\bigconf\\bigconf\\test\\resources\\2.wav"));
        MappedByteBuffer byteBuffer2 = fileChannel2.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel2.size());

        ConfProcess confProcess = new ConfProcess(new Conference(1000));
        confProcess.addIncoming(1,byteBuffer);
        confProcess.addIncoming(2,byteBuffer2);
        confProcess.processInterval();
        ByteBuffer forUser = confProcess.getForUser(3);
        FileChannel open = FileChannel.open(Paths.get("D:\\workspace\\bigconf\\bigconf\\target\\result.wav"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        open.write(forUser);
    }

}