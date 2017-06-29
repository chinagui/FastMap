package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/**
 * Collector POI导入类的请求参数类，POI唯一标识使用fid
 * @ClassName: DefaultPoiImportorCommand
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: DefaultPoiImportorCommand.java
 */
public class CollectorPoiImportorCommand extends AbstractCommand {
	
	private int dbId;
	
	protected CollectorUploadPois pois;

	public CollectorPoiImportorCommand(int dbId,CollectorUploadPois pois){
		this.dbId=dbId;
		this.pois=pois;
	}
	
	public CollectorUploadPois getPois() {
		return pois;
	}
	public void setPois(CollectorUploadPois pois) {
		this.pois = pois;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
}
