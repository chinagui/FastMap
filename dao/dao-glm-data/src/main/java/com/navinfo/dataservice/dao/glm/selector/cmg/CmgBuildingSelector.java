package com.navinfo.dataservice.dao.glm.selector.cmg;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

public class CmgBuildingSelector extends AbstractSelector{
	
	   /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(CmgBuildfaceSelector.class);

    public CmgBuildingSelector(Connection conn) {
        super(CmgBuilding.class, conn);
    }


}
