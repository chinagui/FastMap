package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 详细车道批量联通操作接口
 * 
 * @author zhaokaikai
 * 
 */
public class Command extends AbstractCommand {

	private String requester;
	private List<RdLaneTopoDetail> laneToptInfos = new ArrayList<RdLaneTopoDetail>();// 车道联通信息
	private List<IRow> delToptInfos = new ArrayList<IRow>();// 要删除车道联通信息
	private List<IRow> updateTopInfos = new ArrayList<IRow>();// 要修改的联通信息

	public List<IRow> getDelToptInfos() {
		return delToptInfos;
	}

	public void setDelToptInfos(List<IRow> delToptInfos) {
		this.delToptInfos = delToptInfos;
	}

	private List<Integer> delTopoIds = new ArrayList<Integer>();// 删除的车道联通id
	private List<Integer> updateTopoIds = new ArrayList<Integer>();// 删除的车道联通id
	private int inLanePid;// 进入车道
	private int inLinkPid;// 进入LINK
	private int nodePid;// 进入NODE
	private JSONArray updateArray;

	public JSONArray getUpdateArray() {
		return updateArray;
	}

	public void setUpdateArray(JSONArray updateArray) {
		this.updateArray = updateArray;
	}

	@Override
	public OperType getOperType() {
		return OperType.BATCH;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLANETOPODETAIL;
	}

	public int getInLanePid() {
		return inLanePid;
	}

	public void setInLanePid(int inLanePid) {
		this.inLanePid = inLanePid;
	}

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");
		// 进入线
		this.setInLinkPid(data.getInt("inLinkPid"));
		// 修改的车道联通信息
		if (data.containsKey("updateInfos")) {
			this.updateArray = data.getJSONArray("updateInfos");
			for (int i = 0; i < updateArray.size(); i++) {
				JSONObject obj = updateArray.getJSONObject(i);
				this.updateTopoIds.add(obj.getInt("objId"));
			}
		}
		// 进入点
		this.setNodePid(data.getInt("inNodePid"));
		// 删除的联通信息
		if (data.containsKey("topoIds")) {
			for (int i = 0; i < data.getJSONArray("topoIds").size(); i++) {
				this.delTopoIds.add(data.getJSONArray("topoIds").getInt(i));
			}
		}
		for (int i = 0; i < data.getJSONArray("laneTopoInfos").size(); i++) {
			RdLaneTopoDetail laneTopoDetail = new RdLaneTopoDetail();
			// 车道联通信息
			JSONObject jsonObject = data.getJSONArray("laneTopoInfos")
					.getJSONObject(i);
			laneTopoDetail.setInLanePid(jsonObject.getInt("inLanePid"));
			laneTopoDetail.setInLinkPid(this.getInLinkPid());
			laneTopoDetail.setNodePid(this.getNodePid());
			// 退出车道
			laneTopoDetail.setOutLanePid(jsonObject.getInt("outLanePid"));
			// 退出线
			laneTopoDetail.setOutLinkPid(jsonObject.getInt("outLinkPid"));
			// 处理标识
			if (jsonObject.containsKey("processFlag")) {
				laneTopoDetail.setProcessFlag(jsonObject.getInt("processFlag"));
			}
			// 是否借道
			if (jsonObject.containsKey("throughTurn")) {
				laneTopoDetail.setThroughTurn(jsonObject.getInt("throughTurn"));
			}
			// 车辆类型
			if (jsonObject.containsKey("vehicle")) {
				laneTopoDetail.setVehicle(jsonObject.getLong("VEHICLE"));
			}
			if (jsonObject.containsKey("timeDomain")) {
				laneTopoDetail.setTimeDomain("timeDomain");
			}
			// 车道联通经过线信息
			if (jsonObject.containsKey("laneTopoVias")) {
				List<IRow> topoVias = new ArrayList<IRow>();
				for (int j = 0; j < jsonObject.getJSONArray("laneTopoVias")
						.size(); j++) {
					JSONObject via = jsonObject.getJSONArray("laneTopoVias")
							.getJSONObject(j);
					RdLaneTopoVia topoVia = new RdLaneTopoVia();
					topoVia.setLanePid(via.getInt("lanePid"));
					topoVia.setSeqNum(via.getInt("seqNum"));
					topoVia.setViaLinkPid(via.getInt("linkPid"));
					topoVias.add(topoVia);

				}
				laneTopoDetail.setTopoVias(topoVias);
			}
			laneToptInfos.add(laneTopoDetail);

		}

	}

	public List<RdLaneTopoDetail> getLaneToptInfos() {
		return laneToptInfos;
	}

	public void setLaneToptInfos(List<RdLaneTopoDetail> laneToptInfos) {
		this.laneToptInfos = laneToptInfos;
	}

	public List<IRow> getUpdateTopInfos() {
		return updateTopInfos;
	}

	public void setUpdateTopInfos(List<IRow> updateTopInfos) {
		this.updateTopInfos = updateTopInfos;
	}

	public List<Integer> getDelTopoIds() {
		return delTopoIds;
	}

	public void setDelTopoIds(List<Integer> delTopoIds) {
		this.delTopoIds = delTopoIds;
	}

	public List<Integer> getUpdateTopoIds() {
		return updateTopoIds;
	}

	public void setUpdateTopoIds(List<Integer> updateTopoIds) {
		this.updateTopoIds = updateTopoIds;
	}

}
