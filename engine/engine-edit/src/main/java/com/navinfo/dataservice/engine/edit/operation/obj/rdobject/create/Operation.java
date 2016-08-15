package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.pidservice.PidService;

import net.sf.json.JSONArray;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		createRdObject(result);

		return msg;
	}

	/**
	 * @param nodeLinkPidMap
	 * @throws Exception
	 */
	private void createRdObject(Result result) throws Exception {
		
		RdObject rdObject = new RdObject();
		
		rdObject.setPid(PidService.getInstance().applyRdInterPid());
		
		JSONArray linkArray = this.command.getLinkArray();
		if(linkArray != null)
		{
			//设置子表rd_object_link
			for(int i = 0; i<linkArray.size();i++)
			{
				int linkPid = linkArray.getInt(i);
				
				RdObjectLink objectLink = new RdObjectLink();
				
				objectLink.setLinkPid(linkPid);
				
				objectLink.setPid(rdObject.getPid());
				
				rdObject.getLinks().add(objectLink);
			}
		}
		
		JSONArray interArray = this.command.getInterArray();
		//设置子表rd_object_inter
		for(int i = 0; i<interArray.size();i++)
		{
			int interPid = interArray.getInt(i);
			
			RdObjectInter objectInter = new RdObjectInter();
			
			objectInter.setPid(rdObject.getPid());
			
			objectInter.setInterPid(interPid);
			
			rdObject.getInters().add(objectInter);
		}
		
		JSONArray roadArray = this.command.getRoadArray();
		//设置子表rd_object_road
		for(int i = 0; i<roadArray.size();i++)
		{
			int roadPid = roadArray.getInt(i);
			
			RdObjectRoad objectRoad = new RdObjectRoad();
			
			objectRoad.setPid(rdObject.getPid());
			
			objectRoad.setRoadPid(roadPid);
			
			rdObject.getRoads().add(objectRoad);
		}
		
		result.insertObject(rdObject, ObjStatus.INSERT, rdObject.getPid());
	}
}
