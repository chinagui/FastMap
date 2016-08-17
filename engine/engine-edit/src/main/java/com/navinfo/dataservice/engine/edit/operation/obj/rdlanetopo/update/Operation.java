package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.update;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;

/**
 * @author zhaokk 修改车道信息
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {
		this.updateRdLane(result);
		return null;
	}

	/***
	 * 修改车道联通信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateRdLane(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {
			boolean isChanged = this.command.getDetail().fillChangeFields(
					content);

			if (isChanged) {
				result.insertObject(this.command.getDetail(), ObjStatus.UPDATE,
						this.command.getDetail().getPid());
			}

			
		}
	}
 
}
