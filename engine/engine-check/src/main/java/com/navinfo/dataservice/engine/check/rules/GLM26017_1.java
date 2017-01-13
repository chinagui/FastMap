package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/**
 * @ClassName GLM26017_1
 * @author Han Shaoming
 * @date 2016年12月23日 下午4:43:34
 * @Description TODO
 * 关系类型为“路口”的车信、交限、顺行、路口语音引导关系信息，应制作到登记了路口的点上
 */
public class GLM26017_1 extends baseRule {
	protected Logger log = Logger.getLogger(this.getClass());
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//关系类型编辑（语音引导详细信息表）
			if(row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail)row;
				checkRdVoiceguideDetail(rdVoiceguideDetail);
			}
			//关系类型编辑（顺行表）
			else if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			}
		}
	}

	

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		for(IRow row:checkCommand.getGlmList()){
			//车信关系类型编辑
			if (row instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology) row;
				checkRdLaneTopology(rdLaneTopology);
			}
			//关系类型编辑（语音引导详细信息表）
			else if(row instanceof RdVoiceguideDetail){
				RdVoiceguideDetail rdVoiceguideDetail = (RdVoiceguideDetail)row;
				checkRdVoiceguideDetail(rdVoiceguideDetail);
			}
			//关系类型编辑（顺行表）
			else if(row instanceof RdDirectroute){
				RdDirectroute rdDirectroute = (RdDirectroute)row;
				checkRdDirectroute(rdDirectroute);
			} 
			//交限关系类型
			else if(row instanceof RdRestrictionDetail){
				RdRestrictionDetail rdDirectroute = (RdRestrictionDetail)row;
				checkRdRestrictionDetail(rdDirectroute);
			} 
		}
	}
	
	/**
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdRestrictionDetail(RdRestrictionDetail rdRestrictionDetail) throws Exception {
		if(rdRestrictionDetail.changedFields() != null && rdRestrictionDetail.changedFields().containsKey("relationshipType")){
			int relationshipType = Integer.parseInt(rdRestrictionDetail.changedFields().get("relationshipType").toString());
			if(relationshipType==1){
				StringBuilder sb = new StringBuilder();

				sb.append("SELECT 1 FROM RD_RESTRICTION RR");
				sb.append(" WHERE RR.U_RECORD <> 2");
				sb.append(" AND RR.PID = "+rdRestrictionDetail.getRestricPid());
				sb.append(" AND AND NOT EXISTS (SELECT 1 FROM RD_CROSS_NODE RCN WHERE RCN.NODE_PID = RR.NODE_PID AND RCN.U_RECORD <> 2)");
				
				String sql = sb.toString();
				log.info("RdRestrictionDetail检查GLM26017_1--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_RESTRICTION," + rdRestrictionDetail.getRestricPid() + "]";
					this.setCheckResult("", target, 0,"关系类型为“路口”的交限，应制作到登记了路口的点上");
				}
			}
		}
		
	}



	/**
	 * @author Han Shaoming
	 * @param rdVoiceguideDetail
	 * @throws Exception 
	 */
	private void checkRdVoiceguideDetail(RdVoiceguideDetail rdVoiceguideDetail) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdVoiceguideDetail.changedFields();
		if(changedFields != null && changedFields.containsKey("relationshipType")){
			int relationshipType = Integer.parseInt((String) changedFields.get("relationshipType"));
			if(relationshipType == 1){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT V.PID FROM RD_VOICEGUIDE V, RD_VOICEGUIDE_DETAIL VD");
				sb.append(" WHERE V.PID = VD.VOICEGUIDE_PID AND V.U_RECORD <> 2");
				sb.append(" AND VD.U_RECORD <> 2 AND V.PID = "+rdVoiceguideDetail.getVoiceguidePid()+" AND NOT EXISTS");
				sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = V.NODE_PID AND CN.U_RECORD <> 2)");
				
				String sql = sb.toString();
				log.info("RdVoiceguide检查GLM26017_1--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_VOICEGUIDE," + resultList.get(0).toString() + "]";
					this.setCheckResult("", target, 0,"关系类型为“路口”的语音引导关系信息，应制作到登记了路口的点上");
				}
			}
		}
	}

	/**
	 * @author Han Shaoming
	 * @param rdDirectroute
	 * @throws Exception 
	 */
	private void checkRdDirectroute(RdDirectroute rdDirectroute) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdDirectroute.changedFields();
		if(changedFields != null && changedFields.containsKey("relationshipType")){
			int relationshipType = (int) changedFields.get("relationshipType");
			if(relationshipType == 1){
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT D.PID FROM RD_DIRECTROUTE D");
				sb.append(" WHERE D.U_RECORD <> 2");
				sb.append(" AND D.PID="+rdDirectroute.getPid()+" AND NOT EXISTS");
				sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = D.NODE_PID AND CN.U_RECORD <> 2)");
				
				String sql = sb.toString();
				log.info("RdDirectroute检查GLM26017_1--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_DIRECTROUTE," + rdDirectroute.getPid() + "]";
					this.setCheckResult("", target, 0,"关系类型为“路口”的顺行关系信息，应制作到登记了路口的点上");
				}
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLaneConnexity
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology) throws Exception {
		// TODO Auto-generated method stub
		Map<String, Object> changedFields = rdLaneTopology.changedFields();
		if(changedFields != null && changedFields.containsKey("relationshipType")){
			int relationshipType = Integer.parseInt((String) changedFields.get("relationshipType"));
			if(relationshipType == 1){
				//修改车信,触发检查
				StringBuilder sb = new StringBuilder();
				
				sb.append("SELECT C.PID FROM RD_LANE_CONNEXITY C, RD_LANE_TOPOLOGY T");
				sb.append(" WHERE C.PID = T.CONNEXITY_PID AND C.U_RECORD <>2");
				sb.append(" AND T.U_RECORD <>2 AND T.TOPOLOGY_ID="+rdLaneTopology.getPid());
				sb.append(" AND NOT EXISTS");
				sb.append(" (SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = C.NODE_PID AND CN.U_RECORD <> 2)");
				
				String sql = sb.toString();
				log.info("RdLaneConnexity后检查GLM26017_1--sql:" + sql);
				
				DatabaseOperator getObj = new DatabaseOperator();
				List<Object> resultList = new ArrayList<Object>();
				resultList = getObj.exeSelect(this.getConn(), sql);
				
				if(!resultList.isEmpty()){
					String target = "[RD_LANE_CONNEXITY," + resultList.get(0).toString() + "]";
					this.setCheckResult("", target, 0,"关系类型为“路口”的车信关系信息，应制作到登记了路口的点上");
				}
			}
		}
	}

}
