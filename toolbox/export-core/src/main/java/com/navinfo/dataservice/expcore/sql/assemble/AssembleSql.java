package com.navinfo.dataservice.expcore.sql.assemble;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.sql.ExpSQL;

/**
 * Created by IntelliJ IDEA.
 * User: liuqing
 * Date: 11-6-17
 * Time: 上午10:04
 * 装配sql的接口
 */
public interface AssembleSql {
    public Map<Integer, List<ExpSQL>> assemble(String gdbVersion,String tempTableSuffix) throws Exception;
}
