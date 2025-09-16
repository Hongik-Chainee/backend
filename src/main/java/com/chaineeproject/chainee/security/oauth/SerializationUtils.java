package com.chaineeproject.chainee.security.oauth;

import java.io.*;

public final class SerializationUtils {
    public static byte[] serialize(Object obj) {
        try (var baos = new ByteArrayOutputStream();
             var oos  = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    public static Object deserialize(byte[] bytes) {
        try (var bais = new ByteArrayInputStream(bytes);
             var ois  = new ObjectInputStream(bais)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) { throw new IllegalStateException(e); }
    }
}