package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM08004_1
 * @author songdongyan
 * @date 2016年12月28日
 * @Description: 公交车专用道不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线（也查线线经过线，但不查线线经过线为交叉口内link的情况）
 * 新增交限/卡车交限：RdRestriction
 * 修改交限/卡车交限：RdRestrictionDetail(新增，修改outLinkPid),RdRestrictionVia(新增，修改LinkPid)
 */
public class GLM08004_1 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			//新增交限/卡车交限
			if(obj instanceof RdRestriction ){
				RdRestriction restriObj=(RdRestriction) obj;
				if(restriObj.status().equals(ObjStatus.INSERT)){
					checkRdRestriction(restriObj);
				}
			}
			//修改交限/卡车交限
			else if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail=(RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail,checkCommand);
			}
			//修改交限/卡车交限
			else if(obj instanceof RdRestrictionVia){
				RdRestrictionVia rdRestrictionVia=(RdRestrictionVia) obj;
				checkRdRestrictionVia(rdRestrictionVia,checkCommand);
			}
		}
		
	}
	/**
	 * @param rdRestrictionVia
	 * @param checkCommand
	 * @throws Exception 
	 */
	private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia, CheckCommand checkCommand) throws Exception {
		int linkPid = 0;
		//新增的经过线
		if(rdRestrictionVia.status().equals(ObjStatus.INSERT)){
			linkPid = rdRestrictionVia.getLinkPid();
		}
		//修改linkPid的经过线
		else if(rdRestrictionVia.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionVia.changedFields().containsKey("linkPid")){
				linkPid = Integer.parseInt(rdRestrictionVia.changedFields().get("linkPid").toString());
			}
		}
		if(linkPid!=0){
			//检查经过线
			StringBuilder sb2 = new StringBuilder();
			
			sb2.append("SELECT 1 FROM RD_LINK_FORM RF WHERE RF.FORM_OF_WAY = 22 ");
			sb2.append(" AND RF.U_RECORD <> 2");
			sb2.append(" AND RF.LINK_PID =" + linkPid);
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF WHERE RLF.LINK_PID = RF.LINK_PID");
			sb2.append(" AND RLF.FORM_OF_WAY = 50");
			sb2.append(" AND RLF.U_RECORD <> 2)");

			String sql2 = sb2.toString();
			log.info("RdRestrictionVia前检查GLM08004_1:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/**
	 * @param rdRestrictionDetail
	 * @param checkCommand
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, CheckCommand checkCommand) throws Exception {
		Set<Integer> linkPids = new HashSet<Integer>();
		if(rdRestrictionDetail.status().equals(ObjStatus.INSERT)){
			int outLinkPid = rdRestrictionDetail.getOutLinkPid();
			linkPids.add(outLinkPid);
			for(IRow rdRestrictionViaObj:rdRestrictionDetail.getVias()){
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia)rdRestrictionViaObj;
				linkPids.add(rdRestrictionVia.getLinkPid());
			}
		}
		else if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
				int outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
				linkPids.add(outLinkPid);
			}
		}
		
		if(!linkPids.isEmpty()){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM RF WHERE RF.FORM_OF_WAY = 22 ");
			sb.append(" AND RF.U_RECORD <> 2");
			sb.append(" AND RF.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

			String sql = sb.toString();
			log.info("RdRestrictionDetail前检查GLM08004_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "", 0);
			}
		}
	}

	/**
	 * @param restriObj
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction restriObj) throws Exception {
		//进入线与退出线
		Set<Integer> linkPids = new HashSet<Integer>();
		//经过线
		Set<Integer> viaLinkPids = new HashSet<Integer>();
		
		linkPids.add(restriObj.getInLinkPid());
		for(IRow objTmp:restriObj.getDetails()){
			RdRestrictionDetail detailObj=(RdRestrictionDetail) objTmp;
			linkPids.add(detailObj.getOutLinkPid());
			for(IRow rdRestrictionViaObj:detailObj.getVias()){
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia)rdRestrictionViaObj;
				viaLinkPids.add(rdRestrictionVia.getLinkPid());
			}
		}
		//检查进入线/退出线
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT 1 FROM RD_LINK_FORM RF WHERE RF.FORM_OF_WAY = 22 ");
		sb.append(" AND RF.U_RECORD <> 2");
		sb.append(" AND RF.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM08004_1:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		//检查经过线
		StringBuilder sb2 = new StringBuilder();

		sb2.append("SELECT 1 FROM RD_LINK_FORM RF WHERE RF.FORM_OF_WAY = 22 ");
		sb2.append(" AND RF.U_RECORD <> 2");
		sb2.append(" AND RF.LINK_PID IN (" + StringUtils.join(viaLinkPids.toArray(),",") +")");
		sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF WHERE RLF.LINK_PID = RF.LINK_PID");
		sb2.append(" AND RLF.FORM_OF_WAY = 50");
		sb2.append(" AND RLF.U_RECORD <> 2)");

		String sql2 = sb2.toString();
		log.info("RdRestriction前检查GLM08004_1:" + sql2);

		resultList = getObj.exeSelect(this.getConn(), sql2);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdLinkForm ){
				RdLinkForm rdLinkForm=(RdLinkForm) obj;
				checkRdLinkForm(rdLinkForm);
			}
		}
	}
	/**
	 * @param rdLinkForm
	 * @throws Exception 
	 */
	private void checkRdLinkForm(RdLinkForm rdLinkForm) throws Exception {
		boolean checkFlg = false;
		if(rdLinkForm.status().equals(ObjStatus.INSERT)){
			if(rdLinkForm.getFormOfWay()==22){
				checkFlg = true;
			}
		}
		else if(rdLinkForm.status().equals(ObjStatus.UPDATE)){
			if(rdLinkForm.changedFields().containsKey("formOfWay")){
				if(rdLinkForm.getFormOfWay()==50){
					checkFlg = true;
				}
				int formOfWay = Integer.parseInt(rdLinkForm.changedFields().get("formOfWay").toString());
				if(formOfWay == 22){
					checkFlg = true;
				}
			}
		}
		
		if(checkFlg){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_RESTRICTION B,RD_LINK_FORM F");
			sb.append(" WHERE B.IN_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND B.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID = B.IN_LINK_PID");
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL D,RD_LINK_FORM F");
			sb.append(" WHERE D.OUT_LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND D.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID = D.OUT_LINK_PID");
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL D, RD_RESTRICTION_VIA RV,RD_LINK_FORM F");
			sb.append(" WHERE D.RELATIONSHIP_TYPE = 2");
			sb.append(" AND D.U_RECORD <> 2");
			sb.append(" AND RV.U_RECORD <> 2");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID = RV.LINK_PID");
			sb.append(" AND F.FORM_OF_WAY = 22");
			sb.append(" AND RV.LINK_PID = " + rdLinkForm.getLinkPid());
			sb.append(" AND D.DETAIL_ID = RV.DETAIL_ID");
			sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM LF");
			sb.append(" WHERE RV.LINK_PID = LF.LINK_PID");
			sb.append(" AND LF.U_RECORD <> 2");
			sb.append(" AND LF.FORM_OF_WAY = 50)");
			
			String sql = sb.toString();
			log.info("RdLinkForm后检查GLM08004_1:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if(resultList.size()>0){
				this.setCheckResult("", "[RD_LINK," + rdLinkForm.getLinkPid() + "]", 0);
			}
		}
		
	}

}
