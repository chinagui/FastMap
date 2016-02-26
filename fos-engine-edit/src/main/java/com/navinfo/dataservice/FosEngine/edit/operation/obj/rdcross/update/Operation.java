package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdcross.update;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossName;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkFormSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.service.PidService;

public class Operation implements IOperation {

	private Command command;

	private RdCross cross;
	
	private Connection conn;

	public Operation(Command command, RdCross cross, Connection conn) {
		this.command = command;

		this.cross = cross;
		
		this.conn = conn;

	}

	private String updateProperty(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(cross, ObjStatus.DELETE);

				return null;
			} else {

				boolean isChanged = cross.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(cross, ObjStatus.UPDATE);
				}
			}
		}

		if (content.containsKey("nodes")) {
			JSONArray nodes = content.getJSONArray("nodes");

			for (int i = 0; i < nodes.size(); i++) {

				JSONObject nodeJson = nodes.getJSONObject(i);

				if (nodeJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							nodeJson.getString("objStatus"))) {

						RdCrossNode node = cross.nodeMap.get(nodeJson
								.getString("rowId"));

						if (node == null) {
							throw new Exception("rowId="
									+ nodeJson.getString("rowId") + "的rd_cross_node不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								nodeJson.getString("objStatus"))) {
							result.insertObject(node, ObjStatus.DELETE);

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								nodeJson.getString("objStatus"))) {

							boolean isChanged = node.fillChangeFields(nodeJson);

							if (isChanged) {
								result.insertObject(node, ObjStatus.UPDATE);
							}
						}
					} else {
						RdCrossNode node = new RdCrossNode();

						node.Unserialize(nodeJson);
						
						node.setPid(cross.getPid());
						
						node.setMesh(cross.mesh());

						result.insertObject(node, ObjStatus.INSERT);

						continue;
					}
				}

			}
		}
		
		if (content.containsKey("links")) {
			JSONArray links = content.getJSONArray("links");

			for (int i = 0; i < links.size(); i++) {

				JSONObject json = links.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							json.getString("objStatus"))) {

						RdCrossLink node = cross.linkMap.get(json
								.getString("rowId"));

						if (node == null) {
							throw new Exception("rowId="
									+ json.getString("rowId") + "的rd_cross_link不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								json.getString("objStatus"))) {
							result.insertObject(node, ObjStatus.DELETE);

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								json.getString("objStatus"))) {

							boolean isChanged = node.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(node, ObjStatus.UPDATE);
							}
						}
					} else {
						RdCrossLink link = new RdCrossLink();

						link.Unserialize(json);
						
						link.setPid(cross.getPid());
						
						link.setMesh(cross.mesh());

						result.insertObject(link, ObjStatus.INSERT);

						continue;
					}
				}

			}
		}
		
		if (content.containsKey("names")) {
			JSONArray array = content.getJSONArray("names");

			for (int i = 0; i < array.size(); i++) {

				JSONObject json = array.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							json.getString("objStatus"))) {

						RdCrossName name = cross.nameMap.get(json
								.getString("rowId"));

						if (name == null) {
							throw new Exception("rowId="
									+ json.getString("rowId") + "的rd_cross_name不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								json.getString("objStatus"))) {
							result.insertObject(name, ObjStatus.DELETE);

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								json.getString("objStatus"))) {

							boolean isChanged = name.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(name, ObjStatus.UPDATE);
							}
						}
					} else {
						RdCrossName name = new RdCrossName();

						name.Unserialize(json);
						
						name.setNameId(PidService.getInstance().applyRdCrossNameId());
						
						name.setPid(cross.getPid());
						
						name.setMesh(cross.mesh());

						result.insertObject(name, ObjStatus.INSERT);

						continue;
					}
				}

			}
		}
		
		
		return null;
	}

	private String updateNodeLink(Result result) throws Exception {
		
		JSONObject content = command.getContent();
		
		JSONArray nodePidArray = content.getJSONArray("nodePids");
		
		JSONArray linkPidArray = content.getJSONArray("linkPids");
		
		if(nodePidArray.size() == 1 && cross.getType() == 1){
			cross.changedFields().put("type", 0);
			
			result.getUpdateObjects().add(cross);
		}
		else if(nodePidArray.size() > 1 && cross.getType() == 0){
			cross.changedFields().put("type", 1);
			
			result.getUpdateObjects().add(cross);
		}
		
		for(IRow row : cross.getNodes()){
			RdCrossNode node = (RdCrossNode)row;
			
			int nodePid = node.getNodePid();
			
			int index = nodePidArray.indexOf(nodePid);
			if( index == -1){
				result.getDelObjects().add(node);
			}
			else{
				nodePidArray.remove(index);
			}
		}

		for(int i=0;i<nodePidArray.size();i++){
			int nodePid = nodePidArray.getInt(i);
			
			RdCrossNode node = new RdCrossNode();
			
			node.setPid(cross.getPid());
			
			node.setNodePid(nodePid);
			
			node.setMesh(cross.mesh());
			
			result.getAddObjects().add(node);
		}
		
		RdLinkFormSelector selector = new RdLinkFormSelector(conn);
		
		for(IRow row : cross.getLinks()){
			RdCrossLink crosslink = (RdCrossLink)row;
			
			int linkPid = crosslink.getLinkPid();
			
			int index = linkPidArray.indexOf(linkPid);
			if( index == -1){
				result.getDelObjects().add(crosslink);
				
				//维护道路形态
				List<IRow> forms = selector.loadRowsByParentId(linkPid, true);
				
				boolean needDelete = true;
				IRow deleteRow = null;
				
				for(IRow formrow : forms){

					RdLinkForm form = (RdLinkForm)formrow;
					
					if(form.getFormOfWay() == 33){//环岛
						needDelete = false;
					}
					else if(form.getFormOfWay() == 50){ //交叉点内道路
						form.changedFields().put("formOfWay", 1);
						deleteRow = form;
					}
				}
				
				if(needDelete && deleteRow != null){
					
					if(forms.size() == 1){
						result.insertObject(deleteRow, ObjStatus.UPDATE);
					}
					else{
						result.insertObject(deleteRow, ObjStatus.DELETE);
					}
				}
			}
			else{
				linkPidArray.remove(index);
			}
		}
		
		for(int i=0;i<linkPidArray.size();i++){
			int linkPid = linkPidArray.getInt(i);
			
			RdCrossLink link = new RdCrossLink();
			
			link.setPid(cross.getPid());
			
			link.setLinkPid(linkPid);
			
			link.setMesh(cross.mesh());
			
			result.getAddObjects().add(link);
			
			//维护道路形态
			List<IRow> forms = selector.loadRowsByParentId(linkPid, true);
			
			boolean needAdd = true;
			
			IRow editRow = null;
			
			for(IRow formrow : forms){

				RdLinkForm form = (RdLinkForm)formrow;
				
				if(form.getFormOfWay() == 33){//环岛
					needAdd = false;
				}
				else if (form.getFormOfWay() == 1){
					form.changedFields().put("formOfWay", 50);
					
					editRow = form;
				}
			}
			
			if(needAdd){
				if(editRow != null){
					result.getUpdateObjects().add(editRow);
				}
				else{
					RdLinkForm form = new RdLinkForm();
					
					form.setLinkPid(linkPid);
					
					form.setMesh(cross.mesh());
					
					form.setFormOfWay(50);
					
					result.insertObject(form, ObjStatus.INSERT);
				}
			}
			
		}
		
		return null;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("nodePids") || content.containsKey("linkPids")) {
			return updateNodeLink(result);
		}
		else{
			return updateProperty(result);
		}

	}
	
}
