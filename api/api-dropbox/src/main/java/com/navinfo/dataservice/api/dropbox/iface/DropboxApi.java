package com.navinfo.dataservice.api.dropbox.iface;

import com.navinfo.dataservice.api.dropbox.model.UploadInfo;

/** 
 * @ClassName: DropboxApi
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: DropboxApi.java
 */
public interface DropboxApi {
	UploadInfo getUploadInfoByJobId(int jobId)throws Exception;
}
