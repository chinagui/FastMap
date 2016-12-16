package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM32093
 * @author songdongyan
 * @date 2016年12月13日
 * @Description: 存在连通关系的link之间不允许存在交限信息。排除：卡车交限及时间段交限
 * 新增交限:RdRestriction
 * 修改交限:RdRestriction,RdRestrictionDetail,RdRestrictionCondition
 * 新增车道联通关系:RdLaneTopoDetail
 * 修改车道联通关系:RdLaneTopoDetail,RdLaneTopoVia
 */
public class GLM32093 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增/修改交限RdRestriction
			if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction,checkCommand.getOperType());
			}	
			// 修改交限RdRestrictionDetail
			else if (obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail,checkCommand.getOperType());
			}
			// 修改交限RdRestrictionCondition
			else if (obj instanceof RdRestrictionCondition) {
				RdRestrictionCondition rdRestrictionCondition = (RdRestrictionCondition) obj;
				checkRdRestrictionCondition(rdRestrictionCondition,checkCommand.getOperType());
			}
			//新增/修改车道联通关系RdLaneTopoDetail
			else if (obj instanceof RdLaneTopoDetail) {
				RdLaneTopoDetail rdLaneTopoDetail = (RdLaneTopoDetail) obj;
				checkRdLaneTopoDetail(rdLaneTopoDetail,checkCommand.getOperType());
			}
			//修改查到联通关系RdLaneTopoVia
			else if (obj instanceof RdLaneTopoVia) {
				RdLaneTopoVia rdLaneTopoVia = (RdLaneTopoVia) obj;
				checkRdLaneTopoVia(rdLaneTopoVia,checkCommand.getOperType());
			}
		}
		
	}

	/**
	 * @param rdLaneTopoVia
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneTopoVia(RdLaneTopoVia rdLaneTopoVia, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("WITH LANE_TOPO_INFO AS");
		sb.append(" (SELECT RLTD.TOPO_ID, RLTD.IN_LINK_PID AS LINK_PID, 0 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD");
		sb.append(" WHERE RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoVia.getTopoId());
		sb.append(" UNION");
		sb.append(" SELECT RLTD.TOPO_ID, RLTD.OUT_LINK_PID AS LINK_PID, 1 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD");
		sb.append(" WHERE RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoVia.getTopoId());
		sb.append(" AND NOT EXISTS (SELECT 1");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTD.TOPO_ID = RLTV.TOPO_ID)");
		sb.append(" UNION");
		sb.append(" SELECT RLTD.TOPO_ID, RLTD.OUT_LINK_PID AS LINK_PID, T.SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD,");
		sb.append(" (SELECT RLTV.TOPO_ID, MAX(RLTV.SEQ_NUM) + 1 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTV.U_RECORD <> 2");
		sb.append(" GROUP BY RLTV.TOPO_ID) T");
		sb.append(" WHERE RLTD.TOPO_ID = T.TOPO_ID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoVia.getTopoId());
		sb.append(" UNION");
		sb.append(" SELECT RLTV.TOPO_ID, RLTV.VIA_LINK_PID AS LINK_PID, RLTV.SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTV.U_RECORD <> 2");
		sb.append(" AND RLTV.TOPO_ID="+rdLaneTopoVia.getTopoId());
		sb.append("),");
		
		sb.append(" RESTRIC_INFO AS");
		sb.append(" (SELECT RR.PID, RR.IN_LINK_PID, RRD.OUT_LINK_PID");
		sb.append(" FROM RD_RESTRICTION RR, RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND NOT EXISTS (SELECT 1");
		sb.append(" FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648)=2147483648 AND BITAND(RRC.VEHICLE, 4)=0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4))))");

		sb.append(" SELECT 1");
		sb.append(" FROM LANE_TOPO_INFO LTI1, LANE_TOPO_INFO LTI2, RESTRIC_INFO RI");
		sb.append(" WHERE LTI1.TOPO_ID = LTI2.TOPO_ID");
		sb.append(" AND LTI1.SEQ_NUM < LTI2.SEQ_NUM");
		sb.append(" AND LTI1.LINK_PID = RI.IN_LINK_PID");
		sb.append(" AND LTI2.LINK_PID = RI.OUT_LINK_PID");

		String sql = sb.toString();
		log.info("RdRestriction后检查GLM32093:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_LANE_TOPO_DETAIL," + rdLaneTopoVia.getTopoId() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdLaneTopoDetail
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdLaneTopoDetail(RdLaneTopoDetail rdLaneTopoDetail, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("WITH LANE_TOPO_INFO AS");
		sb.append(" (SELECT RLTD.TOPO_ID, RLTD.IN_LINK_PID AS LINK_PID, 0 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD");
		sb.append(" WHERE RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoDetail.getPid());
		sb.append(" UNION");
		sb.append(" SELECT RLTD.TOPO_ID, RLTD.OUT_LINK_PID AS LINK_PID, 1 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD");
		sb.append(" WHERE RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoDetail.getPid());
		sb.append(" AND NOT EXISTS (SELECT 1");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTD.TOPO_ID = RLTV.TOPO_ID)");
		sb.append(" UNION");
		sb.append(" SELECT RLTD.TOPO_ID, RLTD.OUT_LINK_PID AS LINK_PID, T.SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_DETAIL RLTD,");
		sb.append(" (SELECT RLTV.TOPO_ID, MAX(RLTV.SEQ_NUM) + 1 AS SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTV.U_RECORD <> 2");
		sb.append(" GROUP BY RLTV.TOPO_ID) T");
		sb.append(" WHERE RLTD.TOPO_ID = T.TOPO_ID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTD.TOPO_ID="+rdLaneTopoDetail.getPid());
		sb.append(" UNION");
		sb.append(" SELECT RLTV.TOPO_ID, RLTV.VIA_LINK_PID AS LINK_PID, RLTV.SEQ_NUM");
		sb.append(" FROM RD_LANE_TOPO_VIA RLTV");
		sb.append(" WHERE RLTV.U_RECORD <> 2");
		sb.append(" AND RLTV.TOPO_ID="+rdLaneTopoDetail.getPid());
		sb.append("),");
		
		sb.append(" RESTRIC_INFO AS");
		sb.append(" (SELECT RR.PID, RR.IN_LINK_PID, RRD.OUT_LINK_PID");
		sb.append(" FROM RD_RESTRICTION RR, RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND NOT EXISTS (SELECT 1");
		sb.append(" FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648)=2147483648 AND BITAND(RRC.VEHICLE, 4)=0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4))))");

		sb.append(" SELECT 1");
		sb.append(" FROM LANE_TOPO_INFO LTI1, LANE_TOPO_INFO LTI2, RESTRIC_INFO RI");
		sb.append(" WHERE LTI1.TOPO_ID = LTI2.TOPO_ID");
		sb.append(" AND LTI1.SEQ_NUM < LTI2.SEQ_NUM");
		sb.append(" AND LTI1.LINK_PID = RI.IN_LINK_PID");
		sb.append(" AND LTI2.LINK_PID = RI.OUT_LINK_PID");

		String sql = sb.toString();
		log.info("RdRestriction后检查GLM32093:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_LANE_TOPO_DETAIL," + rdLaneTopoDetail.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdRestrictionCondition
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestrictionCondition(RdRestrictionCondition rdRestrictionCondition, OperType operType) throws Exception {
		long vehicle = rdRestrictionCondition.getVehicle();
		//时间段交限不触发检查
		if(rdRestrictionCondition.getTimeDomain()==null){
			return;
		}
		//存在卡车交限不触发交限
		if(((vehicle&2147483648L)==2147483648L)&&((vehicle&4)==0)){
			return;
		}
		if(((vehicle&2147483648L)==0)&&((vehicle&4)==4)){
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT RR.PID FROM RD_LANE_TOPO_DETAIL RLTD,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RRD.RESTRIC_PID = " + rdRestrictionCondition.getDetailId());
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT RR.PID FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RRD.RESTRIC_PID = " + rdRestrictionCondition.getDetailId());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT RR.PID FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RRD.RESTRIC_PID = " + rdRestrictionCondition.getDetailId());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND RLTV.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");
		
		sb.append(" SELECT RR.PID FROM RD_LANE_TOPO_VIA RLTV2,RD_LANE_TOPO_VIA RLTV1,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RRD.RESTRIC_PID = " + rdRestrictionCondition.getDetailId());
		sb.append(" AND RLTV1.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV2.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTV1.TOPO_ID = RLTV2.TOPO_ID");
		sb.append(" AND RLTV1.SEQ_NUM < RLTV2.SEQ_NUM");
		sb.append(" AND RLTV2.U_RECORD <> 2");
		sb.append(" AND RLTV1.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdRestrictionCondition后检查GLM32093:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_RESTRICTION," + resultList.get(0) + "]";
			this.setCheckResult("", target, 0);
		}
	}

	/**
	 * @param rdRestrictionDetail
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestrictionDetail.getRestricPid());
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestrictionDetail.getRestricPid());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestrictionDetail.getRestricPid());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTV.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");
		
		sb.append(" SELECT 1 FROM RD_LANE_TOPO_VIA RLTV2,RD_LANE_TOPO_VIA RLTV1,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestrictionDetail.getRestricPid());
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTV1.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV2.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTV1.TOPO_ID = RLTV2.TOPO_ID");
		sb.append(" AND RLTV1.SEQ_NUM < RLTV2.SEQ_NUM");
		sb.append(" AND RLTV2.U_RECORD <> 2");
		sb.append(" AND RLTV1.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdRestriction后检查GLM32093:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_RESTRICTION," + rdRestrictionDetail.getRestricPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

	/**
	 * @param rdRestriction
	 * @param operType
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction, OperType operType) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestriction.getPid());
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestriction.getPid());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTD.IN_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");

		sb.append(" SELECT 1 FROM RD_LANE_TOPO_DETAIL RLTD,RD_LANE_TOPO_VIA RLTV,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestriction.getPid());
		sb.append(" AND RLTD.TOPO_ID = RLTV.TOPO_ID");
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTV.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTD.OUT_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTD.U_RECORD <> 2");
		sb.append(" AND RLTV.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");
		sb.append(" UNION ALL");
		
		sb.append(" SELECT 1 FROM RD_LANE_TOPO_VIA RLTV2,RD_LANE_TOPO_VIA RLTV1,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID");
		sb.append(" AND RR.PID = " + rdRestriction.getPid());
		sb.append(" AND NOT EXISTS (SELECT 1 FROM RD_RESTRICTION_CONDITION RRC");
		sb.append(" WHERE RRC.DETAIL_ID = RRD.DETAIL_ID");
		sb.append(" AND RRC.U_RECORD <> 2");
		sb.append(" AND (RRC.TIME_DOMAIN IS NOT NULL OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) =2147483648 AND BITAND(RRC.VEHICLE, 4) = 0) OR");
		sb.append(" (BITAND(RRC.VEHICLE, 2147483648) = 0 AND BITAND(RRC.VEHICLE, 4) = 4)))");
		sb.append(" AND RLTV1.VIA_LINK_PID = RR.IN_LINK_PID");
		sb.append(" AND RLTV2.VIA_LINK_PID = RRD.OUT_LINK_PID");
		sb.append(" AND RLTV1.TOPO_ID = RLTV2.TOPO_ID");
		sb.append(" AND RLTV1.SEQ_NUM < RLTV2.SEQ_NUM");
		sb.append(" AND RLTV2.U_RECORD <> 2");
		sb.append(" AND RLTV1.U_RECORD <> 2");
		sb.append(" AND RR.U_RECORD <> 2");
		sb.append(" AND RRD.U_RECORD <> 2");

		String sql = sb.toString();
		log.info("RdRestriction后检查GLM32093:" + sql);

		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if(resultList.size()>0){
			String target = "[RD_RESTRICTION," + rdRestriction.getPid() + "]";
			this.setCheckResult("", target, 0);
		}
		
	}

}
