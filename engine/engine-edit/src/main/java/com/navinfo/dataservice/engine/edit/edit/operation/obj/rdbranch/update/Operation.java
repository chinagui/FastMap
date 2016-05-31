package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	public Operation(Command command, RdBranch branch) {
		this.command = command;

		this.branch = branch;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 判断是否存在交限进入线
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(branch, ObjStatus.DELETE, branch.pid());
				
				return null;
			} else {

				boolean isChanged = branch.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
				}
			}
		}

		if (content.containsKey("details")) {
			JSONArray details = content.getJSONArray("details");

			for (int i = 0; i < details.size(); i++) {

				JSONObject json = details.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString()
							.equals(json.getString("objStatus"))) {

						RdBranchDetail detail = branch.detailMap
								.get(json.getInt("pid"));
						
						if (detail == null){
							throw new Exception("detailId="+json.getInt("pid")+"的rd_branch_detail不存在");
						}

						if (ObjStatus.DELETE.toString().equals(json
								.getString("objStatus"))) {
							result.insertObject(detail, ObjStatus.DELETE, branch.getPid());
							
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(json
								.getString("objStatus"))) {

							boolean isChanged = detail
									.fillChangeFields(json);

							if (isChanged) {
								result.insertObject(detail, ObjStatus.UPDATE, branch.pid());
							}
						}
					} else {
						RdBranchDetail detail = new RdBranchDetail();
						
						detail.Unserialize(json);
						
						detail.setPid(PidService.getInstance().applyBranchDetailId());
						
						detail.setBranchPid(branch.getPid());
						
						detail.setMesh(branch.mesh());
						
						result.insertObject(detail, ObjStatus.INSERT, branch.pid());
						
						continue;
					}
				}

				if (json.containsKey("names")) {
					
					int detailId = json.getInt("pid");
					
					RdBranchDetail detail = branch.detailMap
							.get(detailId);

					JSONArray names = json.getJSONArray("names");

					for (int j = 0; j < names.size(); j++) {
						JSONObject cond = names.getJSONObject(j);

						if (!cond.containsKey("objStatus")) {
							throw new Exception(
									"传入请求内容格式错误，conditions不存在操作类型objStatus");
						}

						if (!ObjStatus.INSERT.toString()
								.equals(cond.getString("objStatus"))) {

							RdBranchName name = detail.nameMap.get(cond.getInt("pid"));
							
							if (name == null){
								throw new Exception("pid="+cond.getInt("pid")+"的rd_branch_name不存在");
							}

							if (ObjStatus.DELETE.toString().equals(cond
									.getString("objStatus"))) {
								result.insertObject(name, ObjStatus.DELETE, branch.getPid());
								
							} else if (ObjStatus.UPDATE.toString().equals(cond
									.getString("objStatus"))) {

								boolean isChanged = name
										.fillChangeFields(cond);

								if (isChanged) {
									result.insertObject(name, ObjStatus.UPDATE, branch.pid());
								}
							}
						} else {
							RdBranchName name = new RdBranchName();
							
							name.Unserialize(cond);
							
							name.setDetailId(detailId);
							
							name.setMesh(branch.mesh());
							
							name.setPid(PidService.getInstance().applyBranchNameId());
							
							result.insertObject(name, ObjStatus.INSERT, branch.pid());
							
						}
					
					}

				}
			}
		}

		return null;
	}

}
