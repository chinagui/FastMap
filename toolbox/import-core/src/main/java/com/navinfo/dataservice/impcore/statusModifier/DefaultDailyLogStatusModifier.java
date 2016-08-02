package com.navinfo.dataservice.impcore.statusModifier;

import com.navinfo.dataservice.commons.database.OracleSchema;

/*
 * @author MaYunFei
 * 2016年7月21日
 * 描述：import-corePoiReleaseDailyLogStatusModifier.java
 */
public class DefaultDailyLogStatusModifier extends LogStatusModifier {

	private String tempTable=null;

	public DefaultDailyLogStatusModifier(OracleSchema logSchema,String tempTable) {
		super(logSchema);
		this.tempTable = tempTable;
	}

	@Override
	protected String getStatusModSql() {
		return "update LOG_DAY_RELEASE set rel_all_dt = sysdate,rel_all_sta=1,REL_all_LOCK=0 where OP_ID IN (SELECT OP_ID FROM "+tempTable+")";
	}

}

