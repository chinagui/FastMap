package com.navinfo.dataservice.engine.edit.operation.obj.rdtollgate.update;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgate;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgateName;
import com.navinfo.dataservice.dao.glm.model.rd.tollgate.RdTollgatePassage;
import com.navinfo.dataservice.dao.glm.selector.rd.tollgate.RdTollgateSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Title: Operation.java
 * @Description: TODO
 * @author zhangyt
 * @date: 2016年8月10日 下午2:41:31
 * @version: v1.0
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}
	public Operation(Command command,Connection conn) {
		this.command = command;
		this.conn = conn;
	}
	@Override
	public String run(Result result) throws Exception {
		RdTollgate tollgate = this.command.getTollgate();
		JSONObject content = this.command.getContent();
		boolean isChange = tollgate.fillChangeFields(content);
		if (isChange) {
			result.insertObject(tollgate, ObjStatus.UPDATE, tollgate.pid());
			if(content.containsKey("passageNum")){
				this.caleRdlaneForRdTollgate(result,content.getInt("passageNum"));
			}
		}
		result.setPrimaryPid(tollgate.pid());

		if (content.containsKey("passages")) {
			updatePassage(result, content.getJSONArray("passages"));
		}

		if (content.containsKey("names")) {
			updateName(result, content.getJSONArray("names"));
		}

		return null;
	}

	private void updatePassage(Result result, JSONArray array) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = array.iterator();
		RdTollgatePassage passage = null;
		JSONObject jsonPassage = null;
		while (iterator.hasNext()) {
			jsonPassage = iterator.next();
			if (jsonPassage.containsKey("objStatus")) {
				String objStatus = jsonPassage.getString("objStatus");
				passage = this.command.getTollgate().tollgatePassageMap
						.get(jsonPassage.getString("rowId"));
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = passage.fillChangeFields(jsonPassage);
					if (isChange) {
						result.insertObject(passage, ObjStatus.UPDATE,
								passage.getPid());
					}
				} else if (ObjStatus.DELETE.toString().equals(objStatus)) {
					result.insertObject(passage, ObjStatus.DELETE,
							passage.getPid());
				} else if (ObjStatus.INSERT.toString().equals(objStatus)) {
					passage = new RdTollgatePassage();
					passage.setPid(this.command.getTollgate().getPid());
					result.insertObject(passage, ObjStatus.INSERT,
							passage.getPid());
				}
			}
		}

	}

	private void updateName(Result result, JSONArray array) throws Exception {
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> iterator = array.iterator();
		RdTollgateName name = null;
		JSONObject jsonName = null;
		while (iterator.hasNext()) {
			jsonName = iterator.next();
			if (jsonName.containsKey("objStatus")) {
				String objStatus = jsonName.getString("objStatus");
				name = this.command.getTollgate().tollgateNameMap.get(jsonName
						.getString("rowId"));
				if (ObjStatus.UPDATE.toString().equals(objStatus)) {
					boolean isChange = name.fillChangeFields(jsonName);
					if (isChange) {
						result.insertObject(name, ObjStatus.UPDATE,
								name.getNameId());
					}
				} else if (ObjStatus.DELETE.toString().equals(objStatus)) {
					result.insertObject(name, ObjStatus.DELETE,
							name.getNameId());
				} else if (ObjStatus.INSERT.toString().equals(objStatus)) {
					name = new RdTollgateName();
					name.setNameId(PidService.getInstance()
							.applyRdTollgateNamePid());
					name.setPid(this.command.getTollgate().getPid());
					result.insertObject(name, ObjStatus.INSERT,
							name.getNameId());
				}
			}
		}
	}

	/**
	 * 根据被删除的RdLink的Pid、新生成的RdLink<br>
	 * 维护原RdLink上关联的收费站
	 * 
	 * @param result
	 *            待处理的结果集
	 * @param oldLink
	 *            被删除RdLink的Pid
	 * @param newLinks
	 *            新生成的RdLink的集合
	 * @return
	 * @throws Exception
	 */
	public String breakRdTollgate(Result result, int oldLinkPid,
			List<RdLink> newLinks) throws Exception {
		RdTollgateSelector selector = new RdTollgateSelector(this.conn);
		// 查询所有与被删除RdLink关联的收费站
		List<RdTollgate> rdTollgates = selector.loadRdTollgatesWithLinkPid(
				oldLinkPid, true);
		// 循环处理每一个收费站
		for (RdTollgate rdTollgate : rdTollgates) {
			// 收费站的进入点的Pid
			int nodePid = rdTollgate.getNodePid();
			for (RdLink link : newLinks) {
				// 如果新生成线的起点的Pid与收费站的nodePid相等
				// 则该新生成线为退出线，修改退出线Pid
				if (nodePid == link.getsNodePid()) {
					rdTollgate.changedFields().put("outLinkPid", link.pid());
					break;
					// 如果新生成线的终点的Pid与收费站的nodePid相等
					// 则该新生成线为进入线，修改进入线Pid
				} else if (nodePid == link.geteNodePid()) {
					rdTollgate.changedFields().put("inLinkPid", link.pid());
					break;
				}
			}
			// 将需要修改的收费站放入结果集中
			result.insertObject(rdTollgate, ObjStatus.UPDATE, rdTollgate.pid());
		}
		return null;
	}
	/**
	 * 修改收费站维护车道信息
	 * @param result
	 * @param passageNum
	 * @throws Exception
	 */
	private void caleRdlaneForRdTollgate(Result result,int passageNum) throws Exception {
       if(passageNum > 0 ){
    	   com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation operation = new com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.Operation(
					conn);
			operation.setTollgate(this.command.getTollgate());
			operation.setPassageNum(passageNum);
			operation.refRdLaneForTollgate(result);
       }
	}
}
