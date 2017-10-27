package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.api.job.model.RunJobInfo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

import net.sf.json.JSONObject;

/**
 * 项目数据统计启动类
 * @ClassName MediumProgramStatStarter
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:48:48
 * @Description TODO
 */
public class MediumProgramStatStarter extends StatJobStarter {

	@Override
	public String jobType() {
		// TODO Auto-generated method stub
		return "mediumProgramStat";
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
		request.put("type", 1);
		RunJobInfo info = new RunJobInfo(jobType(),request);
		return info;
	}
}
