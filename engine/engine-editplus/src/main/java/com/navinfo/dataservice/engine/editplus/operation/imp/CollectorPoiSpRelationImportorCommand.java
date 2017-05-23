package com.navinfo.dataservice.engine.editplus.operation.imp;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/**
 * Collector POI导入类的请求参数类，POI唯一标识使用fid
 * @ClassName: DefaultPoiImportorCommand
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: DefaultPoiImportorCommand.java
 */
public class CollectorPoiSpRelationImportorCommand extends AbstractCommand {
	
	private int dbId;
	
	protected CollectorUploadPoiSpRelation rels;

	public CollectorPoiSpRelationImportorCommand(int dbId,CollectorUploadPoiSpRelation rels){
		this.dbId=dbId;
		this.rels=rels;
	}
	
	public CollectorUploadPoiSpRelation getRels() {
		return rels;
	}

	public void setRels(CollectorUploadPoiSpRelation rels) {
		this.rels = rels;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}
}
