package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName RELATING_CHECK_INLINK_OUTLINK_VOICE_KIND_9_10
 * @author Han Shaoming
 * @date 2017年2月10日 上午11:13:55
 * @Description TODO
 * 9级、10级路、人渡、步行街不能是语音引导的进入、经过、退出线
 * 新增语音引导	服务端前检查
 * 修改语音引导	服务端前检查
 */
public class RELATING_CHECK_INLINK_OUTLINK_VOICE_KIND_9_10 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
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
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

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
			List<Integer> linkPids = new ArrayList<Integer>();
			//进入线
			int inLinkPid = rdVoiceguide.getInLinkPid();
			linkPids.add(inLinkPid);
			//退出线
			List<IRow> details = rdVoiceguide.getDetails();
			for (IRow iRow : details) {
				 if (iRow instanceof RdVoiceguideDetail){
					RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail) iRow;
					linkPids.add(rdVoiceguideDetail.getOutLinkPid());
					//经过线
					List<IRow> vias = rdVoiceguideDetail.getVias();
					if(vias != null){
						for (IRow iRows : vias) {
							if(iRows instanceof RdVoiceguideVia){
								RdVoiceguideVia rdVoiceguideVia = (RdVoiceguideVia) iRows;
								linkPids.add(rdVoiceguideVia.getLinkPid());
							}
						}
					}
				}
			}
			for (Integer pid : linkPids) {
				boolean check = this.check(pid);
				
				if(check){
					String target = "[RD_VOICEGUIDE," + rdVoiceguide.getPid() + "]";
					this.setCheckResult("", target, 0);
					break;
				}
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
		if(changedFields != null && changedFields.containsKey("outLinkPid")){
			int outLinkPid = (int) changedFields.get("outLinkPid");
			boolean check = this.check(outLinkPid);
			
			if(check){
				String target = "[RD_VOICEGUIDE," + rdVoiceguideDetail.getVoiceguidePid() + "]";
				this.setCheckResult("", target, 0);
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
		 
		sb.append("SELECT RL.LINK_PID FROM RD_LINK RL,RD_LINK_FORM RLF WHERE RL.LINK_PID = "+pid);
		sb.append(" AND RL.LINK_PID = RLF.LINK_PID");
		sb.append(" AND (RL.KIND IN(9,10,11) OR RLF.FORM_OF_WAY = 20)");
		sb.append(" AND RL.U_RECORD <> 2 AND RLF.U_RECORD <> 2");
		String sql = sb.toString();
		log.info("前检查RELATING_CHECK_INLINK_OUTLINK_VOICE_KIND_9_10--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
