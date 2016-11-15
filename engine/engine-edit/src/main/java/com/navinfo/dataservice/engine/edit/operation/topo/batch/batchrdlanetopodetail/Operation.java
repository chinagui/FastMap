package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail;

import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;

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
		List<RdLaneTopoDetail> details = this.command.getLaneToptInfos();
		// 批量增加
		for (RdLaneTopoDetail detail : details) {
			detail.setPid(PidUtil.getInstance().applyRdLaneTopoPid());
			for (IRow row : detail.getTopoVias()) {
				RdLaneTopoVia via = (RdLaneTopoVia) row;
				via.setTopoId(detail.getPid());
			}
			result.insertObject(detail, ObjStatus.INSERT, detail.getPid());
		}
		// 处理批量删除
		for (IRow row : this.command.getDelToptInfos()) {
			RdLaneTopoDetail detail = (RdLaneTopoDetail) row;
			result.insertObject(detail, ObjStatus.DELETE, detail.getPid());
		}
		// 处理批量修改
		if (this.command.getUpdateArray() != null) {
			if (this.command.getUpdateArray().size() > 0) {
				for (int i = 0; i < this.command.getUpdateArray().size(); i++) {
					JSONObject obj = this.command.getUpdateArray()
							.getJSONObject(i);
					this.updateLaneTopos(obj.getJSONObject("data"),
							this.command.getUpdateTopInfos().get(i), result);
				}

			}
		}
	}

	/***
	 * 
	 * @param obj 修改变化字段json
	 * @param row 修改的对象
	 * @param result
	 * @throws Exception
	 */
	private void updateLaneTopos(JSONObject obj, IRow row, Result result)
			throws Exception {
		RdLaneTopoDetail detail = (RdLaneTopoDetail) row;
		JSONObject content = obj.getJSONObject("data");
		if (content.containsKey("objStatus")) {
			boolean isChanged = detail.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(row, ObjStatus.UPDATE, detail.getPid());
			}

		}
	}
}