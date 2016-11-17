package com.navinfo.dataservice.engine.editplus.convert;

public class ObjConvertException extends Exception{
    public ObjConvertException(String message) {
        super(message);
    }

    public ObjConvertException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjConvertException(Throwable cause) {
        super(cause);
    }
}
