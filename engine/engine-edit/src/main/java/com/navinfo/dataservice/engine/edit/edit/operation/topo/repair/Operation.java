package com.navinfo.dataservice.engine.edit.edit.operation.topo.repair;

import java.sql.Connection;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.edit.operation.ICommand;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class Operation implements IOperation {
	
	private Command command;
	
	private RdLink updateLink;
	
	private RdNode enode;
	
	private RdNode snode;
	
	private Connection conn;
	
	public Operation(Connection conn, Command command, RdLink updateLink,RdNode snode, RdNode enode, Check check) {
		
		this.conn = conn;
		
		this.command = command;

		this.updateLink = updateLink;
		
		this.enode = enode;
		
		this.snode = snode;
		
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
				
				int nodePid = mountNode.getInt("nodePid");
				
				int pid = mountNode.getInt("pid");
				
				if (nodePid == updateLink.getsNodePid()){
					content.put("sNodePid", pid);
					
					result.insertObject(snode, ObjStatus.DELETE);
				}else{
					content.put("eNodePid", pid);
					
					result.insertObject(enode, ObjStatus.DELETE);
				}
			}
			
		}else if (command.getInterLines().size() == 1 && command.getInterNodes().size() == 1){
			//link的一个端点打断另外一根link、link的一个端点挂接到另外一组link的端点
			
			this.breakLine();
			
			for(int i=0;i<command.getInterNodes().size();i++){
				JSONObject mountNode = command.getInterNodes().getJSONObject(i);
				
				int nodePid = mountNode.getInt("nodePid");
				
				int pid = mountNode.getInt("pid");
				
				if (nodePid == updateLink.getsNodePid()){
					content.put("sNodePid", pid);
					
					result.insertObject(snode, ObjStatus.DELETE);
				}else{
					content.put("eNodePid", pid);
					
					result.insertObject(enode, ObjStatus.DELETE);
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
	
	public void breakLine() throws Exception{
		
		JSONArray coords = command.getLinkGeom().getJSONArray("coordinates");
		
		for (int i = 0; i <command.getInterLines().size(); i++) {
			//link的一个端点打断另外一根link
			JSONObject interLine = command.getInterLines().getJSONObject(i);
			JSONObject breakJson = new JSONObject();
			JSONObject data = new JSONObject();
			
			breakJson.put("objId", interLine.getInt("pid"));
			breakJson.put("projectId", command.getProjectId());
			
			int nodePid = interLine.getInt("nodePid");
			if (nodePid == updateLink.getsNodePid()) {
				data.put("breakNodePid", updateLink.getsNodePid());
				
				JSONArray coord = coords.getJSONArray(0);
				
				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);
				
				data.put("longitude", lon);
				data.put("latitude", lat);
			} else {
				data.put("breakNodePid", updateLink.geteNodePid());
				
				JSONArray coord = coords.getJSONArray(coords.size()-1);
				
				double lon = coord.getDouble(0);
				double lat = coord.getDouble(1);
				
				data.put("longitude", lon);
				data.put("latitude", lat);
			}
			breakJson.put("data", data);
			ICommand breakCommand = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Command(
					breakJson, breakJson.toString());
			com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint.Process(
					breakCommand, conn);
			breakProcess.runNotCommit();
		}
	}

}
