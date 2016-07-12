package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakin.breaklupoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuFaceTopoSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> {

	private Check check = new Check();

	private Boolean commitFlag = true;

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	public Process(Command command, Result result, Connection conn) throws Exception {
		super(command);
		this.setConn(conn);
		this.setResult(result);
	}

	public boolean prepareData() throws Exception {
		// 获取此LULINK上面拓扑关系
		List<LuFaceTopo> luFaceTopos = new LuFaceTopoSelector(this.getConn()).loadByLinkPid(this.getCommand().getLinkPid(), true);
		this.getCommand().setLuFaceTopos(luFaceTopos);
		List<LuFace> faces = new LuFaceSelector(this.getConn()).loadLuFaceByLinkId(this.getCommand().getLinkPid(), true);
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
			// 创建土地利用点有关土地利用线的具体操作类
			OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
			msg = operation.run(this.getResult());
			// 创建土地利用点有关土地利用面的具体操作类
			OpRefLuFace opRefLuFace = new OpRefLuFace(this.getCommand(),this.getConn());
			opRefLuFace.run(this.getResult());
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
		check.postCheck(this.getConn(), this.getResult(),this.getCommand().getDbId());
	}
	@Override
	public String exeOperation() throws Exception {
		// 创建土地利用点有关土地利用线的具体操作类
		OpTopo operation = new OpTopo(this.getCommand(), check, this.getConn());
		String msg = operation.run(this.getResult());
		// 创建土地利用点有关土地利用面的具体操作类
		OpRefLuFace opRefAdFace = new OpRefLuFace(this.getCommand(),this.getConn());
		opRefAdFace.run(this.getResult());
		return msg;
	}

}
