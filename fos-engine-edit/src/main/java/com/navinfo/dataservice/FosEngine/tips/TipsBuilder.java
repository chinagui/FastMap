package com.navinfo.dataservice.FosEngine.tips;

import com.navinfo.dataservice.FosEngine.comm.db.HBaseAddress;
import com.navinfo.dataservice.FosEngine.comm.db.OracleAddress;
import com.navinfo.dataservice.FosEngine.tips.RdRestriction.RdRestrictionTipsBuilder;

/**
 * 创建Tips
 */
public class TipsBuilder {

	/**
	 * 执行创建Tips
	 * 
	 * @param fmgdbOA
	 *            fmgdb+连接
	 * @param pmOA
	 *            项目库连接
	 * @param uuid
	 *            唯一标识
	 * @return True成功
	 * @throws Exception
	 */
	public boolean run(OracleAddress fmgdbOA, OracleAddress pmOA, String uuid)
			throws Exception {

		// 交限tips
		RdRestrictionTipsBuilder.run(fmgdbOA.getConn(), pmOA.getConn(),
				HBaseAddress.getHBaseConnection(), uuid, 1);

		return false;
	}

}
