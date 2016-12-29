package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM01028_4
 * @author songdongyan
 * @date 2016年12月28日
 * @Description: 10级路/步行街/人渡不能是交限的进入线，退出线，经过线。
 * 新增交限/卡车交限：RdRestriction
 * 修改交限/卡车交限：RdRestrictionDetail(新增，修改outLinkPid),RdRestrictionVia(新增，修改LinkPid)
 */
public class GLM01028_4 extends baseRule{

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
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID =" + linkPid);
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.LINK_PID =" + linkPid);

			String sql2 = sb.toString();
			log.info("RdRestrictionVia前检查GLM01028_4:" + sql2);

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
			linkPids.add(rdRestrictionDetail.getOutLinkPid());
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

			sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
			sb.append(" AND F.U_RECORD <> 2");
			sb.append(" AND F.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");
			sb.append(" UNION");
			sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
			sb.append(" AND R.U_RECORD <> 2");
			sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

			String sql = sb.toString();
			log.info("RdRestrictionDetail前检查GLM01028_4:" + sql);

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
		//进入线与退出线与经过线
		Set<Integer> linkPids = new HashSet<Integer>();
		
		linkPids.add(restriObj.getInLinkPid());
		for(IRow objTmp:restriObj.getDetails()){
			RdRestrictionDetail detailObj=(RdRestrictionDetail) objTmp;
			linkPids.add(detailObj.getOutLinkPid());
			for(IRow rdRestrictionViaObj:detailObj.getVias()){
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia)rdRestrictionViaObj;
				linkPids.add(rdRestrictionVia.getLinkPid());
				
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LINK_FORM F WHERE F.FORM_OF_WAY = 20");
		sb.append(" AND F.U_RECORD <> 2");
		sb.append(" AND F.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");
		sb.append(" UNION");
		sb.append(" SELECT 1 FROM RD_LINK R WHERE R.KIND IN (10,11)");
		sb.append(" AND R.U_RECORD <> 2");
		sb.append(" AND R.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM01028_4:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
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
