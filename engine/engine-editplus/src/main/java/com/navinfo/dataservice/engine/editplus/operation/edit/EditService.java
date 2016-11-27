package com.navinfo.dataservice.engine.editplus.operation.edit;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.plus.operation.OperationFactory;

import net.sf.json.JSONObject;

/** 
 * @ClassName: EditService
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: EditService.java
 */
public class EditService {
	
	public JSONObject runCmd(int dbId,String opType,String objType,JSONObject data)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getConnectionById(dbId);
			EditOperation op = (EditOperation)OperationFactory.getInstance().create(objType+opType,conn);
			EditCommand cmd = null;
//			cmd = new EditCommand();
			cmd.parse(data);
			op.setCmd(cmd);
			op.operate();
			//组装返回json
			return DefaultPrimaryPidChooser.chooseMainObj(op.getResult());
		}catch(Exception e){
			
		}finally{
			DbUtils.closeQuietly(conn);
		}
		return null;
	}
}
