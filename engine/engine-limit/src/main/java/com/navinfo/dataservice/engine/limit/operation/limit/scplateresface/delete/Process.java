package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.delete;

import java.util.List;

import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.limit.ScPlateresFaceSearch;


public class Process extends AbstractProcess<Command>{
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}
	
    @Override
    public boolean prepareData() throws Exception {

        ScPlateresFaceSearch search = new ScPlateresFaceSearch(this.getConn());

        List<ScPlateresFace> results = search.loadByIds(this.getCommand().getGeometryIds());
        
        this.getCommand().setscplateresFaces(results);
        return true;
    }
    
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
}
