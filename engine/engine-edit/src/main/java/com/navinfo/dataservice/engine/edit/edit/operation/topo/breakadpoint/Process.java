package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceTopoSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

/**
 * @author zhaokk
 * 新增行政区划点具体执行类
 */
public class Process extends AbstractProcess<Command> {
	
	private Check check = new Check();
	private Boolean commitFlag = true;
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	public Process(Command command,Result result,Connection conn) throws Exception {
		super(command);
		this.setConn(conn);
		this.setResult(result);
	}
	public boolean prepareData() throws Exception {
		// 获取此ADLINK上行政取区划面拓扑关系
			List<AdFaceTopo> adFaceTopos= new AdFaceTopoSelector(this.getConn())
							.loadByLinkPid(this.getCommand().getLinkPid(), true);
			this.getCommand().setAdFaceTopos(adFaceTopos);
			List<AdFace> faces = new  AdFaceSelector(this.getConn())
								.loadAdFaceByLinkId(this.getCommand().getLinkPid(), true);
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
			this.recordData();
		} catch (Exception e) {
			
			this.getConn().rollback();

			throw e;
		} 

		return msg;
	}
	@Override
	public void postCheck() throws Exception {
		super.postCheck();
		check.postCheck(this.getConn(), this.getResult());
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
