/**
 * 
 */
package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: COM60154
 * @author Zhang Xiaolong
 * @date 2016年11月25日 下午4:10:27
 * @Description: 同一个TMC点和同一个link组成的两组匹配信息中，“位置方向”不能重复
 */
public class COM60154 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			//新增tmc匹配，检查子表数据正确性
			if (obj instanceof RdTmclocation) {
				RdTmclocation rdTmclocation = (RdTmclocation) obj;
				this.checkRdTmclocation(rdTmclocation);
			} 
			//修改tmc子表数据
			else if (obj instanceof RdTmclocationLink) {
				RdTmclocationLink rdTmclocationLink = (RdTmclocationLink) obj;
				this.checkRdTmclocationLink(rdTmclocationLink);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdTmclocation
	 * @throws Exception 
	 */
	private void checkRdTmclocation(RdTmclocation rdTmclocation) throws Exception {
		//判断新增
		if(ObjStatus.INSERT.equals(rdTmclocation.status())){
			boolean check = this.check(rdTmclocation.getPid());
			
			if(check){
				String target = "[RD_TMCLOCATION," + rdTmclocation.getPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdTmclocationLink
	 * @throws Exception 
	 */
	private void checkRdTmclocationLink(RdTmclocationLink rdTmclocationLink) throws Exception {
		if(ObjStatus.UPDATE.equals(rdTmclocationLink.status())){
			Map<String, Object> changedFields = rdTmclocationLink.changedFields();
			if(changedFields != null && !changedFields.isEmpty()){
				if(changedFields.containsKey("locDirect")){
					boolean check = this.check(rdTmclocationLink.getGroupId());
					
					if(check){
						String target = "[RD_TMCLOCATION," + rdTmclocationLink.getGroupId() + "]";
						this.setCheckResult("", target, 0);
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
	private boolean check(int groupId) throws Exception {

		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT RT.GROUP_ID,RTL.LINK_PID FROM RD_TMCLOCATION RT,RD_TMCLOCATION_LINK RTL");
		sb.append(" WHERE RT.GROUP_ID ="+groupId);
		sb.append(" AND RT.GROUP_ID = RTL.GROUP_ID");
		sb.append(" AND RT.U_RECORD <>2 AND RTL.U_RECORD <>2");
		sb.append(" AND EXISTS(SELECT 1 FROM RD_TMCLOCATION RT1,RD_TMCLOCATION_LINK RTL1");
		sb.append(" WHERE RT1.GROUP_ID <> RT.GROUP_ID AND RTL1.LOC_DIRECT = RTL.LOC_DIRECT");
		sb.append(" AND RT1.TMC_ID = RT.TMC_ID AND RTL1.LINK_PID = RTL.LINK_PID");
		sb.append(" AND RT1.GROUP_ID = RTL1.GROUP_ID AND RT1.U_RECORD <>2 AND RTL1.U_RECORD <>2)");
		
		String sql = sb.toString();
		log.info("后检查COM60154--sql:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			flag = true;
		}
		return flag;
	}
}
