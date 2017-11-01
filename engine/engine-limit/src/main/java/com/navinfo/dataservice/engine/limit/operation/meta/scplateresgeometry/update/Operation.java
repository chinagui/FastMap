package com.navinfo.dataservice.engine.limit.operation.meta.scplateresgeometry.update;

import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IOperation;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresGeometry;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

    private Command command;

    public Operation(Command command) {
        this.command = command;
    }

    @Override
    public String run(Result result) throws Exception {
        updateGroup(result);
        return null;
    }

    private void updateGroup(Result result) throws Exception {

        JSONObject content = command.getContent();

        //批量写入boundaryLink
        if (command.getGeometrys() != null) {

            for (ScPlateresGeometry geometry : command.getGeometrys()) {

                if (geometry.getBoundaryLink().equals(command.getBoundaryLink())) {
                    continue;
                }
                geometry.changedFields().put("boundaryLink", command.getBoundaryLink());

                result.insertObject(geometry, ObjStatus.UPDATE, geometry.getGeometryId());

                result.setPrimaryId(geometry.getGeometryId());

            }
        } else {


            ScPlateresGeometry geometry = command.getGeometry();

            if (content.containsKey("objStatus") && ObjStatus.UPDATE.toString().equals(
                    content.getString("objStatus"))) {

                boolean isChanged = geometry.fillChangeFields(content);

                if (isChanged) {
                    result.insertObject(geometry, ObjStatus.UPDATE, geometry.getGeometryId());
                }

            }
            result.setPrimaryId(geometry.getGeometryId());
        }

}
}
