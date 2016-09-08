package com.navinfo.dataservice.commons.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;

public class SerializeUtils
{

    public static byte[] serialize(Serializable s) throws IOException
    {

        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream os = null;
        try
        {
            bo = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bo);
            os.writeObject(s);
            bytes = bo.toByteArray();
        } finally
        {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(bo);
        }
        return bytes;
    }

    public static Serializable deserialize(byte[] ba) throws IOException, ClassNotFoundException
    {
        Serializable serializable = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream is = null;
        try
        {
            bi = new ByteArrayInputStream(ba);
            is = new ObjectInputStream(bi);
            serializable = (Serializable) is.readObject();
        } finally
        {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(bi);
        }
        return serializable;
    }

    public static Serializable deepClone(Serializable s) throws ClassNotFoundException, IOException 
    {
        return deserialize(serialize(s));
    }
    
    public static Object convert(Object object, Class<?> type) {
        if (object instanceof Number) {
            Number number = (Number) object;
            if (type.equals(byte.class) || type.equals(Byte.class)) {
                return number.byteValue();
            }
            if (type.equals(short.class) || type.equals(Short.class)) {
                return number.shortValue();
            }
            if (type.equals(int.class) || type.equals(Integer.class)) {
                return number.intValue();
            }
            if (type.equals(long.class) || type.equals(Long.class)) {
                return number.longValue();
            }
            if (type.equals(float.class) || type.equals(Float.class)) {
                return number.floatValue();
            }
            if (type.equals(double.class) || type.equals(Double.class)) {
                return number.doubleValue();
            }
        }
        return object;
    }
    
}
