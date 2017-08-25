package com.r.bigconf.sound.wav;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavUtils {


    public static int getDataLength(ByteBuffer byteBuffer){
        byte[] length = new byte[4];
        byteBuffer.position(40);
        byteBuffer.get(length,0, 4);
        ByteBuffer bb = ByteBuffer.wrap(length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }


    public static String getInfo(ByteBuffer byteBuffer) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer duplicate = byteBuffer.duplicate();
        byte[] array = new byte[duplicate.remaining()];
        duplicate.get(array);
        sb.append("ChunkId:");
        appendString(array, sb, 0, 3);
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
        return sb.toString();
    }

    private static void appendString(byte[] array, StringBuilder sb, int start, int end) {
        for (int i = start; i <= end; i++) {
            sb.append(getStr(array[i]));
        }
    }

    private static String getStr(byte b) {
        return Character.toString((char) b);
    }

    private static int toInt(byte[] array, int start) {
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
