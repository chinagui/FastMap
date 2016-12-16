package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhaokk 新增行政区划点具体执行类
 */
public class Process extends AbstractProcess<Command> {

	private Check check = new Check();
	private Boolean commitFlag = true;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn)
			throws Exception {
		super();
		this.setCommand(command);
		// 初始化检查参数
		this.initCheckCommand();
		this.setConn(conn);
		this.setResult(result);
	}

	public boolean prepareData() throws Exception {
		// 获取此ADLINK上行政取区划面拓扑关系
		List<AdFace> faces = new AdFaceSelector(this.getConn())
				.loadAdFaceByLinkId(this.getCommand().getLinkPid(), true);
		this.getCommand().setFaces(faces);
		// 获取要打断ADLINK的对象
		AdLink breakLink = (AdLink) new AdLinkSelector(this.getConn())
				.loadById(this.getCommand().getLinkPid(), true, false);
		
		if(this.getCommand().getRepairLinkGeo()!=null)
		{
			breakLink.setGeometry(this.getCommand().getRepairLinkGeo());
		}
		
		this.getCommand().setBreakLink(breakLink);
		// 删除要打断ADLINK
		this.getResult().insertObject(breakLink, ObjStatus.DELETE,
				breakLink.pid());

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
			// 创建行政区划点有关行政区划线具体操作
			OpTopo operation = new OpTopo(this.getCommand(), check,
					this.getConn());
			msg = operation.run(this.getResult());
			// 创建行政区划点有关行政区划面具体操作类
			OpRefAdFace opRefAdFace = new OpRefAdFace(this.getCommand(),
					this.getConn());
			opRefAdFace.run(this.getResult());
		} catch (Exception e) {

			this.getConn().rollback();

			throw e;
		}

		return msg;
	}

	@Override
	public void postCheck() throws Exception {
		check.postCheck(this.getConn(), this.getResult(), this.getCommand()
				.getDbId());
		super.postCheck();
	}

	@Override
	public String exeOperation() throws Exception {
		// 创建行政区划点有关行政区划线具体操作
		OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		// 创建行政区划点有关行政区划面具体操作类
		OpRefAdFace opRefAdFace = new OpRefAdFace(this.getCommand(),
				this.getConn());
		opRefAdFace.run(this.getResult());
		return msg;
	}

}
