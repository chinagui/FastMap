package com.navinfo.dataservice.engine.limit.datahub.model;

import java.io.Serializable;
import java.util.Set;

/** 
 * @ClassName: DbServer 
 * @author Xiao Xiaowen 
 * @Description: TODO
 *  
 */
public class DbServer implements Serializable{
	private int sid;
	protected String type="NONE";
	private String ip;
	private int port;
	private String serviceName;
	private Set<String> bizType;
	private String descp;
	
	private String identity;//ip:port
	public DbServer(){
	} 
	public DbServer(String type, String ip, int port){
		this.type=type;
		this.ip=ip;
		this.port=port;
		this.identity=ip+":"+port;
	}
	
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Set<String> getBizType() {
		return bizType;
	}
	public void setBizType(Set<String> bizType) {
		this.bizType = bizType;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
	public String getIdentity(){
		return this.identity;
	}
	public int hashCode(){
		return getIdentity().hashCode();
	}
	public boolean equals(Object anObject){
		if(anObject==null)return false;
		if(anObject instanceof DbServer
				&&getIdentity().equals(((DbServer) anObject).getIdentity())){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		return "DbServer [sid=" + sid + ", type=" + type + ", ip=" + ip
				+ ", port=" + port + ",serviceName=" +serviceName + ", bizType=" + bizType + ", descp="
				+ descp + ", identity=" + identity + "]";
	}
	
}
