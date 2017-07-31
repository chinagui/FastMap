package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/** 
 * @ClassName: PoiMonthStatStarter
 * @author songdongyan
 * @date 2017年5月25日
 * @Description: PoiMonthStatStarter.java
 */
public class MonthPoiStatStarter extends StatJobStarter {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.statics.launcher.StatJobStarter#jobType()
	 */
	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "monthPoiStat";
	}

}
