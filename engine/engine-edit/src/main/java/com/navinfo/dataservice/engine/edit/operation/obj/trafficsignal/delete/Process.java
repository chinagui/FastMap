package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 
 * @ClassName: Process
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:38:48
 * @Description: TODO
 */
public class Process extends AbstractProcess<Command> implements IProcess {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(this.getConn());

		List<RdTrafficsignal> trafficsignalList = selector.loadByTrafficPid(true, this.getCommand().getPid());

		this.getCommand().setRdTrafficsignalList(trafficsignalList);
		
		if(CollectionUtils.isNotEmpty(trafficsignalList))
		{
			RdCrossNodeSelector crossNodeSelector = new RdCrossNodeSelector(this.getConn());

			IRow crossNode = crossNodeSelector.loadByNodeId(trafficsignalList.get(0).getNodePid(), true);
			
			if(crossNode != null)
			{
				RdCrossSelector crossSelector = new RdCrossSelector(this.getConn());

				RdCross cross = (RdCross) crossSelector.loadById(crossNode.parentPKValue(), true);
				
				if(cross.getSignal() != 0)
				{
					this.getCommand().setRdCross(cross);
				}
			}
			else
			{
				throw new Exception("红绿灯点位非路口点位");
			}
		}

		return true;
	}

	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
