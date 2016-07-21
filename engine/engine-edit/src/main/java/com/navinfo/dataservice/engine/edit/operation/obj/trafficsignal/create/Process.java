package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	private Check check = new Check();

	@Override
	public String preCheck() throws Exception {
		super.preCheck();
		return null;
	}

	@Override
	public boolean prepareData() throws Exception {

		int nodePid = this.getCommand().getNodePid();

		RdCrossNodeSelector selector = new RdCrossNodeSelector(this.getConn());

		IRow crossNode = selector.loadByNodeId(nodePid, true);

		RdCrossSelector crossSelector = new RdCrossSelector(this.getConn());

		RdCross cross = (RdCross) crossSelector.loadById(crossNode.parentPKValue(), true);

		check.checkHasTrafficSignal(cross);

		this.getCommand().setCross(cross);

		Map<Integer,List<Integer>> nodeLinkPidMap = new HashMap<>();
		
		//复合路口和简单路口统一处理
		List<IRow> nodes = cross.getNodes();

		RdLinkSelector linkSelector = new RdLinkSelector(this.getConn());

		for (IRow row : nodes) {
			List<RdLink> links = linkSelector.loadInLinkByNodePid(((RdCrossNode) row).getNodePid(), 50, true);
			
			List<Integer> linkPidList = new ArrayList<>();
			
			for(RdLink link : links)
			{
				linkPidList.add(link.getPid());
			}
			
			nodeLinkPidMap.put(((RdCrossNode) row).getNodePid(), linkPidList);
		}

		this.getCommand().setNodeLinkPidMap(nodeLinkPidMap);

		return false;
	}

	@Override
	public void postCheck() throws Exception {
		super.postCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		Operation operation = new Operation(this.getCommand());
		String msg = operation.run(this.getResult());
		return msg;
	}

}
