package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;

/***
 * 删除车道信息
 * 
 * @author zhaokk
 * 
 */
public class Operation implements IOperation {

	private Command command;


	public Operation(Command command) {
		this.command = command;

	}
	@Override
	public String run(Result result) throws Exception {
		this.deleteRdLane(result);
		return null;
	}

	private void deleteRdLane(Result result) {
		//加载删除详细车道上rdlink其它车道信息
		if(this.command.getLanes().size() > 0){
			for(RdLane lane :this.command.getLanes()){
				if(lane.getPid() == this.command.getRdLane().getPid()){
					result.insertObject(this.command.getRdLane(), ObjStatus.DELETE, lane.getPid());
					continue;
				}
				if(lane.getSeqNum() > this.command.getRdLane().getSeqNum()){
					lane.changedFields().put("seqNum", lane.getSeqNum() -1);
				}
				lane.changedFields().put("laneNum", lane.getLaneNum() -1);
				result.insertObject(lane, ObjStatus.UPDATE, lane.getPid());
			}
			
			
		}
		
	}

}
