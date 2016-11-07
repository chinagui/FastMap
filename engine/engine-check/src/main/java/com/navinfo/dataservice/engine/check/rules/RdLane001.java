package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.helper.DatabaseOperator;
/**
 * 车信	html	RDLANE001	后台	
 * 如果车信进入线和退出线（退出线为环岛或者为特殊交通的除外）挂接在同一点上，而且这个点未登记路口（不属于任何路口），则不允许制作和修改
 * @author zhangxiaoyi
 *
 */
public class RdLane001 extends baseRule {

	public RdLane001() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		for(IRow obj : checkCommand.getGlmList()){
			//create的时候只有主表对象，其中包含的内容涵盖子表内容，可直接用
			if (obj instanceof RdLaneConnexity){//交限
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
			}}
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
