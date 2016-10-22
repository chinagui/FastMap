package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

//GLM01015	两条或者多条不同的Link具有相同的两个端点	闭合环未打断	
//RD_LINK、RW_LINK、AD_LINK、ZONE_LINK、LC_LINK、LU_LINK、ADAS_LINK	新增link

public class GLM01015 extends baseRule {

	public GLM01015() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;				
				StringBuilder sb = new StringBuilder();
		        sb.append("select a.link_pid from rd_link a where A.U_RECORD != 2 AND a.link_pid = ");
		        sb.append(rdLink.getPid());
		        sb.append(" and  exists (select 1 from rd_link b "
		        		+ "where a.link_pid != b.link_pid AND B.U_RECORD != 2"
		        		+ "and a.s_node_pid in (b.s_node_pid,b.e_node_pid) "
		        		+ "and a.e_node_pid in (b.s_node_pid,b.e_node_pid))");
				String sql = sb.toString();
				
		        DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=new ArrayList<Object>();
				resultList=getObj.exeSelect(this.getConn(), sql);
				
				if (resultList.size()>0){
					this.setCheckResult(rdLink.getGeometry(), "[RD_LINK,"+rdLink.getPid()+"]", rdLink.getMeshId());
					}
				}
			}
		}
	}

