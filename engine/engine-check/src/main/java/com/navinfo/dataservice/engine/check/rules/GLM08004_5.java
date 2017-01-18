package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkLimit;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM08004_5
 * @author songdongyan
 * @date 2016年12月28日
 * @Description: 出租车专用道（加了永久穿行限制，并且车辆类型同时且只制作了允许出租车、急救车和行人的link）不能作为交限（包括路口和线线结构里的所有交限）的进入线或者退出线（也查线线经过线，但不查线线经过线为交叉口内link的情况）
 * 新增交限/卡车交限：RdRestriction
 * 修改交限/卡车交限：RdRestrictionDetail(新增，修改outLinkPid),RdRestrictionVia(新增，修改LinkPid)
 */
public class GLM08004_5 extends baseRule {

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

			sb2.append("SELECT 1 FROM RD_LINK_LIMIT RLL WHERE RLL.TYPE = 2 ");
			sb2.append(" AND RLL.VEHICLE = 2147484040");
			sb2.append(" AND RLL.TIME_DOMAIN IS NULL");
			sb2.append(" AND RLL.U_RECORD <> 2");
			sb2.append(" AND RLL.LINK_PID =" + linkPid);
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF WHERE RLF.LINK_PID = RLL.LINK_PID");
			sb2.append(" AND RLF.FORM_OF_WAY = 50");
			sb2.append(" AND RLF.U_RECORD <> 2)");

			String sql2 = sb2.toString();
			log.info("RdRestrictionVia前检查GLM08004_5:" + sql2);

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

			sb.append("SELECT 1 FROM RD_LINK_LIMIT RLL WHERE RLL.TYPE = 2 ");
			sb.append(" AND RLL.VEHICLE = 2147484040");
			sb.append(" AND RLL.TIME_DOMAIN IS NULL");
			sb.append(" AND RLL.U_RECORD <> 2");
			sb.append(" AND RLL.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

			String sql = sb.toString();
			log.info("RdRestrictionDetail前检查GLM08004_5:" + sql);

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

		sb.append("SELECT 1 FROM RD_LINK_LIMIT RLL WHERE RLL.TYPE = 2 ");
		sb.append(" AND RLL.VEHICLE = 2147484040");
		sb.append(" AND RLL.TIME_DOMAIN IS NULL");
		sb.append(" AND RLL.U_RECORD <> 2");
		sb.append(" AND RLL.LINK_PID IN (" + StringUtils.join(linkPids.toArray(),",") +")");

		String sql = sb.toString();
		log.info("RdRestriction前检查GLM08004_5:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			this.setCheckResult("", "", 0);
		}
		//检查经过线
		StringBuilder sb2 = new StringBuilder();

		sb2.append("SELECT 1 FROM RD_LINK_LIMIT RLL WHERE RLL.TYPE = 2 ");
		sb2.append(" AND RLL.VEHICLE = 2147484040");
		sb2.append(" AND RLL.TIME_DOMAIN IS NULL");
		sb2.append(" AND RLL.U_RECORD <> 2");
		sb2.append(" AND RLL.LINK_PID IN (" + StringUtils.join(viaLinkPids.toArray(),",") +")");
		sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF WHERE RLF.LINK_PID = RLL.LINK_PID");
		sb2.append(" AND RLF.FORM_OF_WAY = 50");
		sb2.append(" AND RLF.U_RECORD <> 2)");

		String sql2 = sb2.toString();
		log.info("RdRestriction前检查GLM08004_5:" + sql2);

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
			if(obj instanceof RdLinkLimit ){
				RdLinkLimit rdLinkLimit=(RdLinkLimit) obj;
				checkRdLinkLimit(rdLinkLimit);
			}
		}
		
	}
	
	/**
	 * @param rdLinkLimit
	 * @throws Exception 
	 */
	private void checkRdLinkLimit(RdLinkLimit rdLinkLimit) throws Exception {
		boolean checkFlg = false;
		//进不去出不来的link
		if(rdLinkLimit.status().equals(ObjStatus.INSERT)){
			if((rdLinkLimit.getType()==2)&&(rdLinkLimit.getVehicle()==2147484040L)&&(rdLinkLimit.getTimeDomain()==null)){
				checkFlg = true;
			}
		}
		else if(rdLinkLimit.status().equals(ObjStatus.UPDATE)){
			int type;
			long vehicle;
			String timeDomain;
			if(rdLinkLimit.changedFields().containsKey("type")){
				type = Integer.parseInt(rdLinkLimit.changedFields().get("type").toString());
			}else{
				type = rdLinkLimit.getType();
			}
			
			if(rdLinkLimit.changedFields().containsKey("vehicle")){
				vehicle = Long.parseLong(rdLinkLimit.changedFields().get("vehicle").toString());
			}else{
				vehicle = rdLinkLimit.getVehicle();
			}
			
			if(rdLinkLimit.changedFields().containsKey("timeDomain")){
				if(rdLinkLimit.changedFields().get("timeDomain")!=null){
					timeDomain = rdLinkLimit.changedFields().get("timeDomain").toString();
				}
				else{
					timeDomain = null;
				}
			}else{
				timeDomain = null;
			}
			
			if((type==2)&&(vehicle==2147484040L)&&(timeDomain==null)){
				checkFlg = true;
			}
		}
		
		if(checkFlg){
			StringBuilder sb2 = new StringBuilder();

			sb2.append("SELECT 1 FROM RD_RESTRICTION RR");
			sb2.append(" WHERE RR.IN_LINK_PID = " + rdLinkLimit.getLinkPid());
			sb2.append(" AND RR.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_RESTRICTION_DETAIL RRD");
			sb2.append(" WHERE RRD.OUT_LINK_PID = " + rdLinkLimit.getLinkPid());
			sb2.append(" AND RRD.U_RECORD <> 2");
			sb2.append(" UNION");
			sb2.append(" SELECT 1 FROM RD_RESTRICTION_VIA RRV");
			sb2.append(" WHERE RRV.LINK_PID = " + rdLinkLimit.getLinkPid());
			sb2.append(" AND NOT EXISTS (SELECT 1 FROM RD_LINK_FORM RLF");
			sb2.append(" WHERE RLF.LINK_PID = RRV.LINK_PID");
			sb2.append(" AND RLF.FORM_OF_WAY = 50");
			sb2.append(" AND RLF.U_RECORD <> 2)");
			sb2.append(" AND RRV.U_RECORD <> 2");

			String sql2 = sb2.toString();
			log.info("RdLinkLimit后检查GLM08004_5:" + sql2);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql2);

			if(resultList.size()>0){
				String target = "[RD_LINK," + rdLinkLimit.getLinkPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
		
	}


}
