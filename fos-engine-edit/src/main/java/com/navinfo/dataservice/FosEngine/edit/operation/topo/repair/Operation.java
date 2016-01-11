package com.navinfo.dataservice.FosEngine.edit.operation.topo.repair;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.FosEngine.edit.operation.IProcess;

public class Operation implements IOperation {
	
	private Command command;
	
	private RdLink updateLink;

	public Operation(Command command, RdLink updateLink) {
		this.command = command;

		this.updateLink = updateLink;
	}

	@Override
	public String run(Result result) throws Exception {
		
		JSONObject content = new JSONObject();
		
		content.put("geometry", command.getLinkGeom());

		if (command.getInterLines().size() == 0 && command.getInterNodes().size() ==0){
			//平滑修型,增删形状点
		}else if ((command.getInterLines().size() == 1 || command.getInterLines().size() == 2) && command.getInterNodes().size() == 0){
			
			this.breakLine();
			
		}else if (command.getInterLines().size() == 0 && (command.getInterNodes().size() == 1 || command.getInterNodes().size() == 2)){
			//link的一个端点挂接到另外一组link的端点
			for(int i=0;i<command.getInterNodes().size();i++){
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);
				
				if (mountNode.getString("type").equals("s")){
					content.put("sNodePid", mountNode.getInt("nodePid"));
				}else{
					content.put("eNodePid", mountNode.getInt("nodePid"));
				}
			}
			
		}else if (command.getInterLines().size() == 1 && command.getInterNodes().size() == 1){
			//link的一个端点打断另外一根link、link的一个端点挂接到另外一组link的端点
			
			this.breakLine();
			
			for(int i=0;i<command.getInterNodes().size();i++){
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);
				
				if (mountNode.getString("type").equals("s")){
					content.put("sNodePid", mountNode.getInt("nodePid"));
				}else{
					content.put("eNodePid", mountNode.getInt("nodePid"));
				}
			}
		}else{
			//错误请求
		}
		
		boolean isChanged = updateLink.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(updateLink, ObjStatus.UPDATE);
		}
		
		return null;
	}
	
	private void breakLine() throws Exception{
		for (int i = 0; i <command.getInterLines().size(); i++) {
			//link的一个端点打断另外一根link
			JSONObject interLine = command.getInterLines().getJSONObject(i);
			JSONObject breakJson = new JSONObject();
			breakJson.put("linkPid", interLine.getInt("linkPid"));
			breakJson.put("data", interLine.getJSONObject("data"));
			breakJson.put("projectId", command.getProjectId());
			if (interLine.getString("type").equals("s")) {
				breakJson.put("breakNodePid", updateLink.getsNodePid());
			} else {
				breakJson.put("breakNodePid", updateLink.geteNodePid());
			}
			ICommand breakCommand = new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Command(
					breakJson, breakJson.toString());
			IProcess breakProcess = new com.navinfo.dataservice.FosEngine.edit.operation.topo.breakpoint.Process(
					breakCommand);
			breakProcess.run();
		}
	}

}
