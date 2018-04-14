package com.r.bigconf.core.filter;

import java.nio.ByteBuffer;

import static com.r.bigconf.core.sound.wav.WavUtils.getHiPart;
import static com.r.bigconf.core.sound.wav.WavUtils.getLoPart;
import static com.r.bigconf.core.sound.wav.WavUtils.getaShort;

public class DummyWavFilter implements Filter {

    private static final int DUMMY_FILTER_THRESHOLD = 100;

    @Override
    public ByteBuffer filter(ByteBuffer byteBuffer) {

        int length = byteBuffer.limit();
        ByteBuffer ret = ByteBuffer.allocate(length);
        for(int i = 0; i< length; i=i+2){
            if(i > 44){
                byte lo = byteBuffer.get(i);
                byte hi = byteBuffer.get(i+1);
                short value = getaShort(hi, lo);
                if(value < DUMMY_FILTER_THRESHOLD && value > (DUMMY_FILTER_THRESHOLD * -1)){
                    value = 0;
                }
                ret.put(i, getLoPart(value));
                ret.put(i+1, getHiPart(value));
            } else {
                ret.put(i,byteBuffer.get(i));
                ret.put(i+1,byteBuffer.get(i+1));
            }
        }
        return ret;
    }
}
