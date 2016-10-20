package com.navinfo.dataservice.engine.edit.service;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.bizcommons.service.PidService;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.edit.operation.Transaction;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

/**
 * editApi的实现类
 * 
 * @ClassName: EditApiImpl
 * @author Zhang Xiaolong
 * @date 2016年8月24日 下午7:16:24
 * @Description: TODO
 */
@Service("editApi")
public class EditApiImpl implements EditApi {
	private Connection conn;
	
	private long token;
	
	public EditApiImpl(){
	}
	
	public EditApiImpl(Connection conn){
		this.conn = conn;
	}
	
	public long getToken() {
		return token;
	}

	public void setToken(long token) {
		this.token = token;
	}

	@Override
	public JSONObject run(JSONObject dataObj) throws Exception {
		Transaction t = new Transaction(dataObj.toString());
		
		t.setUserId(token);

		String msg = t.run();

		String log = t.getLogs();

		JSONObject json = new JSONObject();

		json.put("result", msg);

		json.put("log", log);

		json.put("check", t.getCheckLog());

		json.put("pid", t.getPid());

		return json;
	}
	
	public JSONObject runPoi(JSONObject dataObj) throws Exception {
		Transaction t = new Transaction(dataObj.toString(),conn);
		
		t.setUserId(this.token);

		String msg = t.innerRun();

		String log = t.getLogs();

		JSONObject json = new JSONObject();

		json.put("result", msg);

		json.put("log", log);

		json.put("check", t.getCheckLog());

		json.put("pid", t.getPid());

		return json;
	}

	/**
	 * 修改的数据，若没有履历或者只有照片和备注的履历，则为鲜度验证
	 * @param pid
	 * @param conn
	 * @throws Exception
	 */
	public void updatePoifreshVerified(int pid) throws Exception {
			LogReader lr=new LogReader(conn);
			int freshVerified=0;
//			if(!lr.isExistObjHis(pid) || lr.isOnlyPhotoAndMetoHis(pid)){
//				freshVerified=1;
//			}
			String sql="UPDATE poi_edit_status T1 SET T1.fresh_verified = :1 where T1.row_id =(SELECT row_id as a FROM ix_poi where pid = " + pid + ")";
			
			PreparedStatement pstmt = null;
			try {
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, freshVerified);
				pstmt.executeUpdate();
			} catch (Exception e) {
				throw e;

			} finally {
				DBUtils.closeStatement(pstmt);
			}
		}

	@Override
	public long applyPid(String tableName, int count) throws Exception {
		return PidService.getInstance().applyPid(tableName, count);
	}
}
