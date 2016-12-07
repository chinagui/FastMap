/**
 * 
 */
package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName: PERMIT_CHECK_TMC_NUM
 * @author Zhang Xiaolong
 * @date 2016年11月25日 下午4:10:27
 * @Description: TODO
 */
public class PermitCheckTmcNum extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdTmclocation) {
				RdTmclocation tmclocation = (RdTmclocation) obj;

				List<IRow> tmcLocationLinks = tmclocation.getLinks();

				for (IRow row : tmcLocationLinks) {
					RdTmclocationLink link = (RdTmclocationLink) row;

					StringBuilder sb = new StringBuilder();

					sb.append("select link_pid from ("
							+ "select count(1) as num,link_pid from RD_TMCLOCATION_LINK a left join RD_TMCLOCATION b on a.GROUP_ID = b.GROUP_ID where a.U_RECORD !=2 and b.U_RECORD !=2 and a.LINK_PID =");
					sb.append(link.getLinkPid());
					sb.append(" group by a.link_pid,b.TMC_ID)where num>1");

					String sql = sb.toString();

					DatabaseOperator getObj = new DatabaseOperator();
					List<Object> resultList = new ArrayList<Object>();
					resultList = getObj.exeSelect(this.getConn(), sql);

					if (resultList.size() > 0) {
						this.setCheckResult("", "", 0,"同一个TMC位置点和同一个link最多创建两组匹配信息(linkPid:"+link.getLinkPid()+")");
						return;
					}
				}
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
}
