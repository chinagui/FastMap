package com.navinfo.dataservice.impcore.statusModifier;

import com.navinfo.dataservice.commons.database.OracleSchema;

/*
 * @author MaYunFei
 * 2016年7月21日
 * 描述：import-corePoiReleaseDailyLogStatusModifier.java
 */
public class PoiReleaseDailyLogStatusModifier extends LogStatusModifier {

	private String tempTablePoi=null;

	public PoiReleaseDailyLogStatusModifier(OracleSchema logSchema,String tempTable) {
		super(logSchema);
		this.tempTablePoi = tempTable;
	}

	@Override
	protected String getStatusModSql() {
		return "update LOG_DAY_RELEASE set rel_poi_dt = sysdate,rel_poi_sta=1,REL_POI_LOCK=0 where OP_ID IN (SELECT OP_ID FROM "+tempTablePoi+")";
	}

}

