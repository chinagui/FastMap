package com.navinfo.dataservice.engine.edit.zhangyuntao.translate;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.translate.list.ListOperation;
import com.navinfo.navicommons.database.Page;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.util.Map;

/**
 * @Title: ListTest
 * @Package: com.navinfo.dataservice.engine.edit.zhangyuntao.translate
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/17/2017
 * @Version: V1.0
 */
public class ListTest extends InitApplication{

    @Override
    public void init() {
        super.initContext();
    }

    @Test
    public void list() throws Exception {
        ListOperation operation = new ListOperation();
        JSONObject json = new JSONObject();
        json.put("userId", 123);
        json.put("pageSize", 10);
        json.put("pageNum", 1);
        Page page = operation.loadPage(json);
        Map<String,Object> result = new HashedMap();
        result.put("data", page);
        System.out.println(result);
    }

}
