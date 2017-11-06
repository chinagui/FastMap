package com.navinfo.dataservice.engine.limit.glm.model.limit.man;

import java.io.Serializable;

/**
 * Created by ly on 2017/10/26.
 */
public class UserInfo  implements Serializable {
    private Integer userId ;
    private String userRealName ;
    private String userNickName ;
    private String userPassword ;
    private String userEmail ;
    private String userPhone ;


    public UserInfo (){
    }

    public UserInfo (Integer userId ,String userRealName,String userNickName,String userPassword,String userEmail,String userPhone,Integer userLevel,Integer userScore,Object userIcon,String userGpsid,Integer risk){
        this.userId=userId ;
        this.userRealName=userRealName ;
        this.userNickName=userNickName ;
        this.userPassword=userPassword ;
        this.userEmail=userEmail ;
        this.userPhone=userPhone ;

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
        if(null==userEmail){return "";}
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "UserInfo [userId=" + userId +",userRealName="+userRealName+",userNickName="+userNickName+",userPassword="+userPassword+",userEmail="+userEmail+",userPhone="+userPhone+"]";
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

        return true;
    }



}
