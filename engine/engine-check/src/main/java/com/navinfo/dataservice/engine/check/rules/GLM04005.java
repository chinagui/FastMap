package com.navinfo.dataservice.engine.check.rules;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.gate.RdGate;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: GLM04005
 * @author songdongyan
 * @date 2016年12月5日
 * @Description: 大门的进入线到退出线不能有禁止交限（货车交限和时间段交限允许）和顺行
 * 新增交限服务端后检查：禁止交限（不包含货车交限和时间段交限）进入线=大门进入线，并且退出线、经过线=大门退出线。
 * 新增顺行服务端后检查：顺行进入线=大门进入线，并且退出线、经过线=大门退出线。
 * 修改交限： 禁止交限（不包含货车交限和时间段交限）进入线=大门进入线，并且退出线、经过线=大门退出线。
 * 新增大门服务端前检查 
 *
 */
public class GLM04005 extends baseRule{

	/**
	 * 
	 */
	public GLM04005() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 交限RdRestriction
			if (obj instanceof RdGate) {
				RdGate rdGate = (RdGate) obj;
				if(rdGate.status().equals(ObjStatus.INSERT)||rdGate.status().equals(ObjStatus.UPDATE)){
					checkRdGate(rdGate);
				}
			}
		}

	}

	/**
	 * @param rdGate
	 * @throws Exception 
	 */
	private void checkRdGate(RdGate rdGate) throws Exception {
		int inlink = rdGate.getInLinkPid();
		int outlink = rdGate.getOutLinkPid();
		int dir = rdGate.getDir();

		if (rdGate.changedFields.containsKey("dir")) {
			dir = Integer.valueOf((String) rdGate.changedFields.get("dir"));
		}
		
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT 1                                                                      ");
		sb.append("   FROM RD_DIRECTROUTE D                                                       ");
		sb.append("  WHERE D.IN_LINK_PID = " + inlink);
		sb.append("    AND D.OUT_LINK_PID = " + outlink);
		sb.append("    AND D.U_RECORD <> 2                                                        ");
		sb.append(" UNION                                                                         ");
		sb.append(" SELECT 1                                                                      ");
		sb.append("   FROM RD_DIRECTROUTE D, RD_DIRECTROUTE_VIA V                                 ");
		sb.append("  WHERE D.IN_LINK_PID = " + inlink);
		sb.append("    AND D.PID = V.PID                                                          ");
		sb.append("    AND V.LINK_PID = " + outlink);
		sb.append("    AND D.U_RECORD <> 2                                                        ");
		sb.append("    AND V.U_RECORD <> 2                                                        ");
		sb.append(" UNION                                                                         ");
		sb.append(" SELECT 1                                                                      ");
		sb.append("   FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL RD                             ");
		sb.append("  WHERE R.IN_LINK_PID = " + inlink);
		sb.append("    AND R.PID = RD.RESTRIC_PID                                                 ");
		sb.append("    AND RD.OUT_LINK_PID = " + outlink);
		sb.append("    AND R.U_RECORD <> 2                                                        ");
		sb.append("    AND RD.U_RECORD <> 2                                                       ");
		sb.append("    AND RD.TYPE = 1                                                            ");
		sb.append(" UNION                                                                         ");
		sb.append(" SELECT 1                                                                      ");
		sb.append("   FROM RD_RESTRICTION R, RD_RESTRICTION_DETAIL RD, RD_RESTRICTION_VIA RRV     ");
		sb.append("  WHERE R.IN_LINK_PID = " + inlink);
		sb.append("    AND R.PID = RD.RESTRIC_PID                                                 ");
		sb.append("    AND RRV.LINK_PID = " + outlink);
		sb.append("    AND R.U_RECORD <> 2                                                        ");
		sb.append("    AND RD.U_RECORD <> 2                                                       ");
		sb.append("    AND RD.TYPE = 1                                                            ");
		sb.append("    AND RRV.DETAIL_ID = RD.DETAIL_ID                                           ");
		sb.append("    AND RRV.U_RECORD <> 2                                                      ");
		
		if (dir == 2) {
			sb.append(" UNION SELECT 1 FROM RD_RESTRICTION R,RD_RESTRICTION_DETAIL RD       ");
			sb.append(" WHERE R.IN_LINK_PID = " + outlink);
			sb.append(" AND R.PID = RD.RESTRIC_PID                                                    ");
			sb.append(" AND RD.OUT_LINK_PID = " + inlink + " AND R.U_RECORD <> 2 AND RD.U_RECORD <> 2 ");
			sb.append(" AND RD.TYPE = 1                                                               ");
			sb.append(
					" UNION SELECT 1 FROM RD_RESTRICTION R,RD_RESTRICTION_DETAIL RD,RD_RESTRICTION_VIA RRV");
			sb.append(" WHERE R.IN_LINK_PID = " + outlink + " AND R.PID = RD.RESTRIC_PID              ");
			sb.append(" AND RRV.LINK_PID = " + inlink + " AND R.U_RECORD <> 2 AND RD.U_RECORD <> 2    ");
			sb.append(" AND RD.TYPE = 1 AND RRV.DETAIL_ID = RD.DETAIL_ID                              ");
			sb.append(" AND RRV.U_RECORD <> 2                                                         ");
		}
		
		String sql = sb.toString();
		log.info("RdGate GLM04005 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			this.setCheckResult("", "", 0);
		}
		
	}

	/**
	 * @param rdDirectroute
	 * @return
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		if(rdDirectroute.status().equals(ObjStatus.INSERT))
		{
			Set<Integer> linkPidSet = new HashSet<Integer>();
			linkPidSet.add(rdDirectroute.getOutLinkPid());

			for(Map.Entry<String, RdDirectrouteVia> entry:rdDirectroute.directrouteViaMap.entrySet()){
				linkPidSet.add(entry.getValue().getLinkPid());
			}

			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_GATE RG WHERE RG.U_RECORD !=2");
			sb.append(" AND RG.IN_LINK_PID = " + rdDirectroute.getInLinkPid());
			sb.append(" OR RG.OUT_LINK_PID IN (" + StringUtils.join(linkPidSet.toArray(),",") + ")");

			String sql = sb.toString();
			log.info("RdDirectroute GLM04005 sql:" + sql);
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]", 0);
			}
		}
	}

	/**
	 * @param rdRestriction
	 * @return
	 * @throws Exception 
	 */
	private void checkRdRestriction(RdRestriction rdRestriction) throws Exception {
		// TODO Auto-generated method stub
		//新增交限
		if(rdRestriction.status().equals(ObjStatus.INSERT))
		{
			//遍历RdRestrictionDetail，RdRestrictionVia，构造需要检查的linkPid
			Set<Integer> linkPidSet = new HashSet<Integer>();
			int inLinkPid = rdRestriction.getInLinkPid();
			
			for(IRow irow:rdRestriction.getDetails()){
				if (irow instanceof RdRestrictionDetail){
					boolean flg = false;
					RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)irow;
					//非禁止交限，不参与检查
					if(rdRestrictionDetail.getType()!=1){
						continue;
					}
					linkPidSet.add(rdRestrictionDetail.getOutLinkPid());
					
					for(IRow irowInner:rdRestrictionDetail.getVias()){
						if (irowInner instanceof RdRestrictionVia){
							RdRestrictionVia RdRestrictionVia = (RdRestrictionVia)irowInner;
							linkPidSet.add(RdRestrictionVia.getLinkPid());
						}
					}
				}
			}

			
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_GATE RG WHERE RG.U_RECORD <> 2");
			sb.append(" AND (RG.IN_LINK_PID = " + inLinkPid);
			sb.append(" OR RG.OUT_LINK_PID IN (");
			sb.append(StringUtils.join(linkPidSet.toArray(),",") + "))");

			String sql = sb.toString();
			log.info("RdRestriction GLM04005 sql:" + sql);
			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);

			if (!resultList.isEmpty()) {
				this.setCheckResult("", "[RD_RESTRICTION," + rdRestriction.getPid() + "]", 0);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		for (IRow obj : checkCommand.getGlmList()) {
			// 新增交限RdRestriction
			if (obj instanceof RdRestriction) {
				RdRestriction rdRestriction = (RdRestriction) obj;
				checkRdRestriction(rdRestriction);
			}
			//修改交限限制类型
			else if (obj instanceof RdRestrictionDetail) {
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail) obj;
				checkRdRestrictionDetail(rdRestrictionDetail);
			}
			//新增交限经过线
			else if (obj instanceof RdRestrictionVia) {
				RdRestrictionVia rdRestrictionVia = (RdRestrictionVia) obj;
				checkRdRestrictionVia(rdRestrictionVia);
			}
			// 新增顺行RdDirectroute
			else if (obj instanceof RdDirectroute) {
				RdDirectroute rdDirectroute = (RdDirectroute) obj;
				checkRdDirectroute(rdDirectroute);
			}		
		}		
	}

	/**
	 * @param rdRestrictionVia
	 * @throws Exception 
	 */
	private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia) throws Exception {
		if(rdRestrictionVia.status().equals(ObjStatus.INSERT)){
			check(rdRestrictionVia.getDetailId());
		}
		
	}



	/**
	 * @param rdRestrictionCondition
	 * @throws Exception 
	 */
	private void checkRdRestrictionCondition(RdRestrictionCondition rdRestrictionCondition) throws Exception {
		if(!rdRestrictionCondition.status().equals(ObjStatus.DELETE)){
			//时间段交限不参与检查
			String timeDomain = rdRestrictionCondition.getTimeDomain();
			if(rdRestrictionCondition.changedFields().containsKey("timeDomain")){
				timeDomain = rdRestrictionCondition.changedFields().get("timeDomain").toString();
			}
			if(timeDomain!=null){
				return;
			}
			//货车交限，不参与检查:01配送卡车,001运输卡车
			long vehicle = rdRestrictionCondition.getVehicle();
			if(rdRestrictionCondition.changedFields().containsKey("vehicle")){
				vehicle = Long.parseLong(rdRestrictionCondition.changedFields().get("vehicle").toString());
			}
			if((vehicle&6)>=2){
				return;
			}
			
			check(rdRestrictionCondition.getDetailId());
		}
		
	}

	/**
	 * @param rdRestrictionDetail
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionDetail.changedFields().containsKey("type")){
				int type = Integer.parseInt(rdRestrictionDetail.changedFields().get("type").toString());
				if(type==1){
					check(rdRestrictionDetail.getPid());
				}
			}
		}
	}
	
	/**
	 * @param detailId
	 * @throws Exception 
	 */
	private void check(int detailId) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT RR.PID FROM RD_GATE G,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD                            ");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID                                                                      ");
		sb.append(" AND G.IN_LINK_PID = RR.IN_LINK_PID                                                                  ");
		sb.append(" AND G.OUT_LINK_PID = RRD.OUT_LINK_PID                                                               ");
		sb.append(" AND RRD.TYPE = 1                                                                                    ");
		sb.append(" AND G.U_RECORD <> 2                                                                                 ");
		sb.append(" AND RR.U_RECORD <> 2                                                                                ");
		sb.append(" AND RRD.U_RECORD <> 2                                                                               ");
		sb.append(" AND RRD.DETAIL_ID = " + detailId);
		sb.append(" UNION                                                                                               ");
		sb.append(" SELECT RR.PID FROM RD_GATE G,RD_RESTRICTION RR,RD_RESTRICTION_DETAIL RRD,RD_RESTRICTION_VIA RRV     ");
		sb.append(" WHERE RR.PID = RRD.RESTRIC_PID                                                                      ");
		sb.append(" AND RRD.DETAIL_ID = RRV.DETAIL_ID                                                                   ");
		sb.append(" AND G.IN_LINK_PID = RR.IN_LINK_PID                                                                  ");
		sb.append(" AND G.OUT_LINK_PID = RRV.LINK_PID                                                                   ");
		sb.append(" AND RRD.TYPE = 1                                                                                    ");
		sb.append(" AND G.U_RECORD <> 2                                                                                 ");
		sb.append(" AND RR.U_RECORD <> 2                                                                                ");
		sb.append(" AND RRD.U_RECORD <> 2                                                                               ");
		sb.append(" AND RRV.U_RECORD <> 2                                                                               ");
		sb.append(" AND RRD.DETAIL_ID = " + detailId);
		                                                                                                       
		String sql = sb.toString();
		log.info("RdRestriction GLM04005 sql:" + sql);
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);

		if (!resultList.isEmpty()) {
			this.setCheckResult("", "[RD_RESTRICTION," + resultList.get(0) + "]", 0);
		}
		
	}
}
