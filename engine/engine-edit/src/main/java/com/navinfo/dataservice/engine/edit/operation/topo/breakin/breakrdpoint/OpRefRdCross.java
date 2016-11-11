package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 如果原始link为路口内组成link，分割link新生成的NODE应加入路口子点中，
 * 如果此路口的信号灯字段为“无路口红绿灯”或“有行人红绿灯”，则不影响信号灯记录； 如果此路口的信号灯字段为“有路口红绿灯”，则程序自动维护信号灯记录，
 * 具体维护原则见“修改道路路口节点”中“增加节点导致增加组成link”部分对信号灯的维护；
 * 
 * @ClassName: OpRefRdCross
 * @author Zhang Xiaolong
 * @date 2016年10月31日 上午10:01:58
 * @Description: TODO
 */
public class OpRefRdCross implements IOperation {

	private Command command;

	private Connection conn;

	public OpRefRdCross(Command command, Connection connection) {
		this.command = command;

		this.conn = connection;
	}

	@Override
	public String run(Result result) throws Exception {

		boolean isCrossLink = false;

		RdLink breakLink = this.command.getBreakLink();

		for (IRow row : breakLink.getForms()) {
			RdLinkForm form = (RdLinkForm) row;

			if (form.getFormOfWay() == 50) {
				isCrossLink = true;
				break;
			}
		}

		if (isCrossLink) {
			RdCrossSelector crossSelector = new RdCrossSelector(conn);

			List<Integer> linkPid = new ArrayList<>();

			linkPid.add(breakLink.getPid());

			List<RdCross> crossList = crossSelector.loadRdCrossByNodeOrLink(null, linkPid, true);
			
			//是路口内link的，需要新增路口点和路口组成link，删除原路口组成link
			if (CollectionUtils.isNotEmpty(crossList)) {
				// 新增路口点 rd_cross_node
				RdCross cross = crossList.get(0);
				
				JSONArray breakNodeArray = command.getBreakNodes();
				
				for(int i = 0;i<breakNodeArray.size();i++)
				{
					JSONObject  obj = breakNodeArray.getJSONObject(i);
					
					RdCrossNode crossNode = new RdCrossNode();

					crossNode.setPid(cross.getPid());

					crossNode.setNodePid(obj.getInt("nodePid"));

					result.insertObject(crossNode, ObjStatus.INSERT, crossNode.getPid());
				}
				
				List<RdLink> newLinks = command.getNewLinks();
				
				for(RdLink link : newLinks)
				{
					// 新增路口组成link rd_cross_link
					RdCrossLink crossLink1 = new RdCrossLink();

					crossLink1.setPid(cross.getPid());

					crossLink1.setLinkPid(link.getPid());

					result.insertObject(crossLink1, ObjStatus.INSERT, crossLink1.getPid());
				}
				
				//删除原路口组成link
				
				for(RdCross crs : crossList)
				{
					List<IRow> links = crs.getLinks();
					
					for(IRow row : links)
					{
						RdCrossLink crosLink = (RdCrossLink) row;
						
						if(crosLink.getLinkPid() == breakLink.getPid())
						{
							result.insertObject(crosLink, ObjStatus.DELETE, crosLink.getPid());
							//打断前只有一条，找到后跳出循环，提高维护效率
							break;
						}
					}
				}
			}
		}

		return null;
	}
}
