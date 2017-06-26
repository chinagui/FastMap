package com.navinfo.dataservice.scripts.tmp.diff;

public class Param{
	String mongodbHost=null;
	int mongoPort = 0;
	String mongodbDbName;
	String mongodbCollectionName;
	String oralceHost;
	String oraclePort;
	String oracleUser;
	String oracleSid;
	String oraclePwd;
	String diffFidTempTableName;
	String outputFile;
	int diffType;
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
	public String getMongodbCollectionName() {
		return mongodbCollectionName;
	}
	public void setMongodbCollectionName(String mongodbCollectionName) {
		this.mongodbCollectionName = mongodbCollectionName;
	}
	public String getOralceHost() {
		return oralceHost;
	}
	public void setOralceHost(String oralceHost) {
		this.oralceHost = oralceHost;
	}
	public String getOraclePort() {
		return oraclePort;
	}
	public void setOraclePort(String oraclePort) {
		this.oraclePort = oraclePort;
	}
	public String getOracleUser() {
		return oracleUser;
	}
	public void setOracleUser(String oracleUser) {
		this.oracleUser = oracleUser;
	}
	public String getOracleSid() {
		return oracleSid;
	}
	public void setOracleSid(String oracleSid) {
		this.oracleSid = oracleSid;
	}
	public String getOraclePwd() {
		return oraclePwd;
	}
	public void setOraclePwd(String oraclePwd) {
		this.oraclePwd = oraclePwd;
	}
	public String getDiffFidTempTableName() {
		return diffFidTempTableName;
	}
	public void setDiffFidTempTableName(String diffFidTempTableName) {
		this.diffFidTempTableName = diffFidTempTableName;
	}
	public String getOutputFile() {
		return outputFile;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	public int getDiffType(){
		return diffType;
	}
	public void setDiffType(int diffType){
		this.diffType = diffType;
	}
	
}