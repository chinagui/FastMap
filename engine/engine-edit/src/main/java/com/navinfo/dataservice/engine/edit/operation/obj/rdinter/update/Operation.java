package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;
	
	private RdInter rdInter;
	
	public Operation(Command command) {
		this.command = command;
		this.rdInter = this.command.getRdInter();
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		//不编辑主表信息
		
		//node子表
		if (content.containsKey("nodes")) {
			updateNode(result,content);
		}
		
		//link子表
		if(content.containsKey("links"))
		{
			updateLink(result,content);
		}

		return null;
	}

	/**
	 * 跟新rd_inter_node子表
	 * @param result
	 * @param content
	 * @throws Exception 
	 */
	private void updateNode(Result result, JSONObject content) throws Exception{
		JSONArray subObj = content.getJSONArray("nodes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdInterNode row = rdInter.nodeMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的RdInterNode不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, rdInter.pid());
						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									rdInter.pid());
						}
					}
				} else {
					RdInterNode rdInterNode = new RdInterNode();
					
					rdInterNode.setNodePid(json.getInt("nodePid"));
					
					rdInterNode.setPid(rdInter.getPid());

					result.insertObject(rdInterNode, ObjStatus.INSERT, rdInterNode.getPid());
				}
			}
		}
	}
	
	/**更新link子表
	 * @param result
	 * @param content
	 * @throws Exception 
	 */
	private void updateLink(Result result, JSONObject content) throws Exception {
		JSONArray subObj = content.getJSONArray("links");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdInterLink row = rdInter.linkMap.get(json
							.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId")
								+ "的RdInterLink不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, rdInter.pid());
						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE,
									rdInter.pid());
						}
					}
				} else {
					RdInterLink rdInterLink = new RdInterLink();
					
					rdInterLink.setLinkPid(json.getInt("linkPid"));
					
					rdInterLink.setPid(rdInter.getPid());

					result.insertObject(rdInterLink, ObjStatus.INSERT, rdInterLink.getPid());
				}
			}
		}
	}
}
