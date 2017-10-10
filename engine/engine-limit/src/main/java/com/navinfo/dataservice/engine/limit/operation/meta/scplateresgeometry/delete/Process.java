package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.delete;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGeometrySearch;

import java.util.ArrayList;
import java.util.List;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresGeometrySearch search = new ScPlateresGeometrySearch(this.getConn());

        List<ScPlateresGeometry> geometrys = new ArrayList<>();

        for (String groupId : this.getCommand().getIds()) {
            geometrys.add(search.loadById(groupId));
        }

        this.getCommand().setGeometrys(geometrys);

        return true;
    }

    @Override
    public String exeOperation() throws Exception {

        return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
    }
}




