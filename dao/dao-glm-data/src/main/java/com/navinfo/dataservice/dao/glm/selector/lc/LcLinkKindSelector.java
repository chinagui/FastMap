package com.navinfo.dataservice.dao.glm.selector.lc;

import com.navinfo.dataservice.dao.glm.model.lc.LcLinkKind;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;

import java.sql.Connection;

/**
 * @author zhangyt
 * @Title: LcLinkKindSelector.java
 * @Description: TODO
 * @date: 2016年7月27日 下午1:51:02
 * @version: v1.0
 */
public class LcLinkKindSelector extends AbstractSelector {

    public LcLinkKindSelector(Connection conn) throws InstantiationException, IllegalAccessException {
        super(LcLinkKind.class, conn);
    }

}
