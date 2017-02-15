package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * 
 * @ClassName: Process
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:38:48
 * @Description: TODO
 */
public class Process extends AbstractProcess<Command> implements IProcess {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

	@Override
	public boolean prepareData() throws Exception {

		this.getCommand().setRdInter((RdInter) new AbstractSelector(RdInter.class, getConn()).loadById(this.getCommand().getPid(),true));

		return true;
	}
	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub
		// this.createPostCheckGlmList();
		List<IRow> glmList = new ArrayList<IRow>();
		glmList.addAll(this.getResult().getAddObjects());
		glmList.addAll(this.getResult().getUpdateObjects());
		//有检查项需要查删除的RdInter
		for(IRow irow:this.getResult().getDelObjects()){
			if(irow instanceof RdInter){
				glmList.add(irow);
			}
		}
		this.checkCommand.setGlmList(glmList);
		this.checkEngine.postCheck();

	}
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
	}

}
