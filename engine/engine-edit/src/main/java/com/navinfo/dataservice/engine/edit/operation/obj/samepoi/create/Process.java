package com.navinfo.dataservice.engine.edit.operation.obj.samepoi.create;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

/**
 * @author zhangyt
 * @Title: Process.java
 * @Description: TODO
 * @date: 2016年8月29日 上午10:28:31
 * @version: v1.0
 */
public class Process extends AbstractProcess<Command> {

    private Check check = new Check();

    public Process() {
        super();
    }

    public Process(AbstractCommand command) throws Exception {
        super(command);
    }

    @Override
    public String preCheck() throws Exception {
        Command command = this.getCommand();
        IxPoiSelector selector = new IxPoiSelector(this.getConn());
        IxPoi poi = (IxPoi) selector.loadById(command.getPidArray().getInt(0), false, false);
        IxPoi otherPoi = (IxPoi) selector.loadById(command.getPidArray().getInt(1), false, false);
        check.checkKindOfPOI(poi, otherPoi);
        return super.preCheck();
        //return null;
    }

    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand()).run(this.getResult());
    }

}
