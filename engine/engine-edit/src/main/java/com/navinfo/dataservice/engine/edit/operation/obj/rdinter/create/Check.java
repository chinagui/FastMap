package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
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
	
	public void checkLink(Connection conn) throws Exception {
		
		@SuppressWarnings("unchecked")
		List<Integer> linkPids = (List<Integer>) JSONArray.toCollection(command.getLinkArray());
		
		List<RdLink> linkList = new RdLinkSelector(conn).loadByPids(linkPids, true);
		
		//检查link形态
		checkLinkDirect(linkList);
			
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
		
		Map<Integer,String> loadRdNodeWays = selector.loadRdNodeWays(nodePids);
		
		for(Map.Entry<Integer, String> entry : loadRdNodeWays.entrySet())
		{
			int nodePid = entry.getKey();
			
			String forms = entry.getValue();
			
			List<String> formList = Arrays.asList(forms.split(","));
			
			if(formList.contains("2"))
			{
				this.command.getNodeArray().remove(new Integer(nodePid));
			}
		}
	}
	
	/**
	 * 检查link是否正确
	 * @param linkList
	 * @throws Exception
	 */
	public void checkLinkDirect(List<RdLink> linkList) throws Exception
	{
		if(CollectionUtils.isNotEmpty(linkList))
		{
			for(RdLink link : linkList)
			{
				//特殊交通可以制作
				if(link.getSpecialTraffic() == 1)
				{
					return;
				}
				else if(link.getImiCode() != 1 && link.getImiCode() !=2)
				{
					boolean has33Form = false;
					List<IRow> linkForms = link.getForms();
					for(IRow row : linkForms)
					{
						RdLinkForm form = (RdLinkForm) row;
						if(form.getFormOfWay() == 33)
						{
							has33Form = true;
							break;
						}
					}
					//非特殊交通也不包含环岛属性报log
					if(!has33Form)
					{
						throw new Exception("CRFI中的Link："+link.getPid()+"无IMI属性且无环岛或特殊交通类型[GLM28009]");
					}
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
