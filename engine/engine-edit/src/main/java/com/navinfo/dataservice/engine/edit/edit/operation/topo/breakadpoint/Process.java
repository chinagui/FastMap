package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.operator.ad.geo.AdFaceTopoOperator;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceTopoSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;
import com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.OpRefRestrict;

/**
 * @author zhaokk
 * 新增行政区划点具体执行类
 */
public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;
	
	private String postCheckMsg;
	
	private Check check = new Check();
	
	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
				.getProjectId());

	}
	public Process(ICommand command,Connection conn) throws Exception {
		this.command = (Command) command;
		this.result = new Result();
		this.conn = conn;

	}
	@Override
	public ICommand getCommand() {
		
		return command;
	}

	@Override
	public Result getResult() {
		
		return result;
	}
    
	@Override
	public boolean prepareData() throws Exception {
		    //获取此ADLINK上行政取区划面拓扑关系
			List<AdFaceTopo> adFaceTopos= new AdFaceTopoSelector(conn)
							.loadByLinkPid(command.getLinkPid(), true);
			command.setAdFaceTopos(adFaceTopos);
			List<AdFace>  adfaces = new  AdFaceSelector(conn).loadAdFaceByLinkId(command.getLinkPid(), true);
			command.setFaces(adfaces);

		return true;
	}

	@Override
	public String preCheck() throws Exception {
		
		return null;
	}

	@Override
	public String run() throws Exception {
		String msg;
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}
			//创建行政区划点有关行政区划线具体操作
			OpTopo operation = new OpTopo(command, check, conn);
			msg = operation.run(result);
			//创建行政区划点有关行政区划面具体操作类
			OpRefAdFace opRefAdFace = new OpRefAdFace(command,conn);
			opRefAdFace.run(result);
			this.recordData();
			conn.commit();

		} catch (Exception e) {
			
			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				
			}
		}

		return msg;
	}

	@Override
	public void postCheck() throws Exception {
		
		check.postCheck(conn, result);
	}

	@Override
	public String getPostCheck() throws Exception {
		
		return postCheckMsg;
	}

	@Override
	public boolean recordData() throws Exception {
		
		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;
	}

}
