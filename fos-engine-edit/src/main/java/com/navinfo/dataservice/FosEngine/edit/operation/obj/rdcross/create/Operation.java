package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdcross.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCross;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossLink;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.cross.RdCrossNode;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLinkForm;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkFormSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.service.PidService;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		RdCross cross = new RdCross();

		cross.setPid(PidService.getInstance().applyRdCrossPid());

		result.setPrimaryPid(cross.getPid());
		
		if (command.getNodePids().size() > 1){
			cross.setType(1);
		}
		
		List<IRow> nodes = new ArrayList<IRow>();
		
		List<Integer> nodePids = command.getNodePids();

		for (int i=0; i<nodePids.size();i++) {
			
			int nodePid = nodePids.get(i);

			RdCrossNode node = new RdCrossNode();

			node.setPid(cross.getPid());
			
			node.setNodePid(nodePid);
			
			if(i == 0 && nodePids.size() > 1){
				node.setIsMain(1);
			}
			
			nodes.add(node);
		}
		
		cross.setNodes(nodes);
		
		List<IRow> links = new ArrayList<IRow>();
		
		List<Integer> linkPids = command.getLinkPids();
		
		RdLinkFormSelector selector = new RdLinkFormSelector(conn);
		
		for (int i=0; i<linkPids.size();i++) {
			
			int linkPid = linkPids.get(i);

			RdCrossLink link = new RdCrossLink();

			link.setPid(cross.getPid());
			
			link.setLinkPid(linkPid);

			links.add(link);
			
			//维护道路形态
			List<IRow> forms = selector.loadRowsByParentId(linkPid, true);
			
			boolean needAdd = true;
			
			IRow editRow = null;
			
			for(IRow formrow : forms){

				RdLinkForm form = (RdLinkForm)formrow;
				
				if(form.getFormOfWay() == 33){//环岛
					needAdd = false;
				}
				else if (form.getFormOfWay() == 1){
					form.changedFields().put("formOfWay", 50);
					
					editRow = form;
				}
			}
			
			if(needAdd){
				if(editRow != null){
					result.getUpdateObjects().add(editRow);
				}
				else{
					RdLinkForm form = new RdLinkForm();
					
					form.setFormOfWay(50);
					
					form.setLinkPid(linkPid);
					
					result.getAddObjects().add(form);
				}
			}
		}
		
		cross.setLinks(links);

		result.insertObject(cross, ObjStatus.INSERT);

		return null;
	}


}
