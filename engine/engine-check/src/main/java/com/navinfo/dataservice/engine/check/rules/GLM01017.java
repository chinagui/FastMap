package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.CheckEngine;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;




/** 
 * @ClassName: GLM01017
 * @author songdongyan
 * @date 下午3:28:00
 * @Description: 轮渡/人渡种别的Link不能作为交限的进入线、经过线或退出线。
 * Link种别编辑服务端前检查:RdLink
 */
public class GLM01017 extends baseRule{
	
	public void preCheck(CheckCommand checkCommand) throws Exception{
		for (IRow obj : checkCommand.getGlmList()) {
//			// 交限RdRestriction
//			if (obj instanceof RdRestriction) {
//				RdRestriction rdRestriction = (RdRestriction) obj;
//				checkRdRestriction(rdRestriction,checkCommand.getOperType());
//			}	
			//link种别编辑
			if (obj instanceof RdLink) {
				RdLink rdLink = (RdLink) obj;
				checkRdLink(rdLink,checkCommand.getOperType());
			}
		}
	}
	
	/**
	 * @param rdLink
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLink(RdLink rdLink, OperType operType) throws Exception {
		//link种别编辑
		if(rdLink.changedFields().containsKey("kind")){
			int kind = Integer.parseInt(rdLink.changedFields().get("kind").toString()) ;
			//非轮渡/人渡种别的Link,不触发检查
			if(kind!=11&&kind!=13){
				return;
			}
			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_RESTRICTION RS WHERE RS.IN_LINK_PID = " + rdLink.getPid());
			sb.append(" AND RS.U_RECORD <> 2");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL RD WHERE RD.OUT_LINK_PID = " + rdLink.getPid());
			sb.append(" AND RD.U_RECORD <> 2");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_RESTRICTION_VIA VIA WHERE VIA.LINK_PID = " + rdLink.getPid());
			sb.append(" AND VIA.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLink前检查GLM01017:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
		
	}

	/**
	 * @param rdRestriction
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK R WHERE R.KIND IN (11,13)");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID IN (");
		sb.append(" SELECT RS.IN_LINK_PID FROM RD_RESTRICTION RS WHERE RS.U_RECORD <> 2 AND RS.PID = " + rdRestriction.getPid());
		sb.append(" UNION");
		sb.append(" SELECT RD.OUT_LINK_PID FROM RD_RESTRICTION_DETAIL RD WHERE RD.U_RECORD <> 2 AND RD.RESTRIC_PID = " + rdRestriction.getPid());
		sb.append(" UNION");
		sb.append(" SELECT VIA.LINK_PID FROM RD_RESTRICTION_DETAIL RD,RD_RESTRICTION_VIA VIA WHERE RD.DETAIL_ID = VIA.DETAIL_ID AND RD.U_RECORD <> 2 AND VIA.U_RECORD <> 2");
		sb.append(" AND RD.RESTRIC_PID = " + rdRestriction.getPid() + ")");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM01017:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		
	}

	public void postCheck(CheckCommand checkCommand) throws Exception{

	}
	
	public static void main(String[] args) throws Exception{
		List<IRow> details = new ArrayList<IRow>();
		RdRestrictionDetail rdRestrictionDetail = new RdRestrictionDetail();
		rdRestrictionDetail.setOutLinkPid( 197951);
		rdRestrictionDetail.setPid(14076);
		rdRestrictionDetail.setRestricPid(11883);
		details.add(rdRestrictionDetail);
		
		RdRestriction rdRestriction = new RdRestriction();
		rdRestriction.setInLinkPid(197954);
		rdRestriction.setDetails(details);
		rdRestriction.setNodePid(175447);
		rdRestriction.setPid(11883);
		
		List<IRow> objList = new ArrayList<IRow>();
		objList.add(rdRestriction);
		
		//检查调用
		CheckCommand checkCommand=new CheckCommand();
		checkCommand.setGlmList(objList);
		checkCommand.setOperType(OperType.CREATE);
		checkCommand.setObjType(ObjType.RDRESTRICTION);
		CheckEngine checkEngine=new CheckEngine(checkCommand);
		System.out.println(checkEngine.preCheck());
	}

}
