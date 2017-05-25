package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/** 
 * @ClassName: PoiDayStatStarter
 * @author songdongyan
 * @date 2017年5月25日
 * @Description: PoiDayStatStarter.java
 */
public class PoiDayStatStarter extends StatJobStarter {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.statics.launcher.StatJobStarter#jobType()
	 */
	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "poiDayStat";
	}

}
