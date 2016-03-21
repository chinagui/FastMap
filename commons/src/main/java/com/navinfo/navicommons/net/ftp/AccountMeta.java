package com.navinfo.navicommons.net.ftp;

import org.apache.commons.lang.StringUtils;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-7
 */
public class AccountMeta
{
    private String user;
    private int status;
    private String password;
    private int uid;
    private int gid;
    private String dir;
    private int ulBandWidth;
    private int dlBandWidth;
    private String comment;
    private String ipAccess;
    private int quotaSize;
    private int quotaFiles;
    private long sequenceId;//任务id

    public AccountMeta(String user, String password, String dir) {
        this.user = user;
        this.password = password;
        this.dir = dir;
    }
    public AccountMeta() {
       
    }

    public boolean validate()
    {
        return (
                StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)
                && uid != 0 && gid != 0
                && StringUtils.isNotEmpty(dir)
                );
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getUlBandWidth() {
        return ulBandWidth;
    }

    public void setUlBandWidth(int ulBandWidth) {
        this.ulBandWidth = ulBandWidth;
    }

    public int getDlBandWidth() {
        return dlBandWidth;
    }

    public void setDlBandWidth(int dlBandWidth) {
        this.dlBandWidth = dlBandWidth;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIpAccess() {
        return ipAccess;
    }

    public void setIpAccess(String ipAccess) {
        this.ipAccess = ipAccess;
    }

    public int getQuotaSize() {
        return quotaSize;
    }

    public void setQuotaSize(int quotaSize) {
        this.quotaSize = quotaSize;
    }

    public int getQuotaFiles() {
        return quotaFiles;
    }

    public void setQuotaFiles(int quotaFiles) {
        this.quotaFiles = quotaFiles;
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "AccountMeta{" +
                "user='" + user + '\'' +
                ", status=" + status +
                ", password='" + password + '\'' +
                ", uid=" + uid +
                ", gid=" + gid +
                ", dir='" + dir + '\'' +
                ", ulBandWidth=" + ulBandWidth +
                ", dlBandWidth=" + dlBandWidth +
                ", comment='" + comment + '\'' +
                ", ipAccess='" + ipAccess + '\'' +
                ", quotaSize=" + quotaSize +
                ", quotaFiles=" + quotaFiles +
                ", sequenceId=" + sequenceId +
                '}';
    }
}
