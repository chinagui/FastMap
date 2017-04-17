package com.navinfo.dataservice.scripts.tmp.diff;

public class Param{
	String mongodbHost=null;
	int mongoPort = 0;
	String mongodbDbName;
	//
	public Param() {
		super();
	}
	public String getMongodbHost() {
		return mongodbHost;
	}
	public void setMongodbHost(String mongodbHost) {
		this.mongodbHost = mongodbHost;
	}
	public int getMongoPort() {
		return mongoPort;
	}
	public void setMongoPort(int mongoPort) {
		this.mongoPort = mongoPort;
	}
	public String getMongodbDbName() {
		return mongodbDbName;
	}
	public void setMongodbDbName(String mongodbDbName) {
		this.mongodbDbName = mongodbDbName;
	}
	
}