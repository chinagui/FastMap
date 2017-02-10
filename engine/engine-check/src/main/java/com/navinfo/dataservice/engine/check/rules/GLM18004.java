package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM18004
 * @author Han Shaoming
 * @date 2017年1月4日 下午3:52:49
 * @Description TODO
 * 路口的限制信息数组中不能同时存在禁行（普通交限）和路口语音引导
 * 关系类型编辑（语音引导详细信息表）服务端后检查
 * 新增语音引导服务端后检查
 */
public class GLM18004 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//关系类型编辑（语音引导详细信息表）
			if (row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail) row;
				this.checkRdVoiceguideDetail(rdVoiceguideDetail);
			}
			//新增语音引导
			else if (row instanceof RdVoiceguide){
				RdVoiceguide rdVoiceguide = (RdVoiceguide) row;
				this.checkRdVoiceguide(rdVoiceguide);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdVoiceguide
	 * @throws Exception 
	 */
	private void checkRdVoiceguide(RdVoiceguide rdVoiceguide) throws Exception {
		// TODO Auto-generated method stub
		//判断新增
		if(ObjStatus.INSERT.equals(rdVoiceguide.status())){
			boolean check = this.check(rdVoiceguide.getPid());
			
			if(check){
				String target = "[RD_VOICEGUIDE," + rdVoiceguide.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
			
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdVoiceguideDetail
	 * @throws Exception 
	 */
	private void checkRdVoiceguideDetail(RdVoiceguideDetail rdVoiceguideDetail) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdVoiceguideDetail.changedFields();
		if(changedFields != null && changedFields.containsKey("relationshipType")){
			int relationshipType = Integer.parseInt((String) changedFields.get("relationshipType"));
			if(relationshipType == 1){
				boolean check = this.check(rdVoiceguideDetail.getVoiceguidePid());
				
				if(check){
					String target = "[RD_VOICEGUIDE," + rdVoiceguideDetail.getVoiceguidePid() + "]";
					this.setCheckResult("", target, 0);
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int voiceguidePid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT RV.PID FROM RD_VOICEGUIDE RV,RD_VOICEGUIDE_DETAIL  RVD,");
	    sb.append(" RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RV.PID = "+voiceguidePid);
		sb.append(" AND RV.PID = RVD.VOICEGUIDE_PID AND RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RV.IN_LINK_PID = RR.IN_LINK_PID AND RV.NODE_PID = RR.NODE_PID");
		sb.append(" AND RVD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		//sb.append(" AND RVD.RELATIONSHIP_TYPE = 1 AND RRD.RELATIONSHIP_TYPE = 1");
		sb.append(" AND RRD.TYPE = 1 AND RV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2 AND RVD.U_RECORD <> 2 AND RRD.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("后检查GLM18004--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
