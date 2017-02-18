package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM18010
 * @author Han Shaoming
 * @date 2017年2月17日 下午5:27:47
 * @Description TODO
 * 检查对象：语音引导信息
 * 检查原则：
 * 1.检查对象进入线的终点必须至少挂接3条Link，否则报log；
 * 2.检查对象退出线的起点必须至少挂接3条Link，否则报log
 * 新增语音引导	服务端前检查
 */
public class GLM18010 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow row:checkCommand.getGlmList()){
			//新增语音引导
			if (row instanceof RdVoiceguide){
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
					if(vias == null || vias.isEmpty()){
						StringBuilder sb = new StringBuilder();
						
						sb.append("SELECT DISTINCT RL.LINK_PID FROM RD_LINK RL");
						sb.append(" WHERE (RL.S_NODE_PID = "+rdVoiceguide.getNodePid());
						sb.append(" OR RL.E_NODE_PID = "+rdVoiceguide.getNodePid()+")");
						sb.append(" AND RL.U_RECORD <>2");
						String sql = sb.toString();
						log.info("前检查GLM18010--sql:" + sql);
						
						DatabaseOperator getObj = new DatabaseOperator();
						List<Object> resultList = new ArrayList<Object>();
						resultList = getObj.exeSelect(this.getConn(), sql);
						
						if(resultList.size()<3){
							String target = "[RD_VOICEGUIDE," + rdVoiceguide.getPid() + "]";
							this.setCheckResult("", target, 0);
						}
					}
					//线线关系
					else if(!vias.isEmpty()){
						List<Integer> viaPids = new ArrayList<Integer>();
						for (IRow iRows : vias) {
							if(iRows instanceof RdVoiceguideVia){
								RdVoiceguideVia rdVoiceguideVia = (RdVoiceguideVia) iRows;
								viaPids.add(rdVoiceguideVia.getLinkPid());
							}
						}
						for (Integer pid : linkPids) {
							boolean check = this.check(pid,viaPids);
							
							if(check){
								String target = "[RD_VOICEGUIDE," + rdVoiceguide.getPid() + "]";
								this.setCheckResult("", target, 0);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param viaPids 
	 * @param rdNodeForm
	 * @throws Exception 
	 */
	private boolean check(int pid, List<Integer> viaPids) throws Exception {
		// TODO Auto-generated method stub
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("WITH T AS(SELECT RL.S_NODE_PID NODE_PID FROM RD_LINK RL,RD_LINK RL1 WHERE");
		sb.append(" RL.LINK_PID = "+pid);
		sb.append(" AND (RL.S_NODE_PID = RL1.S_NODE_PID OR RL.S_NODE_PID = RL1.E_NODE_PID)");
		sb.append(" AND RL1.LINK_PID IN("+StringUtils.join(viaPids,",")+")");
		sb.append(" AND RL.U_RECORD <>2 AND RL1.U_RECORD <>2");
		sb.append(" UNION");
		sb.append(" SELECT RL.E_NODE_PID NODE_PID FROM RD_LINK RL,RD_LINK RL2 WHERE");
		sb.append(" RL.LINK_PID = "+pid);
		sb.append(" AND (RL.E_NODE_PID = RL2.S_NODE_PID OR RL.E_NODE_PID = RL2.E_NODE_PID)");
		sb.append(" AND RL2.LINK_PID IN("+StringUtils.join(viaPids,",")+")");
		sb.append(" AND RL.U_RECORD <>2 AND RL2.U_RECORD <>2)");
		sb.append(" SELECT T.NODE_PID FROM RD_LINK RL ,T");
		sb.append(" WHERE (RL.S_NODE_PID = T.NODE_PID OR RL.E_NODE_PID = T.NODE_PID)");
		sb.append(" AND RL.U_RECORD <>2 GROUP BY T.NODE_PID HAVING COUNT(1)<3");
		String sql = sb.toString();
		log.info("前检查GLM18010--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
