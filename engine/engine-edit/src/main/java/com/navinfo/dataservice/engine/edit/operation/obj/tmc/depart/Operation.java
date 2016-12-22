package com.navinfo.dataservice.engine.edit.operation.obj.tmc.depart;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocation;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdTmclocationLink;

/**
 * 上下线分离维护TMC信息
* @ClassName: Operation 
* @author Zhang Xiaolong
* @date 2016年12月20日 上午10:46:25 
* @Description: TODO
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    /**
     * 维护上下线分离对TMC的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Map<Integer, RdLink> leftLinks, Map<Integer, RdLink> rightLinks, Result result) throws Exception {
        for (RdLink link : links) {
            updateTmcLocation(link.getTmclocations(),link,leftLinks.get(link.getPid()),rightLinks.get(link.getPid()),result);
        }
        return "";
    }

    /**
     * 上下线分离更新tmc匹配关系
     * @param tmcLocations
     * @param originLink
     * @param leftLink
     * @param rightLink
     * @param result
     */
    private void updateTmcLocation(List<IRow> tmcLocations, RdLink originLink,RdLink leftLink, RdLink rightLink, Result result) {
    	int originLinkPid = originLink.getPid();
    	
    	int originDirect = originLink.getDirect();
    	
        for (IRow row : tmcLocations) {
            RdTmclocation rdTmclocation = (RdTmclocation) row;
            for(IRow tmcLinkRow : rdTmclocation.getLinks())
            {
            	RdTmclocationLink link = (RdTmclocationLink) tmcLinkRow;
            	//找到原link的tmc信息，赋值给分离后的同方向的link
            	if(link.getLinkPid() == originLinkPid)
            	{
            		if(leftLink.getDirect() == originDirect)
            		{
            			link.changedFields().put("linkPid", leftLink.getPid());
            			
            			result.insertObject(link, ObjStatus.UPDATE, link.getGroupId());
            		}
            		else if(rightLink.getDirect() == originDirect)
            		{
            			link.changedFields().put("linkPid", rightLink.getPid());
            			
            			result.insertObject(link, ObjStatus.UPDATE, link.getGroupId());
            		}
            		break;
            	}
            }
        }
    }

}
