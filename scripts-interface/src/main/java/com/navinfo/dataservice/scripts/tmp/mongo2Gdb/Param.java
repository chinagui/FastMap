package com.navinfo.dataservice.scripts.tmp.mongo2Gdb;

import java.util.Properties;

public class Param {
	String mongodbHost = null;
	int mongoPort = 0;
	String mongodbDbName;
	String mongodbCollectionName;
	Properties props = null;
	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	int expType = 0;

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

	public int getExpType() {
		return expType;
	}

	public void setExpType(int expType) {
		this.expType = expType;
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

	public String getMongodbCollectionName() {
		return mongodbCollectionName;
	}

	public void setMongodbCollectionName(String mongodbCollectionName) {
		this.mongodbCollectionName = mongodbCollectionName;
	}

}