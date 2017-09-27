package com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresManoeuvre;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Command;
import com.navinfo.dataservice.engine.limit.operation.meta.scplateresmanoeuvre.delete.Operation;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresManoeuvreSearch;

public class Process extends AbstractProcess<Command> {
	public Process(AbstractCommand command) throws Exception {
		super(command);
	}

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresManoeuvreSearch search = new ScPlateresManoeuvreSearch(this.getConn());

        List<ScPlateresManoeuvre>  results = new ArrayList<>();
        
        for(int i = 0 ; i<this.getCommand().getManoeuvreId().size(); i++ ){
        	results.add(search.loadById(this.getCommand().getManoeuvreId().getInt(i),getCommand().getGroupId()));
        }
        
        this.getCommand().setManoeuvre(results);

        return true;
    }
    
	@Override
	public String exeOperation() throws Exception {
		return new Operation(this.getCommand()).run(this.getResult());
	}
}
