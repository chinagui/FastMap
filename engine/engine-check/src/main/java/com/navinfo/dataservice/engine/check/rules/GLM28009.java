package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM28009
 * @author songdongyan
 * @date 2017年1月5日
 * @Description: CRFI中的Link无IMI属性且无环岛或特殊交通类型，程序报log
 * 新增CRFI:RdInter
 * 修改CRFI:RdInterLink
 */
public class GLM28009 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//RdInter新增触发
			if (obj instanceof RdInter){
				RdInter rdInter = (RdInter)obj;
				checkRdInter(rdInter);
			}
			//RdInterLink新增会触发
			else if (obj instanceof RdInterLink){
				RdInterLink rdInterLink = (RdInterLink)obj;
				checkRdInterLink(rdInterLink);
			}
		}
		
	}

	/**
	 * @param rdInterLink
	 * @throws Exception 
	 */
	private void checkRdInterLink(RdInterLink rdInterLink) throws Exception {
		if(rdInterLink.status().equals(ObjStatus.INSERT)){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND R.SPECIAL_TRAFFIC = 0 ");
				sb.append(" AND (R.IMI_CODE = 0 OR R.IMI_CODE = 3)");
				sb.append(" AND R.LINK_PID =" + rdInterLink.getLinkPid() );
				sb.append(" UNION ");
				sb.append(" SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND (R.IMI_CODE = 0 OR R.IMI_CODE = 3)");
				sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.LINK_PID = R.LINK_PID AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 33)");          
				sb.append(" AND R.LINK_PID =" + rdInterLink.getLinkPid() );
				String sql = sb.toString();
				log.info("RdInterLink前检查GLM28009:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					this.setCheckResult("", "", 0);
				}
		}
		
	}

	/**
	 * @param rdInter
	 * @throws Exception 
	 */
	private void checkRdInter(RdInter rdInter) throws Exception {
		if(rdInter.status().equals(ObjStatus.INSERT)){
			Set<Integer> linkPidSet = new HashSet<Integer>();
			for(IRow iRow:rdInter.getLinks()){
				RdInterLink rdInterLink = (RdInterLink)iRow;
				linkPidSet.add(rdInterLink.getLinkPid());
			}
			if(!linkPidSet.isEmpty()){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND R.SPECIAL_TRAFFIC = 0 ");
				sb.append(" AND (R.IMI_CODE = 0 OR R.IMI_CODE = 3)");
				sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")" );
				sb.append(" UNION ");
				sb.append(" SELECT 1 FROM RD_LINK R");
				sb.append(" WHERE R.U_RECORD <> 2");
				sb.append(" AND (R.IMI_CODE = 0 OR R.IMI_CODE = 3)");
				sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM F WHERE F.LINK_PID = R.LINK_PID AND F.U_RECORD <> 2 AND F.FORM_OF_WAY = 33)");          
				sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")" );

				String sql = sb.toString();
				log.info("RdInter前检查GLM28009:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(resultList.size()>0){
					this.setCheckResult("", "", 0);
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
