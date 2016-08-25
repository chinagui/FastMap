package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectInter;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
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
		
		rdObject.setPid(PidService.getInstance().applyRdObjectPid());
		
		rdObject.setGeometry(this.command.getPointGeo());
		
		JSONArray linkArray = this.command.getLinkArray();
		
		List<Integer> pidList = new ArrayList<>();
		
		if(linkArray != null)
		{
			//设置子表rd_object_link
			for(int i = 0; i<linkArray.size();i++)
			{
				int linkPid = linkArray.getInt(i);
				
				if(!pidList.contains(linkPid))
				{
					RdObjectLink objectLink = new RdObjectLink();
					
					objectLink.setLinkPid(linkPid);
					
					objectLink.setPid(rdObject.getPid());
					
					rdObject.getLinks().add(objectLink);
					
					pidList.add(linkPid);
				}
			}
		}
		
		JSONArray interArray = this.command.getInterArray();
		//设置子表rd_object_inter
		pidList.clear();
		for(int i = 0; i<interArray.size();i++)
		{
			int interPid = interArray.getInt(i);
			
			if(!pidList.contains(interPid))
			{
				RdObjectInter objectInter = new RdObjectInter();
				
				objectInter.setPid(rdObject.getPid());
				
				objectInter.setInterPid(interPid);
				
				rdObject.getInters().add(objectInter);
				
				pidList.add(interPid);
			}
		}
		
		pidList.clear();
		JSONArray roadArray = this.command.getRoadArray();
		//设置子表rd_object_road
		for(int i = 0; i<roadArray.size();i++)
		{
			int roadPid = roadArray.getInt(i);
			
			if(!pidList.contains(roadPid))
			{
				RdObjectRoad objectRoad = new RdObjectRoad();
				
				objectRoad.setPid(rdObject.getPid());
				
				objectRoad.setRoadPid(roadPid);
				
				rdObject.getRoads().add(objectRoad);
				
				pidList.add(roadPid);
			}
		}
		
		result.insertObject(rdObject, ObjStatus.INSERT, rdObject.getPid());
	}
}
