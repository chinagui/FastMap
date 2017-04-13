/**
 * 
 */
package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.engine.check.core.NiValException;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: COM60155
 * @author Zhang Xiaolong
 * @date 2016年11月25日 下午4:10:27
 * @Description: 同一个TMC位置点和同一个link创建的两组匹配信息中，方向关系不能相同
 */
public class COM60155 extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdTmclocation) {
				RdTmclocation tmclocation = (RdTmclocation) obj;

				List<IRow> tmcLocationLinks = tmclocation.getLinks();
				//新增tmc匹配，检查子表数据正确性
				for (IRow row : tmcLocationLinks) {
					checkTmcLocDirect(row, checkCommand.getOperType());
				}
			} else if (obj instanceof RdTmclocationLink) {
				//修改tmc子表数据
				checkTmcLocDirect(obj, checkCommand.getOperType());
			}
		}
	}

	private void checkTmcLocDirect(IRow row, OperType operType) throws Exception {
		RdTmclocationLink link = (RdTmclocationLink) row;

		int direct = link.getDirect();

		//修改操作从changeFields里获取修改后的方向值
		if (operType == OperType.UPDATE && link.changedFields().containsKey("direct")) {
			direct = (int) link.changedFields().get("direct");
		}

		StringBuilder sb = new StringBuilder();
		 
		sb.append("SELECT RT.GROUP_ID,RTL.LINK_PID FROM RD_TMCLOCATION RT,RD_TMCLOCATION_LINK RTL");
		sb.append(" WHERE RT.GROUP_ID ="+link.getGroupId());
		sb.append(" AND RT.GROUP_ID = RTL.GROUP_ID");
		sb.append(" AND RT.U_RECORD <>2 AND RTL.U_RECORD <>2");
		sb.append(" AND EXISTS(SELECT 1 FROM RD_TMCLOCATION RT1,RD_TMCLOCATION_LINK RTL1");
		sb.append(" WHERE RT1.GROUP_ID <> RT.GROUP_ID AND RTL1.DIRECT = RTL.DIRECT");
		sb.append(" AND RT1.TMC_ID = RT.TMC_ID AND RTL1.LINK_PID = RTL.LINK_PID");
		sb.append(" AND RT1.GROUP_ID = RTL1.GROUP_ID AND RT1.U_RECORD <>2 AND RTL1.U_RECORD <>2)");
		String sql = sb.toString();

		log.info("后检查COM60155--sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(!resultList.isEmpty()){
			String target = "[RD_TMCLOCATION," + link.getGroupId() + "]";
			this.setCheckResult("", target, 0);
			return;
		}
	}
}
