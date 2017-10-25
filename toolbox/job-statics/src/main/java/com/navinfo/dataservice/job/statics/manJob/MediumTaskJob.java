package com.navinfo.dataservice.job.statics.manJob;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.ManConstant;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.statics.tools.MongoDao;
import com.navinfo.dataservice.engine.statics.tools.OracleDao;
import com.navinfo.dataservice.engine.statics.tools.StatUtil;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.exception.ServiceException;
import net.sf.json.JSONObject;

/**
 * 任务统计
 * @ClassName TaskJob
 * @author Han Shaoming
 * @date 2017年8月4日 下午8:42:39
 * @Description TODO
 */
public class MediumTaskJob extends AbstractStatJob {
	
	public MediumTaskJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String stat() throws JobException {
		try {
			//获取统计时间
			MediumTaskJobRequest statReq = (MediumTaskJobRequest)request;
			log.info("start stat "+statReq.getJobType());
			TaskJobUtil util=new TaskJobUtil();
			JSONObject result = util.stat(statReq.getTimestamp(), statReq.getProgramType());
			log.info("end stat "+statReq.getJobType());
			return result.toString();
			
		} catch (Exception e) {
			log.error("任务统计:"+e.getMessage(), e);
			throw new JobException("任务统计:"+e.getMessage(),e);
		}
	}
	
}
