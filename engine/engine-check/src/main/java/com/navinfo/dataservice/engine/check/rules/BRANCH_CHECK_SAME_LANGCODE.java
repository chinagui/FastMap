package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
import com.navinfo.dataservice.engine.check.helper.GeoHelper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/*
 * BRANCH_CHECK_SAME_LANGCODE	同一组分歧名称中，不能存在两条语言代码相同的名称
 */


public class BRANCH_CHECK_SAME_LANGCODE extends baseRule {

	public BRANCH_CHECK_SAME_LANGCODE() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdBranch){
				RdBranch rdBranch = (RdBranch)obj;
				
				//是否存在分歧名称
				/*String sql=" SELECT N.NAME "
						+ "FROM RD_BRANCH B, RD_BRANCH_DETAIL D,RD_BRANCH_NAME N "
						+ "WHERE B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID "
						+ "AND B.BRANCH_PID="+rdBranch.getPid();
		
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> resultList=getObj.exeSelect(this.getConn(), sql);
				if (resultList==null && resultList.size()==0){
					continue;
				}*/
				
				//查找同一组分歧名称中，是否存在两条语言代码相同的名称
				String selectSql="SELECT N.NAME_GROUPID "
						+ "FROM RD_BRANCH B, RD_BRANCH_DETAIL D, RD_BRANCH_NAME N "
						+ "WHERE B.BRANCH_PID = D.BRANCH_PID AND D.DETAIL_ID = N.DETAIL_ID "
						+ "AND B.U_RECORD != 2 AND D.U_RECORD != 2 AND N.U_RECORD != 2 "
						+ "AND B.BRANCH_PID="+rdBranch.getPid()
						+ " GROUP BY N.NAME_GROUPID HAVING COUNT(N.LANG_CODE) > 1";
				DatabaseOperator getObj=new DatabaseOperator();
				List<Object> results=getObj.exeSelect(this.getConn(), selectSql);
				if (results!=null && results.size()>1){
					this.setCheckResult("", "", 0);
				}
				
				}
			}
		}
	}


