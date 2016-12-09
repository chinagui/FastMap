package com.navinfo.dataservice.engine.edit.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.PidService;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.engine.batch.BatchProcess;
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
	 * @Title: updatePoifreshVerified
	 * @Description: 修改的数据，安卓上传：1)若只有照片和备注的履历，则为鲜度验证,更新为待作业;2)若没任何修改履历，也为鲜度验证，更新为已作业
	 * web:不修改状态，只做鲜度验证字段的判断修改，web保存的数据总在已作业
	 * (修)(第七迭代) 变更:当新增 poi_edit_status 时,为 commit_his_status 字段赋默认值 0 
	 * @param pid
	 * @param platform
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年12月8日 下午1:52:16 
	 */
	public void updatePoifreshVerified(int pid,String platform) throws Exception {
			LogReader lr=new LogReader(conn);
			int freshVerified=0;
			int status=1;
			if(!lr.isExistObjHis(pid)){
				freshVerified=1;
				status=2;
			}
			if(lr.isExistObjHis(pid) && lr.isOnlyPhotoAndMetoHis(pid)){
				freshVerified=1;
			}
			String sql=null;
			if ("web".endsWith(platform)){
				sql="UPDATE poi_edit_status T1 SET T1.fresh_verified = :1 where T1.pid =" + pid ;
			}else{
				sql="UPDATE poi_edit_status T1 SET T1.fresh_verified = :1,T1.status="+status+" where T1.pid = " + pid;
			}
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

	@Override
	public void runBatch(JSONObject dataObj) throws Exception {
		BatchProcess batchProcess = new BatchProcess("row","save");
		Connection subConn = null;
		int dbId = dataObj.getInt("dbId");
		try {
			subConn = DBConnector.getInstance().getConnectionById(dbId);
			List<String> batchList = batchProcess.getRowRules();
			batchProcess.execute(dataObj, subConn, new EditApiImpl(subConn), batchList);
			subConn.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(subConn);
		}
	}
}
