package com.navinfo.dataservice.expcore.sql.replacer;


import java.util.List;
import com.navinfo.dataservice.expcore.sql.ExpSQL;

/**
 * User: liuqing
 * Date: 2010-9-29
 * Time: 14:21:14
 */
public interface SqlReplacer {

    /**
     *
     * @param expSQL
     * @param statmentArgs
     * @return
     */
    public ExpSQL replaceByTempTable(ExpSQL expSQL, String condition);
}
