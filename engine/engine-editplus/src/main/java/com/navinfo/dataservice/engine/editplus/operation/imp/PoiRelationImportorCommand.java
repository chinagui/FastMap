package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.util.Collection;

import com.navinfo.dataservice.dao.plus.operation.AbstractCommand;

/** 
 * @ClassName: PoiRelationImporterCommand
 * @author xiaoxiaowen4127
 * @date 2016年11月28日
 * @Description: PoiRelationImporterCommand.java
 */
public class PoiRelationImportorCommand extends AbstractCommand {
	protected Collection<PoiRelation> poiRels;

	public Collection<PoiRelation> getPoiRels() {
		return poiRels;
	}

	public void setPoiRels(Collection<PoiRelation> poiRels) {
		this.poiRels = poiRels;
	} 
}
