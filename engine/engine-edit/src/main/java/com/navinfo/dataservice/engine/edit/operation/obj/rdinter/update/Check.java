package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

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
	private void checkNodeDirect(Connection conn,String nodePisStr) throws Exception {

		RdNodeSelector selector = new RdNodeSelector(conn);

		List<Integer> loadRdNodeWays = selector.loadRdNodeWays(nodePisStr);

		if (loadRdNodeWays.contains(2)) {
			throw new Exception("图郭点不允许参与制作CRF交叉点");
		}
	}

	/**
	 * 检查link是否正确
	 * 
	 * @param linkList
	 * @throws Exception
	 */
	private void checkLinkAttribute(List<RdLink> linkList) throws Exception {
		if (CollectionUtils.isNotEmpty(linkList)) {
			for (RdLink link : linkList) {
				if (link.getImiCode() != 1 && link.getImiCode() != 2) {
					throw new Exception("link:"+link.getPid()+"不具有'I、M'属性");
				}
			}
		}
	}

	/**
	 * @param conn
	 * @param inter 
	 * @throws Exception
	 */
	public void hasNodeIsInter(Connection conn, RdInter inter) throws Exception {
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
		
		//检查点形态是否正确
		this.checkNodeDirect(conn,nodePids);
		
		List<RdLink> linkList = checkLinkIsCorrect(buf,inter,conn);
		
		//检查线的形态是否正确
		this.checkLinkAttribute(linkList);
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private List<RdLink> checkLinkIsCorrect(StringBuffer buf,RdInter inter,Connection conn) throws Exception {
		for(IRow row : inter.getNodes())
		{
			RdInterNode interNode = (RdInterNode) row; 
			
			buf.append(","+interNode.getNodePid());
		}
		
		String allNodePids = buf.toString();
				
		List<RdLink> linkList = new RdLinkSelector(conn).loadLinkPidByNodePids(allNodePids, true);
		
		//后台计算出需要新增的rd_inter_link集合
		List<RdLink> resultList = new ArrayList<>();
		
		for(RdLink link : linkList)
		{
			for(IRow row : inter.getLinks())
			{
				RdInterLink interLink = (RdInterLink) row;
				
				if(link.getPid() != interLink.getLinkPid())
				{
					resultList.add(link);
					break;
				}
			}
		}
		
		JSONArray linkArray = this.command.getLinkArray();
		
		JSONArray compareArray = new JSONArray();
		
		if(linkArray != null)
		{
			for (int i = 0; i < linkArray.size(); i++) {
				JSONObject json = linkArray.getJSONObject(i);

				if (json.containsKey("objStatus")) {

					if (ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {
						compareArray.add(json.getInt("linkPid"));
					}
				}
			}
		}
		
		checkLink(resultList,compareArray);
		
		return linkList;
	}
	
	/**
	 * 检查link参数正确性
	 * @param linkList link集合
	 * @throws Exception
	 */
	private void checkLink(List<RdLink> linkList,JSONArray linkArray) throws Exception {
		if(linkList != null && linkArray != null && linkArray.size()>0)
		{
			@SuppressWarnings("unchecked")
			List<Integer> linkPids = (List<Integer>) JSONArray.toCollection(linkArray);
			if(linkList.size() != linkPids.size())
			{
				new Exception("传递的link参数不正确:包含的link个数错误");
			}
			else
			{
				List<Integer> dbLinkPids = new ArrayList<>();
				
				for(RdLink link :linkList)
				{
					dbLinkPids.add(link.getPid());
				}
				
				if(!(linkPids.containsAll(dbLinkPids) && dbLinkPids.containsAll(linkPids)))
				{
					throw new Exception("传递的link参数不正确：link_pid错误");
				}
				else
				{
					this.checkLinkDirect(linkList);
				}
			}
		}
		else
		{
			throw new Exception("传递的link参数不正确：缺失link参数");
		}
	}
	
	/**
	 * 检查link是否正确
	 * @param linkList
	 * @throws Exception
	 */
	private void checkLinkDirect(List<RdLink> linkList) throws Exception
	{
		if(CollectionUtils.isNotEmpty(linkList))
		{
			for(RdLink link : linkList)
			{
				if(link.getImiCode() != 1 && link.getImiCode() !=2)
				{
					throw new Exception("link:"+link.getPid()+"不具有'I、M'属性,不允许制作");
				}
			}
		}
	}
}
