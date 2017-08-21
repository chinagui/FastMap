package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

public class IxDealershipHistoryOperator {
	private static Logger log = LoggerRepos.getLogger(DataEditService.class);
	
	public static void addWorkflowStatusHistory(Connection conn,IxDealershipResult bean,Long userId)throws ServiceException{
		try{
			if (bean!=null&&bean.getOldValues()!=null && bean.getOldValues().containsKey("WORKFLOW_STATUS")){
				QueryRunner run = new QueryRunner();
				String sqlStr="insert into ix_dealership_history "
						+ "(history_id,result_id,field_name,u_record,old_value,new_value,u_date,user_id)"
						+ "values(HISTORY_SEQ.NEXTVAL,"+ bean.getResultId() +",'WORKFLOW_STATUS',2,'"+bean.getOldValues().get("WORKFLOW_STATUS")+"','"+bean.getWorkflowStatus()+"',sysdate,"+userId+")";
				run.execute(conn,sqlStr);
			};
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new ServiceException("修改失败，原因为:"+e.getMessage(),e);
		}
	}
}
