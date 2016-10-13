package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneFaceSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhaokk
 * 新增行政区划点具体执行类
 */
public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	public Process(Command command,Result result,Connection conn) throws Exception {
		super(command,result,conn);
	}
	public boolean prepareData() throws Exception {
		// 获取此ZONELINK上行政取区划面拓扑关系
			List<ZoneFace> faces = new  ZoneFaceSelector(this.getConn())
								.loadZoneFaceByLinkId(this.getCommand().getLinkPid(), true);
			this.getCommand().setFaces(faces);

		return true;
	}

	
	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			//创建行政区划点有关行政区划线具体操作
			OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
			msg = operation.run(this.getResult());
			//创建行政区划点有关行政区划面具体操作类
			OpRefAdFace opRefAdFace = new OpRefAdFace(this.getCommand(),this.getConn());
			opRefAdFace.run(this.getResult());
		} catch (Exception e) {
			
			this.getConn().rollback();

			throw e;
		} 

		return msg;
	}
	@Override
	public void postCheck() throws Exception {
		check.postCheck(this.getConn(), this.getResult(),this.getCommand().getDbId());
		super.postCheck();
	}
	@Override
	public String exeOperation() throws Exception {
		//创建行政区划点有关行政区划线具体操作
		OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		//创建行政区划点有关行政区划面具体操作类
		OpRefAdFace opRefAdFace = new OpRefAdFace(this.getCommand(),this.getConn());
		opRefAdFace.run(this.getResult());
		return msg;
	}

}
