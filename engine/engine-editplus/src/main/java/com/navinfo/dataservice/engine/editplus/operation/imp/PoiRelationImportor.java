package com.navinfo.dataservice.engine.editplus.operation.imp;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;

/** 
 * @ClassName: PoiRelationImportor
 * @author xiaoxiaowen4127
 * @date 2016年11月25日
 * @Description: PoiRelationImportor.java
 */
public class PoiRelationImportor extends AbstractOperation{
	
	public PoiRelationImportor(Connection conn, String name, OperationResult preResult) {
		super(conn, name, preResult);
	}

	public void doImport(OperationResult or,List<PoiRelation> relations){
	}

	@Override
	public void operate() throws Exception {
		
	}
}
