package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail;

import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * 车道联通批量操作
 * 
 * @author 赵凯凯
 * 
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		this.createRdLaneTopos(result);
		return null;
	}

	/**
	 * 新增车道联通信息/如果进入车道和退出车道变化 需要删除原有车道联通信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void createRdLaneTopos(Result result) throws Exception {
		List<RdLaneTopoDetail>  details = this.command.getLaneToptInfos();
		for(RdLaneTopoDetail detail:details){
			detail.setPid(PidService.getInstance().applyRdLaneTopoPid());
			for(IRow row :detail.getTopoVias()){
				RdLaneTopoVia via = (RdLaneTopoVia)row;
				via.setTopoId(detail.getPid());
			}
			result.insertObject(detail, ObjStatus.INSERT, detail.getPid());
		}
		for(IRow row:this.command.getDelToptInfos()){
			RdLaneTopoDetail detail = (RdLaneTopoDetail)row;
			result.insertObject(detail,ObjStatus.DELETE, detail.getPid());
		}
	}
}