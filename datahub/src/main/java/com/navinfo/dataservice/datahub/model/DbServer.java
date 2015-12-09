package com.navinfo.dataservice.datahub.model;

import java.util.Set;

import com.navinfo.dataservice.commons.database.oracle.DbServerType;
/** 
 * @ClassName: DbServer 
 * @author Xiao Xiaowen 
 * @Description: TODO
 *  
 */
public class DbServer {
	private int sid;
	protected String type=DbServerType.TYPE_NONE;
	private String ip;
	private String port;
	private String serviceName;
	private Set<String> useType;
	private String descp;
	
	private String identity;//ip:port
	
	public DbServer(String type,String ip,String port){
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
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public Set<String> getUseType() {
		return useType;
	}
	public void setUseType(Set<String> useType) {
		this.useType = useType;
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
}
