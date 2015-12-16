package com.navinfo.dataservice.FosEngine.edit.operation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.Result;

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
	private ICommand command;

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
	private ICommand createCommand() {
		JSONObject json = JSONObject.fromObject(requester);

		operType = Enum.valueOf(OperType.class, json.getString("command"));
		
		objType = Enum.valueOf(ObjType.class, json.getString("type"));
		
		switch(objType){
		case RDLINK:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create.Command(
						json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.update.Command(
						json, requester);
			case DELETE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink.Command(json,
						requester);
			case BREAK:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Command(json,
						requester);
			}
		case RDNODE:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Command(json,
						requester);
			}
		case RDRESTRICTION:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.create.Command(
						json, requester);
			case UPDATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.update.Command(
						json, requester);
			}
		}

		return null;
	}

	/**
	 * 创建操作进程
	 * 
	 * @param command
	 *            操作命令
	 * @return 操作进程
	 * @throws Exception
	 */
	private IProcess createProcess(ICommand command) throws Exception {

		switch(objType){
		case RDLINK:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.create.Process(
						command);
			case UPDATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdlink.update.Process(
						command);
			case DELETE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.deletelink.Process(
						command);
			case BREAK:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Process(
						command);
			}
		case RDNODE:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Process(
						command);
			}
		case RDRESTRICTION:
			switch(operType){
			case CREATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.create.Process(
						command);
			case UPDATE:
				return new com.navinfo.dataservice.FosEngine.edit.operation.obj.rdrestriction.update.Process(
						command);
			}
		}

		return null;
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
	
	public JSONArray getCheckLog(){
		return process.getResult().getCheckResults();
				
	}

}
