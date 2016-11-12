package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlanetopodetail;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoVia;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * 详细车道批量联通操作接口
 * 
 * @author zhaokaikai
 * 
 */
public class Command extends AbstractCommand {

	private String requester;
	private List<RdLaneTopoDetail> laneToptInfos;//车道联通信息
	private List<IRow> delToptInfos = new ArrayList<IRow>() ;//要删除车道联通信息
	public List<IRow> getDelToptInfos() {
		return delToptInfos;
	}

	public void setDelToptInfos(List<IRow> delToptInfos) {
		this.delToptInfos = delToptInfos;
	}

	private List<Integer> topoIds;
	private int inLanePid;//进入车道
	private int inLinkPid;//进入LINK
	private int nodePid;//进入NODE
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
		this.setInLinkPid(data.getInt("inLinkPid"));
		this.setNodePid(data.getInt("inNodePid"));
		laneToptInfos = new ArrayList<RdLaneTopoDetail>();
		topoIds = new ArrayList<Integer>();
		if(data.containsKey("topoIds")){
			for(int i = 0 ; i < data.getJSONArray("topoIds").size();i++){
				topoIds.add(data.getJSONArray("topoIds").getInt(i));
			}
		}
		this.setTopoIds(topoIds);
		for(int i = 0;i < data.getJSONArray("laneTopoInfos").size(); i++ ){
			RdLaneTopoDetail  laneTopoDetail = new RdLaneTopoDetail();
			JSONObject jsonObject = data.getJSONArray("laneTopoInfos").getJSONObject(i);
			laneTopoDetail.setInLanePid(jsonObject.getInt("inLanePid"));
			laneTopoDetail.setInLinkPid(this.getInLinkPid());
			laneTopoDetail.setNodePid(this.getNodePid());
			laneTopoDetail.setOutLanePid(jsonObject.getInt("outLanePid"));
			laneTopoDetail.setOutLinkPid(jsonObject.getInt("outLinkPid"));
			if(jsonObject.containsKey("laneTopoVias")){
				List<IRow> topoVias = new ArrayList<IRow>();
				for(int j =0 ;j < jsonObject.getJSONArray("laneTopoVias").size();j++){
					JSONObject via = jsonObject.getJSONArray("laneTopoVias").getJSONObject(j);
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

	public List<Integer> getTopoIds() {
		return topoIds;
	}

	public void setTopoIds(List<Integer> topoIds) {
		this.topoIds = topoIds;
	}


}
