package com.navinfo.dataservice.engine.man.job.operator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.bean.JobDetail;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class JobDetailOperator {
	private Connection conn;
	private Logger log = LoggerRepos.getLogger(JobProgressOperator.class);
	public JobDetailOperator(Connection conn) {
        this.conn = conn;
    }
	public void insert(JobDetail jobDetail) throws SQLException {
        QueryRunner run = new QueryRunner();
        String sql = "insert into job_detail(job_id,type,num) values(?,?,?)";
        run.update(conn, sql, jobDetail.getJobId(), jobDetail.getType(), jobDetail.getNum());
    }
	
	public void batchInsert(Long jobId,JSONObject detailJson) throws SQLException {
		Iterator keyIter = detailJson.keys();
		while(keyIter.hasNext()){
			JobDetail detailTmp=new JobDetail();
			detailTmp.setJobId(jobId);
			String key = String.valueOf(keyIter.next());
			if(key.equals("tipsNum")){
				detailTmp.setType(1);
			}
			detailTmp.setNum(detailJson.getLong(key));
			insert(detailTmp);
		}
    }
}
