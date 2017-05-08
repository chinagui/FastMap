package com.navinfo.dataservice.engine.dropbox.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.dropbox.iface.DropboxApi;
import com.navinfo.dataservice.api.dropbox.model.UploadInfo;

/** 
 * @ClassName: DropboxApiImpl
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: DropboxApiImpl.java
 */
@Service("dropboxApi")
public class DropboxApiImpl implements DropboxApi {

	@Override
	public UploadInfo getUploadInfoByJobId(int jobId) throws Exception {
		return UploadInfoService.getInstance().getByJobId(jobId);
	}

}
