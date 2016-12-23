package com.navinfo.dataservice.engine.check.rules;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.check.core.baseRule;

/** 
 * @ClassName: PERMIT_CHECK_LANE_PASSLINKS_LESS15
 * @author songdongyan
 * @date 2016年12月22日
 * @Description: 一条进入线和一条退出线之间，不能超过15条经过线
 * 新增车信服务端前检查:RdLaneConnexity
 * 修改车信服务端前检查:RdLaneTopology
 */
public class PERMIT_CHECK_LANE_PASSLINKS_LESS15 extends baseRule{

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.engine.check.core.baseRule#preCheck(com.navinfo.dataservice.dao.check.CheckCommand)
	 */
	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLaneConnexity){
				RdLaneConnexity rdLaneConnexity = (RdLaneConnexity)obj;
				checkRdLaneConnexity(rdLaneConnexity,checkCommand.getOperType());
			}
			//修改车信
			else if(obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology)obj;
				checkRdLaneTopology(rdLaneTopology,checkCommand);
			}
		}
		
	}

	/**
	 * @param rdLaneTopology
	 * @param checkCommand
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology, CheckCommand checkCommand) {
		//新增联通关系
		ObjStatus temp = rdLaneTopology.status();
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
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
				if(viaNum>15){
					this.setCheckResult("", "", 0);
				}
		}
		//修改联通关系
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			int viaNum = rdLaneTopology.getVias().size();
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
			
			if(viaNum>15){
				this.setCheckResult("", "", 0);
			}
		}
		
	}

	/**
	 * @param rdLaneConnexity
	 * @param operType
	 */
	private void checkRdLaneConnexity(RdLaneConnexity rdLaneConnexity, OperType operType) {
		Map<String, Object> changedFields=rdLaneConnexity.changedFields();
		//新增执行该检查
		if(changedFields!=null && !changedFields.isEmpty()){return;}
		for(IRow topo:rdLaneConnexity.getTopos()){
			RdLaneTopology topoObj=(RdLaneTopology) topo;
			if(topoObj.getRelationshipType()==2){
				List<IRow> vias=topoObj.getVias();
				if(vias.size()>15){
					this.setCheckResult("", "", 0);
					return;
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
