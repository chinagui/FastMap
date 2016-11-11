package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;

import net.sf.json.JSONArray;

public class Check {
	
	private Command command;
	
	public Check(Command command)
	{
		this.command = command;
	}
	
	public void checkLinkByNode(Connection conn) throws Exception {
		
		String nodePids = JsonUtils.getStringValueFromJSONArray(this.command.getNodeArray());
		
		List<RdLink> linkList = new RdLinkSelector(conn).loadLinkPidByNodePids(nodePids, true);
		
		//检查link参数正确性
		checkLink(linkList);
			
	}
	
	/**
	 * 检查link参数正确性
	 * @param linkList link集合
	 * @throws Exception
	 */
	private void checkLink(List<RdLink> linkList) throws Exception {
		if(linkList != null && command.getLinkArray() != null && command.getLinkArray().size()>0)
		{
			@SuppressWarnings("unchecked")
			List<Integer> linkPids = (List<Integer>) JSONArray.toCollection(command.getLinkArray());
			if(linkList.size() != linkPids.size())
			{
				throw new Exception("传递的link参数不正确:包含的link个数错误");
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
		if(CollectionUtils.isNotEmpty(linkList) && ((command.getLinkArray() == null)||(command.getLinkArray().size() == 0)))
		{
			throw new Exception("传递的link参数不正确：缺失link参数");
		}
	}

	/**
	 * 检查node是否正确
	 * @param conn
	 * @throws Exception
	 */
	public void checkNodeDirect(Connection conn) throws Exception
	{
		String nodePids = JsonUtils.getStringValueFromJSONArray(this.command.getNodeArray());
		
		RdNodeSelector selector = new RdNodeSelector(conn);
		
		List<Integer> loadRdNodeWays = selector.loadRdNodeWays(nodePids);
		
		if(loadRdNodeWays.contains(2))
		{
			throw new Exception("图郭点不允许参与制作CRF交叉点");
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

	/**
	 * @param conn
	 * @throws Exception 
	 */
	public void hasRdInter(Connection conn) throws Exception {
		RdInterSelector selector = new RdInterSelector(conn);
		
		String nodePids = JsonUtils.getStringValueFromJSONArray(this.command.getNodeArray());
		
		if(StringUtils.isEmpty(nodePids))
		{
			throw new Exception("CRF交叉点制作参数必须包含node");
		}
		
		List<Integer> interPidList = selector.loadInterPidByNodePid(nodePids, false);
		
		if(CollectionUtils.isNotEmpty(interPidList))
		{
			throw new Exception("所选点位已包含crf交叉点");
		}
	}
}
