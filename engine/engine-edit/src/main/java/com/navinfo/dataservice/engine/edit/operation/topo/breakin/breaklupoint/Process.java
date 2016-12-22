package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceTopoSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

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
		// 获取此LULINK上面拓扑关系
		List<LuFaceTopo> luFaceTopos = new LuFaceTopoSelector(this.getConn())
				.loadByLinkPid(this.getCommand().getLinkPid(), true);
		this.getCommand().setLuFaceTopos(luFaceTopos);
		List<LuFace> faces = new LuFaceSelector(this.getConn())
				.loadLuFaceByLinkId(this.getCommand().getLinkPid(), true);
		this.getCommand().setFaces(faces);

		// 获取要打断LCLINK的对象
		LuLink breakLink = (LuLink) new LuLinkSelector(this.getConn())
				.loadById(this.getCommand().getLinkPid(), true, false);
		
		if(this.getCommand().getRepairLinkGeo()!=null)
		{
			breakLink.setGeometry(this.getCommand().getRepairLinkGeo());
		}
		this.getCommand().setBreakLink(breakLink);
		// 删除要打断LCLINK
		this.getResult().insertObject(breakLink, ObjStatus.DELETE,
				breakLink.pid());

		return true;
	}

	public String innerRun() throws Exception {
		String msg;
		try {

			this.prepareData();
			
			// 创建土地利用点有关土地利用线的具体操作类
			OpTopo operation = new OpTopo(this.getCommand(), this.getConn());
			msg = operation.run(this.getResult());
			// 创建土地利用点有关土地利用面的具体操作类
			OpRefLuFace opRefLuFace = new OpRefLuFace(this.getCommand(),
					this.getConn());
			opRefLuFace.run(this.getResult());
			
			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
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
		// 创建土地利用点有关土地利用线的具体操作类
		OpTopo operation = new OpTopo(this.getCommand(), this.getConn());
		String msg = operation.run(this.getResult());
		// 创建土地利用点有关土地利用面的具体操作类
		OpRefLuFace opRefAdFace = new OpRefLuFace(this.getCommand(),
				this.getConn());
		opRefAdFace.run(this.getResult());
		return msg;
	}

}
