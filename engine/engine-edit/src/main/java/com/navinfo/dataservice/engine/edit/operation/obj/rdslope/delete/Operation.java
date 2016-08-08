package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

/***
 * 删除坡度信息
 * 
 * @author zhaokk
 * 
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;

	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.deleteRdSlope(result);
		return null;
	}

	private void deleteRdSlope(Result result) {
		result.insertObject(this.command.getSlope(), ObjStatus.DELETE,
				this.command.getSlope().getPid());
	}

	/**
	 * 删除link维护信息
	 * 
	 * @param linkPid
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void deleteByLink(Result result, int linkPid) throws Exception {

		if (conn == null || linkPid == 0) {
			return;
		}
        //如果删除的是退出线 则对应的坡度被删除
		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<RdSlope> slopes = selector.loadByOutLink(linkPid, true);

		for (RdSlope slope : slopes) {

			result.insertObject(slope, ObjStatus.DELETE, slope.getPid());
		}
		//如果删除的是接续 LINK需要维护 坡度接续 LINK 表
		List<RdSlopeVia> rdSlopeVias =  selector.loadBySeriesLink(linkPid, true);
		
		for(RdSlopeVia via :rdSlopeVias){
			List<IRow>  vias = new AbstractSelector(RdSlopeVia.class, conn).loadRowsByParentId(via.getSlopePid(), true);
			for(IRow row :vias){
				RdSlopeVia slopeVia = (RdSlopeVia)row;
				if(slopeVia.getSeqNum() >= via.getSeqNum()){
					result.insertObject(slopeVia, ObjStatus.DELETE, slopeVia.getSlopePid());
				}
			}
		}
	}

}
