package com.navinfo.dataservice.engine.edit.service;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.EditApi;

/** 
 * @ClassName: EditApiImpl
 * @author xiaoxiaowen4127
 * @date 2016年8月23日
 * @Description: EditApiImpl.java
 */
@Service("editApi")
public class EditApiImpl implements EditApi {

	@Override
	public long applyPid(String tableName, int count) throws Exception {
		return PidService.getInstance().applyPid(tableName, count);
	}

}
