package com.navinfo.dataservice.engine.limit.commons.database.oracle;

public class SessionProperties {
	private long sid;
	private long serial;// serial#
	private String url;
	private String username;
	private String password;

	@Override
	public boolean equals(Object obj) {
		return obj.toString().equals(this.toString());
	}

	public SessionProperties(long sid, long serial, String url, String username, String password) {
		super();
		this.sid = sid;
		this.serial = serial;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public SessionProperties() {

	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getSid() {
		return sid;
	}

	public void setSid(long sid) {
		this.sid = sid;
	}

	public long getSerial() {
		return serial;
	}

	public void setSerial(long serial) {
		this.serial = serial;
	}

	@Override
	public String toString() {
		return "SessionProperties [sid=" + sid + ", serial=" + serial + ", url=" + url + ", username=" + username + ", password=" + password + "]";
	}

}
