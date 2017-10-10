package com.navinfo.dataservice.engine.limit.operation.limit.scplatereslink.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresLink;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresLinkSearch;

public class Process extends AbstractProcess<Command>{

	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
    @Override
    public boolean prepareData() throws Exception {

        ScPlateresLinkSearch search = new ScPlateresLinkSearch(this.getConn());

        List<ScPlateresLink> results = search.loadByIds(this.getCommand().getGeometryIds());
        
        this.getCommand().setscplateresLinks(results);

        return true;
    }
    
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}

}
