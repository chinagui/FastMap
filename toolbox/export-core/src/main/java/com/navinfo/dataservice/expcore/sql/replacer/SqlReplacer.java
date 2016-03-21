package com.navinfo.dataservice.expcore.sql.replacer;


import java.util.Set;

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
    public ExpSQL replaceByTempTable(ExpSQL expSQL, String condition,Set<String> conditionParams);
//    public ExpSQL replace(ExpSQL expSQL, Map<String, StatmentArgs> statmentArgs);
}
