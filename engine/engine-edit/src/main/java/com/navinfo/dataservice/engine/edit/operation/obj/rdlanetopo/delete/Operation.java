package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/***
 * 删除车道联通信息信息
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
		this.deleteRdLaneTopo(result);
		return null;
	}

	private void deleteRdLaneTopo(Result result) {
		if(this.command.getDetail() != null ){
			result.insertObject(this.command.getDetail(), ObjStatus.DELETE, this.command.getDetail().getPid());
		}
	}

}
