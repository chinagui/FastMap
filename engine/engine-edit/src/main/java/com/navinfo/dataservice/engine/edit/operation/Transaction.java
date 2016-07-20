package com.navinfo.dataservice.engine.edit.operation;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.iface.Result;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 操作控制器
 */
public class Transaction {

	/**
	 * 请求参数
	 */
	private String requester;

	/**
	 * 操作类型
	 */
	private OperType operType;

	/**
	 * 对象类型
	 */
	private ObjType objType;

	/**
	 * 命令对象
	 */
	private AbstractCommand command;

	/**
	 * 操作进程对象
	 */
	private IProcess process;

	public OperType getOperType() {
		return operType;
	}

	public Transaction(String requester) {
		this.requester = requester;
	}

	public String getRequester() {
		return requester;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	/**
	 * 创建操作命令
	 * 
	 * @return 命令
	 */
	private AbstractCommand createCommand() throws Exception {
		JSONObject json = JSONObject.fromObject(requester);

		operType = Enum.valueOf(OperType.class, json.getString("command"));

		objType = Enum.valueOf(ObjType.class, json.getString("type"));

		switch (objType) {
		case RDLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Command(json,
						requester);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(json,
						requester);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Command(json,
						requester);
			case UPDOWNDEPART:
				return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Command(json,
						requester);
			// case DEPART:
			// return new
			// com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Command(
			// json, requester);
			}
		case RDNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Command(json,
						requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(json, requester);
			}
		case RDRESTRICTION:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Command(json,
						requester);
			}
		case RDCROSS:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete.Command(json, requester);
			}
		case RDBRANCH:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Command(json, requester);
			}
		case RDLANECONNEXITY:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Command(json,
						requester);
			}
		case RDSPEEDLIMIT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Command(json,
						requester);
			}
		case RDLINKSPEEDLIMIT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Command(
						json, requester);
			}
		case ADADMIN:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Command(json, requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Command(json, requester);
			}
		case RDGSC:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Command(json, requester);
			}
		case ADNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Command(json, requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Command(json,
						requester);
			}
		case ADLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Command(json, requester);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Command(json,
						requester);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Command(json,
						requester);
			}
		case ADFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Command(json, requester);
			}
		case ADADMINGROUP:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Command(json,
						requester);
			}
		case IXPOI:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Command(json, requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Command(json, requester);
			}
		case IXPOIPARENT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Command(json, requester);

			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Command(json, requester);

			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Command(json);
			}
		case RWNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Command(json,
						requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command(json, requester);
			}

		case RWLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Command(json,
						requester);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Command(json,
						requester);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Command(json,
						requester);
			}
		case ZONENODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Command(json, requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Command(json,
						requester);
			}
		case ZONELINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Command(json, requester);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Command(json,
						requester);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Command(json,
						requester);
			}
		case ZONEFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Command(json, requester);
			}
		case LUNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Command(json, requester);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Command(json,
						requester);
			}
		case LULINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Command(json, requester);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Command(json,
						requester);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Command(json,
						requester);
			}
		case LUFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Command(json, requester);
			}
		case RDELECTRONICEYE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Command(json, requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Command(json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Command(json, requester);
			}
		case RDELECEYEPAIR:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Command(json,
						requester);
			default:
				break;
			}
		case RDTRAFFICSIGNAL:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Command(json,
						requester);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Command(json,
						requester);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Command(json,
						requester);
			default:
				break;
			}
		}

		throw new Exception("不支持的操作类型");
	}

	/**
	 * 创建操作进程
	 * 
	 * @param command
	 *            操作命令
	 * @return 操作进程
	 * @throws Exception
	 */
	private IProcess createProcess(AbstractCommand command) throws Exception {

		switch (objType) {
		case RDLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdlink.Process(command);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(command);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrdlink.Process(command);
			case UPDOWNDEPART:
				return new com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink.Process(command);
			// case DEPART:
			// return new
			// com.navinfo.dataservice.engine.edit.edit.operation.topo.departnode.Process(
			// command);
			}
		case RDNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process(command);
			}
		case RDRESTRICTION:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete.Process(command);
			}
		case RDCROSS:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdcross.delete.Process(command);
			}
		case RDBRANCH:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.delete.Process(command);
			}
		case RDLANECONNEXITY:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.delete.Process(command);
			}
		case RDSPEEDLIMIT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdspeedlimit.delete.Process(command);
			}
		case RDLINKSPEEDLIMIT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdlink.rdlinkspeedlimit.create.Process(
						command);
			}
		case ADADMIN:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.delete.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move.Process(command);
			}
		case RDGSC:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete.Process(command);
			}
		case ADNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adnode.update.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadnode.Process(command);
			}
		case ADLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adlink.update.Process(command);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakadpoint.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleteadlink.Process(command);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairadlink.Process(command);
			}
		case ADFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adface.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adface.delete.Process(command);
			}
		case ADADMINGROUP:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.adadmingroup.delete.Process(command);
			}
		case IXPOI:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.create.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.move.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poi.delete.Process(command);
			}
		case IXPOIPARENT:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.create.Process(command);

			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.update.Process(command);

			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.poiparent.delete.Process(command);
			}
		case RWNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwnode.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwnode.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process(command);
			}
		case RWLINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rwlink.update.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterwlink.Process(command);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairrwlink.Process(command);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrwpoint.Process(command);
			}

		case ZONENODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonenode.update.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonenode.Process(command);
			}
		case ZONELINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zonelink.update.Process(command);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakzonepoint.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletezonelink.Process(command);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairzonelink.Process(command);
			}
		case ZONEFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.zoneface.delete.Process(command);
			}
		case LUNODE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lunode.update.Process(command);
			case MOVE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelunode.Process(command);
			}
		case LULINK:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.create.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.lulink.update.Process(command);
			case BREAK:
				return new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklupoint.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.topo.delete.deletelulink.Process(command);
			case REPAIR:
				return new com.navinfo.dataservice.engine.edit.operation.topo.repair.repairlulink.Process(command);
			}
		case LUFACE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.luface.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.luface.delete.Process(command);
			}
		case RDELECTRONICEYE:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.delete.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.update.Process(command);
			}
		case RDELECEYEPAIR:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete.Process(command);
			default:
				break;
			}
		case RDTRAFFICSIGNAL:
			switch (operType) {
			case CREATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create.Process(command);
			case DELETE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.delete.Process(command);
			case UPDATE:
				return new com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update.Process(command);
			default:
				break;
			}
		}

		throw new Exception("不支持的操作类型");

	}

	public Result createResult() {
		return null;
	}

	/**
	 * 执行操作
	 * 
	 * @return
	 * @throws Exception
	 */
	public String run() throws Exception {
		command = this.createCommand();

		process = this.createProcess(command);

		return process.run();

	}

	/**
	 * @return 操作简要日志信息
	 */
	public String getLogs() {

		return process.getResult().getLogs();
	}

	public JSONArray getCheckLog() {
		return process.getResult().getCheckResults();

	}

	public int getPid() {
		return process.getResult().getPrimaryPid();
	}

}
