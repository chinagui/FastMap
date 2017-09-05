package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

import net.sf.json.JSONObject;

/**
 * PersonTips starter
 * Created by zhangjunfang on 2017/8/10.
 */
public class PersonTipsStatStarter extends StatJobStarter {

    @Override
    public String jobType() {
        return "personTipsJob";
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
