package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Check {

	private Command command;

	public Check(Command command) {
		this.command = command;
	}

	/**
	 * 检查node是否正确
	 * 
	 * @param conn
	 * @throws Exception
	 */
	public void checkNodeDirect(Connection conn) throws Exception {

		String nodePids = JsonUtils.getStringValueFromJSONArray(this.command.getNodeArray());
		
		if(StringUtils.isEmpty(nodePids))
		{
			return;
		}
		
		RdNodeSelector selector = new RdNodeSelector(conn);

		Map<Integer, String> loadRdNodeWays = selector.loadRdNodeWays(nodePids);

		for (Map.Entry<Integer, String> entry : loadRdNodeWays.entrySet()) {
			int nodePid = entry.getKey();

			String forms = entry.getValue();

			List<String> formList = Arrays.asList(forms.split(","));

			if (formList.contains("2")) {
				this.command.getNodeArray().remove(nodePid);
			}
		}
	}

	/**
	 * @param conn
	 * @param inter
	 * @throws Exception
	 */
	public void hasNodeIsInter(Connection conn, RdInter inter) throws Exception {

		// 检查点形态是否正确
		this.checkNodeDirect(conn);

		RdInterSelector selector = new RdInterSelector(conn);

		StringBuffer buf = new StringBuffer();

		JSONArray nodeArray = this.command.getNodeArray();

		for (int i = 0; i < nodeArray.size(); i++) {
			JSONObject json = nodeArray.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
					buf.append(json.getInt("nodePid") + ",");
				}
			}
		}

		String nodePids = buf.deleteCharAt(buf.lastIndexOf(",")).toString();

		List<Integer> interPidList = selector.loadInterPidByNodePid(nodePids, false);

		if (CollectionUtils.isNotEmpty(interPidList)) {
			throw new Exception("新增的点位已包含crf交叉点");
		}

		checkLinkIsCorrect(buf, inter, conn);

	}

	/**
	 * @throws Exception
	 * 
	 */
	private List<RdLink> checkLinkIsCorrect(StringBuffer buf, RdInter inter, Connection conn) throws Exception {
		for (IRow row : inter.getNodes()) {
			RdInterNode interNode = (RdInterNode) row;

			buf.append("," + interNode.getNodePid());
		}

		String allNodePids = buf.toString();

		List<RdLink> linkList = new RdLinkSelector(conn).loadLinkPidByNodePids(allNodePids, true);

		// 后台计算出需要新增的rd_inter_link集合
		List<RdLink> resultList = new ArrayList<>();

		for (RdLink link : linkList) {
			for (IRow row : inter.getLinks()) {
				RdInterLink interLink = (RdInterLink) row;

				if (link.getPid() != interLink.getLinkPid()) {
					resultList.add(link);
					break;
				}
			}
		}

		JSONArray linkArray = this.command.getLinkArray();

		JSONArray compareArray = new JSONArray();

		if (linkArray != null) {
			for (int i = 0; i < linkArray.size(); i++) {
				JSONObject json = linkArray.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
						compareArray.add(json.getInt("linkPid"));
					}
				}
			}
		}
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Check check = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create.Check();
		
		check.checkLinkDirect(resultList);

		return linkList;
	}
	
	/**
	 * 检查link参数正确性
	 * 
	 * @param linkList
	 *            link集合
	 * @throws Exception
	 */
	public void checkLink(List<RdLink> linkList, JSONArray linkArray) throws Exception {
		if (linkList != null && linkArray != null && linkArray.size() > 0) {
			@SuppressWarnings("unchecked")
			List<Integer> linkPids = (List<Integer>) JSONArray.toCollection(linkArray);
			if (linkList.size() != linkPids.size()) {
				throw new Exception("传递的link参数不正确:包含的link个数错误");
			} else {
				List<Integer> dbLinkPids = new ArrayList<>();

				for (RdLink link : linkList) {
					dbLinkPids.add(link.getPid());
				}

				if (!(linkPids.containsAll(dbLinkPids) && dbLinkPids.containsAll(linkPids))) {
					throw new Exception("传递的link参数不正确：link_pid错误");
				}
			}
		} else {
			throw new Exception("传递的link参数不正确：缺失link参数");
		}
	}

}
