package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.api.edit.upload.UploadPois;
import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/**
 * POI导入类的请求参数类，导入一般为外部平台，所以POI唯一标识使用fid
 * @ClassName: DefaultPoiImportorCommand
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: DefaultPoiImportorCommand.java
 */
public class DefaultPoiImportorCommand extends AbstractCommand {
	
	private int dbId;
	
	protected UploadPois pois;

	public DefaultPoiImportorCommand(int dbId,UploadPois pois){
		this.dbId=dbId;
		this.pois=pois;
	}
	
	public UploadPois getPois() {
		return pois;
	}
	public void setPois(UploadPois pois) {
		this.pois = pois;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
}
