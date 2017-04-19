package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM54004
 * @author Han Shaoming
 * @date 2017年3月27日 下午1:37:49
 * @Description TODO
 * 只要是做了车辆限制的道路（有时间段不检查）都不应该制作位置表信息，否则报log
 * 限制类型编辑	服务端后检查
 * 时间段编辑（link限制表）	服务端后检查
 * TMC图面编辑(新增)	服务端后检查
 */
public class GLM54004 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow row : checkCommand.getGlmList()) {
			//新增rdtmclocation对象，检查子表数据
			if (row instanceof RdTmclocation) {
				RdTmclocation rdTmclocation = (RdTmclocation) row;
				this.checkRdTmclocation(rdTmclocation);
			}
			//限制类型编辑,时间段编辑（link限制表）
			else if(row instanceof RdLinkLimit){
				RdLinkLimit rdLinkLimit = (RdLinkLimit) row;
				this.checkRdLinkLimit(rdLinkLimit);
			}
		}

	}

	/**
	 * @author Han Shaoming
	 * @param rdTmclocation
	 * @throws Exception 
	 */
	private void checkRdTmclocation(RdTmclocation rdTmclocation) throws Exception {
		if(rdTmclocation.status().equals(ObjStatus.INSERT)){
			List<IRow> tmcLocationLinks = rdTmclocation.getLinks();
			for (IRow subRow : tmcLocationLinks) {
				RdTmclocationLink rdTmclocationLink = (RdTmclocationLink) subRow;
				boolean check = this.check(rdTmclocationLink.getLinkPid());
				if(check){
					String target = "[RD_TMCLOCATION," + rdTmclocationLink.getGroupId() + "]";
					this.setCheckResult("", target, 0);
					return;
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdLinkLimit
	 * @throws Exception 
	 */
	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		boolean checkFlag = false;
		if(rdLinkLimit.status().equals(ObjStatus.INSERT)){
			int type = rdLinkLimit.getType();
			if(type == 2){
				checkFlag = true;
			}
		}
		else if(rdLinkLimit.status().equals(ObjStatus.UPDATE)){
			Map<String, Object> changedFields = rdLinkLimit.changedFields();
			if(changedFields != null && !changedFields.isEmpty()){
				if(changedFields.containsKey("timeDomain")){
					String timeDomain = (String) changedFields.get("timeDomain");
					if(StringUtils.isEmpty(timeDomain)){
						checkFlag = true;
					}
				}
			}
			if(changedFields != null && !changedFields.isEmpty()){
				if(changedFields.containsKey("type")){
					int type = (int) changedFields.get("type");
					if(type == 2){
						checkFlag = true;
					}
				}
			}
		}
		if(checkFlag){
			boolean check = this.check(rdLinkLimit.getLinkPid());
			
			if(check){
				String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
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
		
		sb.append("SELECT DISTINCT RLM.LINK_PID");
		 sb.append(" FROM RD_TMCLOCATION_LINK RTL, RD_LINK_LIMIT RLM");
		sb.append(" WHERE RLM.LINK_PID = "+pid);
		sb.append(" AND RLM.TYPE = 2 AND RLM.TIME_DOMAIN IS NULL");
		sb.append(" AND RTL.LINK_PID = RLM.LINK_PID");
		sb.append(" AND RTL.U_RECORD <>2 AND RLM.U_RECORD <>2");
		String sql = sb.toString();
		log.info("后检查GLM54004--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}

}
