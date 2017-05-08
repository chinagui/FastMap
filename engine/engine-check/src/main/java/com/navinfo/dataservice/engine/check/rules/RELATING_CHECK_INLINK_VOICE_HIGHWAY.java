package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName RELATING_CHECK_INLINK_VOICE_HIGHWAY
 * @author Han Shaoming
 * @date 2017年3月23日 下午3:56:30
 * @Description TODO
 * 高速道路、城市高速不能作为路口语音引导信息的进入线
 * 新增语音引导	服务端后检查
 * 修改语音引导	服务端后检查
 * link种别编辑	服务端后检查
 */
public class RELATING_CHECK_INLINK_VOICE_HIGHWAY extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//修改语音引导
			if (row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail) row;
				this.checkRdVoiceguideDetail(rdVoiceguideDetail);
			}
			//新增语音引导
			else if (row instanceof RdVoiceguide){
				RdVoiceguide rdVoiceguide = (RdVoiceguide) row;
				this.checkRdVoiceguide(rdVoiceguide);
			}
			//Link种别编辑
			else if (row instanceof RdLink){
				RdLink rdLink = (RdLink) row;
				this.checkRdLink(rdLink);
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
		if(ObjStatus.UPDATE.equals(rdVoiceguideDetail.status())){
			Map<String, Object> changedFields = rdVoiceguideDetail.changedFields();
			if(changedFields != null && !changedFields.isEmpty() ){
				if(changedFields.containsKey("relationshipType")){
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
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int pid) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT RV.PID FROM RD_VOICEGUIDE RV,RD_VOICEGUIDE_DETAIL RVD,RD_LINK RL");
		sb.append(" WHERE RV.PID = "+pid);
		sb.append(" AND RV.PID = RVD.VOICEGUIDE_PID AND RELATIONSHIP_TYPE =1");
		sb.append(" AND RL.LINK_PID = RV.IN_LINK_PID AND RL.KIND IN(1,2)");
		sb.append(" AND RV.U_RECORD <>2 AND RVD.U_RECORD <>2 AND RL.U_RECORD <>2");
		String sql = sb.toString();
		log.info("后检查RELATING_CHECK_INLINK_VOICE_HIGHWAY--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLink
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink) throws Exception {
		// TODO Auto-generated method stub
		if(rdLink.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLink.changedFields();
			if(changedFields != null && !changedFields.isEmpty()){
				//Link种别编辑
				if(changedFields.containsKey("kind")){
					int kind = (int) changedFields.get("kind");
					if(kind == 1 || kind == 2){
						StringBuilder sb = new StringBuilder();
						
						sb.append("SELECT RV.PID FROM RD_VOICEGUIDE RV,RD_VOICEGUIDE_DETAIL RVD");
						sb.append(" WHERE RV.IN_LINK_PID = "+rdLink.getPid());
						sb.append(" AND RV.PID = RVD.VOICEGUIDE_PID AND RELATIONSHIP_TYPE =1");
						sb.append(" AND RV.U_RECORD <>2 AND RVD.U_RECORD <>2");
						
						String sql = sb.toString();
						log.info("RdLink后检查RELATING_CHECK_INLINK_VOICE_HIGHWAY--sql:" + sql);
						
						DatabaseOperator getObj = new DatabaseOperator();
						List<Object> resultList = new ArrayList<Object>();
						resultList = getObj.exeSelect(this.getConn(), sql);
						
						if(!resultList.isEmpty()){
							String target = "[RD_LINK," + rdLink.getPid() + "]";
							this.setCheckResult("", target, 0);
						}
					}
				}
			}
		}
	}

}
