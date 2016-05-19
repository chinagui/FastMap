package com.navinfo.dataservice.engine.check.rules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.engine.check.core.baseRule;
import com.navinfo.dataservice.engine.check.graph.HashSetRdLinkAndPid;

/*
 * GLM01205	Link信息	大陆环岛检查	形态	一组含“环岛”属性的link组成的link链上的所有link，道路功能等级必须相同。	环岛的功能等级不同
 */
public class GLM01205 extends baseRule {

	public GLM01205() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void preCheck(CheckCommand checkCommand) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void postCheck(CheckCommand checkCommand) throws Exception {
		List<Integer> linkPidList=new ArrayList<Integer>();
		for(IRow obj : checkCommand.getGlmList()){
			if (obj instanceof RdLink){
				RdLink rdLink = (RdLink)obj;
				//非环岛link不查此规则
				List<IRow> forms=rdLink.getForms();
				if(forms.size()==0){continue;}
				boolean isHuandao=false;
				for(int i=0;i<forms.size();i++){
					RdLinkForm form=(RdLinkForm) forms.get(i);
					if(form.getFormOfWay()==33){isHuandao=true;}
				}
				if(!isHuandao){continue;}
				//一条环岛link链上的link不重复检查
				if(linkPidList.contains(rdLink.getPid())){continue;}
				
				//获取rdLink对应的链
				HashSetRdLinkAndPid huandaoChain=getLoader().loadHandaoChain(getConn(), rdLink);
				
				linkPidList.removeAll(huandaoChain.getRdLinkPidSet());
				linkPidList.addAll(huandaoChain.getRdLinkPidSet());
				
				int fc=rdLink.getFunctionClass();
				Iterator<RdLink> huandaoIterator=huandaoChain.iterator();
				String target="";
				boolean isError=false;
				while(huandaoIterator.hasNext()){
					RdLink linkObj=huandaoIterator.next();
					if(!target.isEmpty()){target=target+";";}
					target=target+"[RD_LINK,"+linkObj.getPid()+"]";
					if(fc!=linkObj.getFunctionClass()){isError=true;}
				}
				if(isError){this.setCheckResult(rdLink.getGeometry(), target, rdLink.getMeshId());}
			}
		}
	}

}
