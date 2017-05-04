package com.navinfo.dataservice.engine.check.rules;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: GLM08004_4
 * @author songdongyan
 * @date 2017年4月28日
 * @Description: GLM08004_4.java
 */
public class GLM08004_4 extends baseRule {

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
				if(!rdRestrictionDetail.status().equals(ObjStatus.DELETE)){
					checkRdRestrictionDetail(rdRestrictionDetail,checkCommand);
				}
			}
			//修改交限/卡车交限
			else if(obj instanceof RdRestrictionCondition){
				RdRestrictionCondition rdRestrictionCondition=(RdRestrictionCondition) obj;
				if(!rdRestrictionCondition.status().equals(ObjStatus.DELETE)){
					checkRdRestrictionCondition(rdRestrictionCondition,checkCommand);
				}
			}
			//修改交限/卡车交限
			else if(obj instanceof RdRestrictionVia){
				RdRestrictionVia rdRestrictionVia=(RdRestrictionVia) obj;
				if(!rdRestrictionVia.status().equals(ObjStatus.DELETE)){
					checkRdRestrictionVia(rdRestrictionVia,checkCommand);
				}
			}
		}
	}
	
	/**
	 * @param restriObj
	 */
	private void checkRdRestriction(RdRestriction restriObj) {
		List<IRow> details=restriObj.getDetails();
		Set<Integer> linkPidList = new HashSet<Integer>();
		for(IRow objTmp:details){
			RdRestrictionDetail detailObj=(RdRestrictionDetail) objTmp;
			if(detailObj.getRelationshipType()==2){
				List<IRow> conditionObjs = detailObj.getConditions();
				for(IRow conditionObj:conditionObjs){
					RdRestrictionCondition rdRestrictionCondition = (RdRestrictionCondition)conditionObj;
					if(rdRestrictionCondition.getTimeDomain()!=null){
						List<IRow> vias=detailObj.getVias();
						for(IRow via:vias){
							RdRestrictionVia rdRestrictionVia = (RdRestrictionVia)via;
							linkPidList.add(rdRestrictionVia.getLinkPid());
						}
						linkPidList.add(detailObj.getOutLinkPid());
						linkPidList.add(restriObj.getInLinkPid());
						checkRdLink(linkPidList,rdRestrictionCondition.getTimeDomain());
					}
				}
			}
		}
	}
	
	/**
	 * @param rdRestrictionDetail
	 * @param checkCommand 
	 * @throws SQLException 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail, CheckCommand checkCommand) throws SQLException {
		Set<Integer> linkPidList = new HashSet<Integer>();
		int type = rdRestrictionDetail.getType();
		int outLinkPid = rdRestrictionDetail.getOutLinkPid();
		if(rdRestrictionDetail.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionDetail.changedFields().containsKey("type")){
				type = Integer.parseInt(rdRestrictionDetail.changedFields().get("type").toString());
			}
			if(rdRestrictionDetail.changedFields().containsKey("outLinkPid")){
				outLinkPid = Integer.parseInt(rdRestrictionDetail.changedFields().get("outLinkPid").toString());
			}
		}
		
		//如果新增或者修改RD_RESTRICTION_DETAIL,则RD_RESTRICTION_CONDITION也会修改或新增。但经过线可能不会修改
		if(type==2){
			//获取经过线
			Set<Integer> viaLinkSet = getViaLinkListByRdRestrictionDetailId(rdRestrictionDetail.getPid());
			String timeDomain = null;
			for(IRow obj:checkCommand.getGlmList()){
				if(obj instanceof RdRestrictionCondition){
					RdRestrictionCondition rdRestrictionCondition = (RdRestrictionCondition)obj;
					if(rdRestrictionCondition.getDetailId() == rdRestrictionDetail.getPid()){
						if(!rdRestrictionCondition.status().equals(ObjStatus.DELETE)){
							timeDomain = rdRestrictionCondition.getTimeDomain();
							if(rdRestrictionCondition.changedFields().containsKey("timeDomain")){
								timeDomain = rdRestrictionCondition.changedFields().get("timeDomain").toString();
							}
						}
					}
				}
				else if(obj instanceof RdRestrictionVia){
					RdRestrictionVia via = (RdRestrictionVia)obj;
					if(via.getDetailId() == rdRestrictionDetail.getPid()){
						if(via.status().equals(ObjStatus.DELETE)){
							if(viaLinkSet.contains(via.getLinkPid())){
								viaLinkSet.remove(via.getLinkPid());
							}
						}else{
							viaLinkSet.add(via.getLinkPid());
						}
					}
				}
			}
			
			linkPidList.add(outLinkPid);
			linkPidList.addAll(viaLinkSet);
			if(timeDomain!=null){
				checkRdLink(linkPidList,timeDomain);
			}
		}
	}


	/**
	 * @param rdRestrictionCondition
	 * @param checkCommand 
	 * @throws SQLException 
	 */
	private void checkRdRestrictionCondition(RdRestrictionCondition rdRestrictionCondition, CheckCommand checkCommand) throws SQLException {
		Set<Integer> linkPidList = new HashSet<Integer>();
		String timeDomain = rdRestrictionCondition.getTimeDomain();
		if(rdRestrictionCondition.status().equals(ObjStatus.UPDATE)){
			if(rdRestrictionCondition.changedFields().containsKey("timeDomain")){
				timeDomain = rdRestrictionCondition.changedFields().get("timeDomain").toString();
			}
		}

		//获取经过线
		Set<Integer> viaLinkSet = getViaLinkListByRdRestrictionDetailId(rdRestrictionCondition.getDetailId());

		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)obj;
				//如果关联rdRestrictionDetail为新增或修改，此种情况，checkRdRestrictionDetail已经处理，此处不再做处理
				if(rdRestrictionCondition.getDetailId() == rdRestrictionDetail.getPid()){
					return;
				}
			}
			else if(obj instanceof RdRestrictionVia){
				RdRestrictionVia via = (RdRestrictionVia)obj;
				if(via.getDetailId() == rdRestrictionCondition.getDetailId()){
					if(via.status().equals(ObjStatus.DELETE)){
						if(viaLinkSet.contains(via.getLinkPid())){
							viaLinkSet.remove(via.getLinkPid());
						}
					}else{
						viaLinkSet.add(via.getLinkPid());
					}
				}
			}
		}
		
		Map<Integer,Integer> detailMap = getRdRestrictionDetailByDetailId(rdRestrictionCondition.getDetailId());
		linkPidList.addAll(detailMap.values());
		linkPidList.addAll(viaLinkSet);
		if(timeDomain!=null){
			checkRdLink(linkPidList,timeDomain);
		}

	}

	/**
	 * @param rdRestrictionVia
	 * @param checkCommand 
	 * @throws SQLException 
	 */
	private void checkRdRestrictionVia(RdRestrictionVia rdRestrictionVia, CheckCommand checkCommand) throws SQLException {
		for(IRow obj:checkCommand.getGlmList()){
			if(obj instanceof RdRestrictionDetail){
				RdRestrictionDetail rdRestrictionDetail = (RdRestrictionDetail)obj;
				//如果关联rdRestrictionDetail为新增或修改，此种情况，checkRdRestrictionDetail已经处理，此处不再做处理
				if(rdRestrictionVia.getDetailId() == rdRestrictionDetail.getPid()){
					return;
				}
			}
			else if(obj instanceof RdRestrictionCondition){
				RdRestrictionCondition rdRestrictionCondition = (RdRestrictionCondition)obj;
				//如果关联RdRestrictionCondition为新增或修改，此种情况，checkRdRestrictionCondition已经处理，此处不再做处理
				if(rdRestrictionVia.getDetailId() == rdRestrictionCondition.getDetailId()){
					return;
				}
			}
		}
		
		Map<Integer,String> detailMap = getRdRestrictionDetailAndConditionByDetailId(rdRestrictionVia.getDetailId());
		
		for(Map.Entry<Integer, String> entry:detailMap.entrySet()){
			Set<Integer> linkPidList = new HashSet<Integer>();

			linkPidList.add(entry.getKey());
			linkPidList.add(rdRestrictionVia.getLinkPid());
			checkRdLink(linkPidList,entry.getValue());
		}

	}

	
	/**
	 * @param detailId
	 * @return
	 * @throws SQLException 
	 */
	private Map<Integer, String> getRdRestrictionDetailAndConditionByDetailId(int detailId) throws SQLException {
		QueryRunner run = new QueryRunner();

		StringBuilder sb2 = new StringBuilder();

		sb2.append(" SELECT D.OUT_LINK_PID ,C.TIME_DOMAIN                            ");
		sb2.append("   FROM RD_RESTRICTION_DETAIL D, RD_RESTRICTION_CONDITION C      ");
		sb2.append("  WHERE D.DETAIL_ID = " + detailId);
		sb2.append("    AND D.U_RECORD <> 2                                          ");
		sb2.append("    AND D.TYPE = 2                                               ");
		sb2.append("    AND D.DETAIL_ID = C.DETAIL_ID                                ");
		sb2.append("    AND C.TIME_DOMAIN IS NOT NULL                                ");
		
		String sql2 = sb2.toString();
		log.info("getRdRestrictionDetailAndConditionByDetailId SQL :" + sql2);
		
		
		ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {
			public Map<Integer, String> handle(ResultSet rs) throws SQLException {
				Map<Integer, String> result = new HashMap<Integer, String>();
				while(rs.next()) {
					result.put(rs.getInt("DETAIL_ID"), rs.getString("TIME_DOMAIN"));
				}
				return result;
			}
		};
		Map<Integer, String> result =  run.query(this.getConn(), sql2,rsHandler);
		return result;
	}

	/**
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	private Set<Integer> getViaLinkListByRdRestrictionDetailId(int pid) throws SQLException {
		QueryRunner run = new QueryRunner();

		StringBuilder sb2 = new StringBuilder();

		sb2.append(" SELECT V.LINK_PID                ");
		sb2.append("   FROM RD_RESTRICTION_VIA V      ");
		sb2.append("  WHERE V.DETAIL_ID = " + pid);
		sb2.append("    AND V.U_RECORD <> 2           ");
		
		String sql2 = sb2.toString();
		log.info("getViaLinkListByRdRestrictionDetailId SQL :" + sql2);
		
		
		ResultSetHandler<Set<Integer>> rsHandler = new ResultSetHandler<Set<Integer>>() {
			public Set<Integer> handle(ResultSet rs) throws SQLException {
				Set<Integer> result = new HashSet<Integer>();
				while(rs.next()) {
					result.add(rs.getInt("LINK_PID"));
				}
				return result;
			}
		};
		Set<Integer> result =  run.query(this.getConn(), sql2,rsHandler);
		return result;
	}
	
	/**
	 * @param detailId
	 * @return
	 * @throws SQLException 
	 */
	private Map<Integer, Integer> getRdRestrictionDetailByDetailId(int detailId) throws SQLException {
		QueryRunner run = new QueryRunner();

		StringBuilder sb2 = new StringBuilder();

		sb2.append(" SELECT D.DETAIL_ID,D.OUT_LINK_PID  ");
		sb2.append("   FROM RD_RESTRICTION_DETAIL D     ");
		sb2.append("  WHERE D.DETAIL_ID = " + detailId);
		sb2.append("    AND D.U_RECORD <> 2             ");
		sb2.append("    AND D.TYPE = 2                  ");
		
		String sql2 = sb2.toString();
		log.info("getRdRestrictionDetailByDetailId SQL :" + sql2);
		
		
		ResultSetHandler<Map<Integer, Integer>> rsHandler = new ResultSetHandler<Map<Integer, Integer>>() {
			public Map<Integer, Integer> handle(ResultSet rs) throws SQLException {
				Map<Integer, Integer> result = new HashMap<Integer, Integer>();
				while(rs.next()) {
					result.put(rs.getInt("DETAIL_ID"), rs.getInt("OUT_LINK_PID"));
				}
				return result;
			}
		};
		Map<Integer, Integer> result =  run.query(this.getConn(), sql2,rsHandler);
		return result;
	}



	/**
	 * @param linkPidList
	 * @param timeDomain
	 * @throws SQLException 
	 */
	private void checkRdLink(Set<Integer> linkPidList, String timeDomain) throws SQLException {
		if(linkPidList.size()==0){
			return;
		}
		QueryRunner run = new QueryRunner();

		StringBuilder sb2 = new StringBuilder();

		sb2.append(" SELECT LIM.LINK_PID,LIM.TIME_DOMAIN        ");
		sb2.append("   FROM RD_LINK_LIMIT LIM                   ");
		sb2.append("  WHERE LIM.LINK_PID IN (" + StringUtils.join(linkPidList.toArray(),",") + ")");
		sb2.append("    AND LIM.TIME_DOMAIN IS NOT NULL         ");
		sb2.append("    AND LIM.VEHICLE = 2147483786            ");
		sb2.append("    AND LIM.U_RECORD <> 2                   ");
		sb2.append("    AND NOT EXISTS (SELECT 1                ");
		sb2.append("           FROM RD_LINK_FORM F              ");
		sb2.append("          WHERE F.LINK_PID = LIM.LINK_PID   ");
		sb2.append("            AND F.FORM_OF_WAY = 50)         ");
		
		String sql2 = sb2.toString();
		log.info("GLM08004_4 checkRdLink SQL :" + sql2);
		
		
		ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {
			public Map<Integer, String> handle(ResultSet rs) throws SQLException {
				Map<Integer, String> result = new HashMap<Integer, String>();
				while(rs.next()) {
					result.put(rs.getInt("DETAIL_ID"), rs.getString("TIME_DOMAIN"));
				}
				return result;
			}
		};
		Map<Integer, String> result =  run.query(this.getConn(), sql2,rsHandler);
		
		
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
