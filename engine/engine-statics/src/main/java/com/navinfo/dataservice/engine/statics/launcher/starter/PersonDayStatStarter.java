package com.navinfo.dataservice.engine.statics.launcher.starter;

import java.text.ParseException;

import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

import net.sf.json.JSONObject;

/** 
 * @ClassName: PoiDayStatStarter
 * @author songhe
 * @date 2017年8月03日
 * @Description: PersonDayStatStarter.java
 * 
 */
public class PersonDayStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "personDayJob";
	}
	/**
	 * 如果不需要启动，RunJobInfo==null，配置job启动参数
	 * @return
	 * @throws Exception 
	 */
	protected RunJobInfo startRun() throws Exception{		
		//默认启动参数timestamp，取当前时间的小时的整点
		String timestamp=DateUtils.dateToString(DateUtils.getSysdate(), "yyyyMMddHH0000");
		JSONObject request=new JSONObject();
		request.put("timestamp", timestamp);
		request.put("workDay", DateUtils.dateToString(DateUtils.getDayBefore(
				DateUtils.stringToDate(timestamp, DateUtils.DATE_YMD)),DateUtils.DATE_YMD));
		RunJobInfo info = new RunJobInfo(jobType(),request);
		return info;
	}
}
