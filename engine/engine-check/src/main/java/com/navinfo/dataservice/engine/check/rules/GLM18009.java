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
 * @ClassName GLM18009
 * @author Han Shaoming
 * @date 2017年3月23日 下午5:24:34
 * @Description TODO
 * 语音引导详细信息中的语音代码中值域不能为“0”、“16”及“19”，否则报log；
 * 语音代码编辑	服务端后检查
 * 新增语音引导	服务端后检查
 */
public class GLM18009 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//语音代码编辑
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
		if(ObjStatus.UPDATE.equals(rdVoiceguideDetail.status())){
			Map<String, Object> changedFields = rdVoiceguideDetail.changedFields();
			if(changedFields != null && !changedFields.isEmpty() ){
				if(changedFields.containsKey("guideCode")){
					int guideCode = (int) changedFields.get("guideCode");
					if(guideCode == 0 ||guideCode == 16 || guideCode == 19){
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
		    
		sb.append("SELECT RVD.VOICEGUIDE_PID FROM RD_VOICEGUIDE_DETAIL RVD");
		sb.append(" WHERE RVD.VOICEGUIDE_PID = "+pid);
		sb.append(" AND RVD.U_RECORD != 2 AND RVD.GUIDE_CODE IN (0, 16, 19)");
		String sql = sb.toString();
		log.info("后检查GLM18009--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
