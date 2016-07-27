package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete;



import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/***
 * 删除坡度信息
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
		this.deleteRdSlope(result);
		return null;
	}
	
	private void deleteRdSlope(Result  result){
		result.insertObject(this.command.getSlope(), ObjStatus.DELETE, this.command.getSlope().getPid());
	}

		

}
