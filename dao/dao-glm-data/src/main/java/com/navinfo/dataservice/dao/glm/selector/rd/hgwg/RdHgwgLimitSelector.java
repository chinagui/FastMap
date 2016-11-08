package com.navinfo.dataservice.dao.glm.selector.rd.hgwg;

import com.navinfo.dataservice.dao.glm.model.rd.hgwg.RdHgwgLimit;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import java.sql.Connection;

/**
 * Created by chaixin on 2016/11/7 0007.
 */
public class RdHgwgLimitSelector extends AbstractSelector {
    public RdHgwgLimitSelector(Connection conn) {
        super(RdHgwgLimit.class, conn);
    }
}
