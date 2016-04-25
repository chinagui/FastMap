package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmingroup.update;

import net.sf.json.JSONObject;

import org.json.JSONArray;

import com.google.gson.Gson;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdminTree;

public class Command implements ICommand {

	private String requester;

	private int projectId;

	private JSONObject content;

	private JSONArray groupTree;

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDRESTRICTION;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");

		this.content = json.getJSONObject("data");

		//groupTree = content.getJSONArray("groupTree");

		//parseJson2Bean(groupTree.getJSONObject(0).toString());

	}

	private static  AdAdminTree parseJson2Bean(String jsonArray) {
		AdAdminTree tree = null;

		Gson gson = new Gson();
		tree = gson.fromJson(jsonArray, AdAdminTree.class);

		return tree;
	}

	public static void main(String[] args) throws Exception {
		String jsonArray = "[{\"regionId\":1273,\"name\":\"中国大陆\",\"group\":{\"groupId\":248,"
				+ "\"regionIdUp\":1273,\"rowId\":\"2D71EFCB1966DCE7E050A8C083040693\"},"
				+ "\"children\":[{\"regionId\":163,\"name\":\"北京市\",\"group\":{\"groupId\":40,"
				+ "\"regionIdUp\":163,\"rowId\":\"2D71EFCB16D7DCE7E050A8C083040693\"},\"part\":{\"groupId\":248,\"regionIdDown\":163,\"rowId\":\"2D71EFCB56BEDCE7E050A8C083040693\"},\"children\":[{\"regionId\":580,\"name\":\"北京市\",\"group\":{\"groupId\":114,\"regionIdUp\":580,\"rowId\":\"2D71EFCB1711DCE7E050A8C083040693\"},\"part\":{\"groupId\":40,\"regionIdDown\":580,\"rowId\":\"2D71EFCB642CDCE7E050A8C083040693\"},\"children\":[{\"regionId\":1421,\"name\":\"北京市区\",\"group\":{\"groupId\":286,\"regionIdUp\":1421,\"rowId\":\"2D71EFCB179FDCE7E050A8C083040693\"},\"part\":{\"groupId\":114,\"regionIdDown\":1421,\"rowId\":\"2D71EFCB679CDCE7E050A8C083040693\"}}]}]}]}]";
		JSONArray array = new JSONArray(jsonArray);
		AdAdminTree tree = parseJson2Bean(array.get(0).toString());
		System.out.println(tree.Serialize(ObjLevel.BRIEF));
	}
}
