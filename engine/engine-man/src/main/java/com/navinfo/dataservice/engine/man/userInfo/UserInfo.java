package com.navinfo.dataservice.engine.man.userInfo;


/** 
* @ClassName:  UserInfo 
* @author code generator
* @date 2016-06-14 03:05:15 
* @Description: TODO
*/
public class UserInfo  {
	private Integer userId ;
	private String userRealName ;
	private String userNickName ;
	private String userPassword ;
	private String userEmail ;
	private String userPhone ;
	private Integer userLevel ;
	private Integer userScore ;
	private Object userIcon ;
	private String userGpsid ;
	private String deviceToken;
	private String devicePlatform;
	private String deviceVersion;
	
	public UserInfo (){
	}
	
	public UserInfo (Integer userId ,String userRealName,String userNickName,String userPassword,String userEmail,String userPhone,Integer userLevel,Integer userScore,Object userIcon,String userGpsid){
		this.userId=userId ;
		this.userRealName=userRealName ;
		this.userNickName=userNickName ;
		this.userPassword=userPassword ;
		this.userEmail=userEmail ;
		this.userPhone=userPhone ;
		this.userLevel=userLevel ;
		this.userScore=userScore ;
		this.userIcon=userIcon ;
		this.userGpsid=userGpsid ;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUserRealName() {
		return userRealName;
	}
	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}
	public String getUserNickName() {
		return userNickName;
	}
	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserPhone() {
		return userPhone;
	}
	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}
	public Integer getUserLevel() {
		return userLevel;
	}
	public void setUserLevel(Integer userLevel) {
		this.userLevel = userLevel;
	}
	public Integer getUserScore() {
		return userScore;
	}
	public void setUserScore(Integer userScore) {
		this.userScore = userScore;
	}
	public Object getUserIcon() {
		return userIcon;
	}
	public void setUserIcon(Object userIcon) {
		this.userIcon = userIcon;
	}
	public String getUserGpsid() {
		return userGpsid;
	}
	public void setUserGpsid(String userGpsid) {
		this.userGpsid = userGpsid;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UserInfo [userId=" + userId +",userRealName="+userRealName+",userNickName="+userNickName+",userPassword="+userPassword+",userEmail="+userEmail+",userPhone="+userPhone+",userLevel="+userLevel+",userScore="+userScore+",userIcon="+userIcon+",userGpsid="+userGpsid+"]";
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((userRealName == null) ? 0 : userRealName.hashCode());
		result = prime * result + ((userNickName == null) ? 0 : userNickName.hashCode());
		result = prime * result + ((userPassword == null) ? 0 : userPassword.hashCode());
		result = prime * result + ((userEmail == null) ? 0 : userEmail.hashCode());
		result = prime * result + ((userPhone == null) ? 0 : userPhone.hashCode());
		result = prime * result + ((userLevel == null) ? 0 : userLevel.hashCode());
		result = prime * result + ((userScore == null) ? 0 : userScore.hashCode());
		result = prime * result + ((userIcon == null) ? 0 : userIcon.hashCode());
		result = prime * result + ((userGpsid == null) ? 0 : userGpsid.hashCode());
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
		UserInfo other = (UserInfo) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (userRealName == null) {
			if (other.userRealName != null)
				return false;
		} else if (!userRealName.equals(other.userRealName))
			return false;
		if (userNickName == null) {
			if (other.userNickName != null)
				return false;
		} else if (!userNickName.equals(other.userNickName))
			return false;
		if (userPassword == null) {
			if (other.userPassword != null)
				return false;
		} else if (!userPassword.equals(other.userPassword))
			return false;
		if (userEmail == null) {
			if (other.userEmail != null)
				return false;
		} else if (!userEmail.equals(other.userEmail))
			return false;
		if (userPhone == null) {
			if (other.userPhone != null)
				return false;
		} else if (!userPhone.equals(other.userPhone))
			return false;
		if (userLevel == null) {
			if (other.userLevel != null)
				return false;
		} else if (!userLevel.equals(other.userLevel))
			return false;
		if (userScore == null) {
			if (other.userScore != null)
				return false;
		} else if (!userScore.equals(other.userScore))
			return false;
		if (userIcon == null) {
			if (other.userIcon != null)
				return false;
		} else if (!userIcon.equals(other.userIcon))
			return false;
		if (userGpsid == null) {
			if (other.userGpsid != null)
				return false;
		} else if (!userGpsid.equals(other.userGpsid))
			return false;
		return true;
	}
	
	
	
}
