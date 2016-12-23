package com.navinfo.dataservice.engine.edit.operation.obj.adface.delete;

import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdAdminSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.operation.AbstractProcess;

public class Process extends AbstractProcess<Command> implements IProcess {

    private Check check = new Check();

    /**
     * @param command
     * @throws Exception
     */
    public Process(AbstractCommand command) throws Exception {
        super(command);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void postCheck() throws Exception {

        check.postCheck(this.getConn(), this.getResult(), this.getCommand().getDbId());
        super.postCheck();
    }

    @Override
    public boolean prepareData() throws Exception {
        AdFace face = (AdFace) new AdFaceSelector(getConn()).loadById(getCommand().getFaceId(), true);
        getCommand().setFace(face);
        try {
            AdAdmin admin = (AdAdmin) new AdAdminSelector(getConn()).loadById(face.getRegionId(), true);
            getCommand().setAdAdmin(admin);
        } catch (Exception e) {
        }
        return super.prepareData();
    }

    /* (non-Javadoc)
         * @see com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess#createOperation()
         */
    @Override
    public String exeOperation() throws Exception {
        return new Operation(this.getCommand(), check, this.getConn()).run(this.getResult());
    }


}
