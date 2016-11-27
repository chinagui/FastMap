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
public class CheckTmcpointDirectPn extends baseRule {

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			if (obj instanceof RdTmclocation) {
				RdTmclocation tmclocation = (RdTmclocation) obj;

				List<IRow> tmcLocationLinks = tmclocation.getLinks();

				for (IRow row : tmcLocationLinks) {
					RdTmclocationLink link = (RdTmclocationLink) row;
					
					if(link.changedFields().containsKey("locDirect"))
					{
						int locDirect = (int) link.changedFields().get("locDirect");
						
						int tmcId = tmclocation.getTmcId();
						
						if( locDirect == 3 || locDirect == 4)
						{
							StringBuilder sb = new StringBuilder();

							sb.append("select 1 from RD_TMCLOCATION a,RD_TMCLOCATION_LINK b where a.GROUP_ID = b.GROUP_ID and b.LINK_PID =");
							sb.append(link.getLinkPid());
							sb.append(" and a.TMC_ID = ");
							sb.append(tmcId);
							sb.append(" and b.LOC_DIRECT =");
							sb.append(locDirect);
							sb.append(" group by b.LINK_PID,a.TMC_ID having count(1) >1");
							String sql = sb.toString();

							DatabaseOperator getObj = new DatabaseOperator();
							List<Object> resultList = new ArrayList<Object>();
							resultList = getObj.exeSelect(this.getConn(), sql);

							if (resultList.size() > 0) {
								this.setCheckResult("", "", 0,"同一个TMC点和同一个link组成的两组匹配信息中，“位置方向”只允许一个为P，一个为N(linkPid:"+link.getLinkPid()+")");
								return;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
	}
}
