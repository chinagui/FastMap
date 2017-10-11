package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgroup.delete;

import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGroup;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.limit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.limit.search.meta.ScPlateresGroupSearch;

import java.util.ArrayList;
import java.util.List;

public class Process extends AbstractProcess<Command> {

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public boolean prepareData() throws Exception {

        ScPlateresGroupSearch search = new ScPlateresGroupSearch(this.getConn());

        List<ScPlateresGroup> groups = new ArrayList<>();

        for (String groupId : this.getCommand().getGroupIds()) {
            groups.add(search.loadById(groupId));
        }

        this.getCommand().setGroups(groups);

        return true;
    }

    @Override
    public String exeOperation() throws Exception {

        return new Operation(this.getCommand(),this.getConn()).run(this.getResult());
    }
}




