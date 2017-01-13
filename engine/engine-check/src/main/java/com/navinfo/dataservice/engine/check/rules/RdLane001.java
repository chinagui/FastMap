package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
/**
 * 车信	html	RDLANE001	后台	
 * 如果车信进入线和退出线（退出线为环岛或者为特殊交通的除外）挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * @author zhangxiaoyi
 *新增车信	服务端前检查
 *修改车信	服务端前检查
 */
public class RdLane001 extends baseRule {

	public RdLane001() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdLaneConnexity){//新增车信
				RdLaneConnexity laneObj=(RdLaneConnexity) obj;
				Map<String, Object> changedFields=laneObj.changedFields();
				//新增执行该检查
				if(changedFields!=null && changedFields.size()>0){continue;}
				List<Integer> outLinkPidList=new ArrayList<Integer>();
				for(IRow topo:laneObj.getTopos()){
					outLinkPidList.add(((RdLaneTopology) topo).getOutLinkPid());
				}
				RdLinkSelector linkSelector=new RdLinkSelector(getConn());
				List<IRow> linkObjList=linkSelector.loadByIds(outLinkPidList, false, false);
				boolean hasSameNode=false;
				int nodePid=laneObj.getNodePid();
				for(IRow linkTmp:linkObjList){
					RdLink linkObj=(RdLink) linkTmp;
					//退出线为环岛或者为特殊交通的除外
					if(linkObj.getSpecialTraffic()==1){continue;}
					List<IRow> outForms=linkSelector.loadRowsByClassParentId(RdLinkForm.class, linkObj.getPid(), false, null);
					boolean isHuandao=false;
					for(IRow form:outForms){
						if(((RdLinkForm) form).getFormOfWay()==33){
							isHuandao=true;
							break;
						}
					}
					if(isHuandao){continue;}
					if(nodePid==linkObj.getsNodePid()||nodePid==linkObj.geteNodePid()){hasSameNode=true;break;}
				}
				//进入线和退出线挂接在同一点上，而且这个点未登记路口
				if(hasSameNode && !isCrossNode(nodePid)){
					this.setCheckResult("", "", 0);
					return;
				}
			}
			//修改车信
			else if (obj instanceof RdLaneTopology){
				RdLaneTopology rdLaneTopology = (RdLaneTopology) obj;
				checkRdLaneTopology(rdLaneTopology);
			}
		}
	}
	
	/**
	 * @author Han Shaoming
	 * @param rdLaneTopology
	 * @throws Exception 
	 */
	private void checkRdLaneTopology(RdLaneTopology rdLaneTopology) throws Exception {
		// TODO Auto-generated method stub
		int outLinkPid = 0; 
		if(rdLaneTopology.status().equals(ObjStatus.INSERT)){
			outLinkPid = rdLaneTopology.getOutLinkPid();
		}
		else if(rdLaneTopology.status().equals(ObjStatus.UPDATE)){
			if(rdLaneTopology.changedFields().containsKey("outLinkPid")){
				outLinkPid = Integer.parseInt(rdLaneTopology.changedFields().get("outLinkPid").toString());
			}
		}
		
		if(outLinkPid!=0){
			StringBuilder sb = new StringBuilder();

			sb.append("SELECT DISTINCT RLC.PID FROM RD_LANE_CONNEXITY RLC,RD_LANE_TOPOLOGY RLT,RD_LINK RL,RD_LINK_FORM RLF");
			sb.append(" WHERE RL.LINK_PID = "+outLinkPid +" AND RLC.PID = "+rdLaneTopology.getConnexityPid());
			sb.append(" AND RLC.PID = RLT.CONNEXITY_PID AND RL.LINK_PID = RLF.LINK_PID");
			sb.append(" AND RL.SPECIAL_TRAFFIC <>1 AND RLF.FORM_OF_WAY <>33");
			sb.append(" AND (RLC.NODE_PID = RL.S_NODE_PID OR RLC.NODE_PID = RL.E_NODE_PID)");
			sb.append(" AND RLC.U_RECORD <>2 AND RLT.U_RECORD <>2 AND RL.U_RECORD <>2 AND RLF.U_RECORD <>2");
			sb.append(" AND NOT EXISTS(SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.NODE_PID = RLC.NODE_PID AND CN.U_RECORD <> 2)");
			String sql = sb.toString();
			log.info("修改车信前检查RDLANE001--sql:" + sql);

			DatabaseOperator getObj = new DatabaseOperator();
			List<Object> resultList = new ArrayList<Object>();
			resultList = getObj.exeSelect(this.getConn(), sql);
			if (!resultList.isEmpty()) {
				String target = "[RD_LANE_CONNEXITY," + rdLaneTopology.getConnexityPid() + "]";
				this.setCheckResult("", target, 0);
			}
		}
	}

	/**
	 * 判断点是否制作了路口
	 * @param nodePid
	 * @return 路口点 true；非路口点 false
	 * @throws Exception
	 */
	private boolean isCrossNode(int nodePid) throws Exception{
		String sql="SELECT 1 FROM RD_CROSS_NODE CN WHERE CN.U_RECORD != 2 AND CN.NODE_PID = "+nodePid;
		DatabaseOperator operator=new DatabaseOperator();
		List<Object> result=operator.exeSelect(getConn(), sql);
		if(result!=null && result.size()>0){return true;}
		return false;
	}

	
	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
