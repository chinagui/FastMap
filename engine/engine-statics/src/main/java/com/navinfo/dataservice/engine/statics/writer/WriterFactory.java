package com.navinfo.dataservice.engine.statics.writer;

public class WriterFactory {
	public static DefaultWriter createWriter(String JobName){
		return new DefaultWriter();		
	}
}
