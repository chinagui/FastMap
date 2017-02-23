package com.navinfo.dataservice.engine.check.rules;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;

/** 
 * @ClassName: RDLANE005
 * @author songdongyan
 * @date 2017年2月22日
 * @Description: RDLANE005 路口车信里不允许有经过线信息
 * 修改车信服务端后检查：RdLaneTopolyge,RdLaneVia
 */
public class RDLANE005  extends baseRule {

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//修改车信
			if(obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand);
			}
			//修改车信
			else if(obj instanceof RdLaneVia){
				RdLaneVia rdLaneVia = (RdLaneVia)obj;
				checkRdLaneVia(rdLaneVia,checkCommand);
			}
		}
		
	}

	/**
	 * @param rdLaneVia
	 * @param checkCommand 
	 * @throws Exception 
	 */
	private void checkRdLaneVia(RdLaneVia rdLaneVia, CheckCommand checkCommand) throws Exception {
		if(rdLaneVia.status().equals(ObjStatus.INSERT)){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT 1 FROM RD_LANE_TOPOLOGY T ");
			sb.append(" WHERE T.TOPOLOGY_ID = " + rdLaneVia.getTopologyId());
			sb.append(" AND T.RELATIONSHIP_TYPE = 1");
			sb.append(" AND T.U_RECORD <> 2");

			String sql = sb.toString();
			log.info("RdLaneVia前检查RdLane002:" + sql);
			
			PreparedStatement pstmt = this.getConn().prepareStatement(sql);	
			ResultSet resultSet = pstmt.executeQuery();
			
			boolean flg = false;
			if (resultSet.next()){
				flg = true;;
			} 
			resultSet.close();
			pstmt.close();
			
			if(flg){
				this.setCheckResult("", "", 0);
				return;
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param checkCommand 
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, CheckCommand checkCommand) throws Exception {
		//新增联通关系
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			if(rdLaneTopology.getRelationshipType()==1){
				int viaNum = 0;
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//排除删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							continue;
						}
						if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
							viaNum ++;
						}
					}
				}
				if(viaNum!=0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int relationshipType = 1;
			if(rdLaneTopology.changedFields().containsKey("relationshipType")){
				relationshipType = Integer.parseInt(rdLaneTopology.changedFields().get("relationshipType").toString());
			}
			int viaNum = getViaNum(rdLaneTopology.getPid());
			if(relationshipType==1){
				for(IRow objInnerLoop : checkCommand.getGlmList()){
					if(objInnerLoop instanceof RdLaneVia){
						RdLaneVia rdLaneVia = (RdLaneVia)objInnerLoop;
						//删除的经过线
						if(rdLaneVia.status().equals(ObjStatus.DELETE)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								viaNum --;
							}
							continue;
						}
						//新增的经过线
						if(rdLaneVia.status().equals(ObjStatus.INSERT)){
							if(rdLaneVia.getTopologyId()==rdLaneTopology.getPid()){
								viaNum ++;
							}
							continue;
						}
					}
				}
				
				if(viaNum!=0){
					this.setCheckResult("", "", 0);
					return;
				}
			}
		}
	}
	
	/**
	 * @param pid
	 * @return
	 * @throws Exception 
	 */
	private int getViaNum(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT COUNT(1) FROM RD_LANE_TOPOLOGY T ,RD_LANE_VIA V");
		sb.append(" WHERE T.TOPOLOGY_ID = V.TOPOLOGY_ID");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND V.U_RECORD <> 2");
		sb.append(" AND T.TOPOLOGY_ID = " + pid);

		String sql = sb.toString();
		log.info("前检查RdLane005:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
		List<Object> resultList = new ArrayList<Object>();
		resultList = getObj.exeSelect(this.getConn(), sql);
		
		if(resultList!=null && resultList.size()>0){
			return Integer.parseInt(resultList.get(0).toString());
		}
		return 0;
	}

	/**
	 * @param pid
	 * @throws Exception 
	 */
	private void check(int pid) throws Exception {
		StringBuilder sb = new StringBuilder();

		sb.append("SELECT T.CONNEXITY_PID FROM RD_LANE_TOPOLOGY T ,RD_LANE_VIA V");
		sb.append(" WHERE T.RELATIONSHIP_TYPE = 1");
		sb.append(" AND T.TOPOLOGY_ID = V.TOPOLOGY_ID");
		sb.append(" AND T.U_RECORD <> 2");
		sb.append(" AND V.U_RECORD <> 2");
		sb.append(" AND T.TOPOLOGY_ID = " + pid);

		String sql = sb.toString();
		log.info("后检查RdLane005:" + sql);
		
		DatabaseOperator getObj = new DatabaseOperator();
        List<Object> resultList = getObj.exeSelect(this.getConn(), sql);
        if (resultList != null && resultList.size() > 0) {
            this.setCheckResult("", "[RD_LANE_CONNEXITY," + resultList.get(0) + "]", 0);
        }
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#postCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {

		
	}

}
