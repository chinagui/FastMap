package com.navinfo.dataservice.engine.man.userDevice;

/** 
* @ClassName:  UserDevice 
* @author code generator
* @date 2016-06-14 07:33:00 
* @Description: TODO
*/
public class UserDevice  {
	private Integer deviceId ;
	private Integer userId ;
	private String deviceToken ;
	private String devicePlatform ;
	private String deviceVersion ;
	private String deviceModel ;
	private String deviceSystemVersion ;
	private String deviceDescendantVersion ;
	
	public UserDevice (){
	}
	
	public UserDevice (Integer deviceId ,Integer userId,String deviceToken,String devicePlatform,String deviceVersion,String deviceModel,String deviceSystemVersion,String deviceDescendantVersion){
		this.deviceId=deviceId ;
		this.userId=userId ;
		this.deviceToken=deviceToken ;
		this.devicePlatform=devicePlatform ;
		this.deviceVersion=deviceVersion ;
		this.deviceModel=deviceModel ;
		this.deviceSystemVersion=deviceSystemVersion ;
		this.deviceDescendantVersion=deviceDescendantVersion ;
	}
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	public String getDevicePlatform() {
		return devicePlatform;
	}
	public void setDevicePlatform(String devicePlatform) {
		this.devicePlatform = devicePlatform;
	}
	public String getDeviceVersion() {
		return deviceVersion;
	}
	public void setDeviceVersion(String deviceVersion) {
		this.deviceVersion = deviceVersion;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getDeviceSystemVersion() {
		return deviceSystemVersion;
	}
	public void setDeviceSystemVersion(String deviceSystemVersion) {
		this.deviceSystemVersion = deviceSystemVersion;
	}
	public String getDeviceDescendantVersion() {
		return deviceDescendantVersion;
	}
	public void setDeviceDescendantVersion(String deviceDescendantVersion) {
		this.deviceDescendantVersion = deviceDescendantVersion;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserDevice [deviceId=" + deviceId +",userId="+userId+",deviceToken="+deviceToken+",devicePlatform="+devicePlatform+",deviceVersion="+deviceVersion+",deviceModel="+deviceModel+",deviceSystemVersion="+deviceSystemVersion+",deviceDescendantVersion="+deviceDescendantVersion+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((deviceToken == null) ? 0 : deviceToken.hashCode());
		result = prime * result + ((devicePlatform == null) ? 0 : devicePlatform.hashCode());
		result = prime * result + ((deviceVersion == null) ? 0 : deviceVersion.hashCode());
		result = prime * result + ((deviceModel == null) ? 0 : deviceModel.hashCode());
		result = prime * result + ((deviceSystemVersion == null) ? 0 : deviceSystemVersion.hashCode());
		result = prime * result + ((deviceDescendantVersion == null) ? 0 : deviceDescendantVersion.hashCode());
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDevice other = (UserDevice) obj;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (deviceToken == null) {
			if (other.deviceToken != null)
				return false;
		} else if (!deviceToken.equals(other.deviceToken))
			return false;
		if (devicePlatform == null) {
			if (other.devicePlatform != null)
				return false;
		} else if (!devicePlatform.equals(other.devicePlatform))
			return false;
		if (deviceVersion == null) {
			if (other.deviceVersion != null)
				return false;
		} else if (!deviceVersion.equals(other.deviceVersion))
			return false;
		if (deviceModel == null) {
			if (other.deviceModel != null)
				return false;
		} else if (!deviceModel.equals(other.deviceModel))
			return false;
		if (deviceSystemVersion == null) {
			if (other.deviceSystemVersion != null)
				return false;
		} else if (!deviceSystemVersion.equals(other.deviceSystemVersion))
			return false;
		if (deviceDescendantVersion == null) {
			if (other.deviceDescendantVersion != null)
				return false;
		} else if (!deviceDescendantVersion.equals(other.deviceDescendantVersion))
			return false;
		return true;
	}
	
	
	
}
