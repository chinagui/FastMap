/**
 * 
 */
package com.navinfo.dataservice.datahub.model;

import java.util.Set;

/**
 * @author xiaoxiaowen4127
 *
 */
public class DbServer {
	private int sid;
	private String ip;
	private String port;
	private String serviceName;
	private Set<String> useType;
	private String descp;
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
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
}
