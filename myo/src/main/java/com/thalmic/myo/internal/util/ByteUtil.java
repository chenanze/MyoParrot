package com.thalmic.myo.internal.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ByteUtil
{
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[(j * 2)] = hexArray[(v >>> 4)];
            hexChars[(j * 2 + 1)] = hexArray[(v & 0xF)];
        }
        return new String(hexChars);
    }

    public static UUID getUuidFromBytes(byte[] array, int index)
    {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        long msb = buffer.getLong(index);
        long lsb = buffer.getLong(index + 8);
        return new UUID(msb, lsb);
    }

    public static String getString(byte[] array, int offset)
    {
        if (offset > array.length) {
            return null;
        }
        byte[] strBytes = new byte[array.length - offset];
        for (int i = 0; i != array.length - offset; i++) {
            strBytes[i] = array[(offset + i)];
        }
        return new String(strBytes);
    }
}
