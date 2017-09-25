package com.navinfo.dataservice.engine.limit.test;

import com.navinfo.dataservice.commons.springmvc.ClassPathXmlAppContextInit;
import com.navinfo.dataservice.engine.limit.operation.Transaction;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ly on 2017/9/21.
 */
public class operationTest extends ClassPathXmlAppContextInit {


    protected Logger log = Logger.getLogger(this.getClass());

    @Before
    public void before(){
        initContext(new String[]{"dubbo-consumer-datahub-test.xml"});
    }


    @Test
    public void create01() throws Exception {

        String parameter = "{ \"command\": \"CREATE\", \"type\": \"SCPLATERESGROUP\", \"data\": { \"groupType\": 1, \"adAdmin\":110000, \"infoIntelId\":\"ABC1528010\", \"principle\": \"test\" ,\"condition\":\"S\", }, \"dbId\": 13, \"subtaskId\": 846 } ";

//        ScPlateresLink
//                ScPlateresFace
        //ScPlateresGeometry
        //ScPlateresInfo
        //ScPlateresManoeuvre
        //ScPlateresRdLink
        //ScPlateresInfo
        Transaction t = new Transaction(parameter);

        String msg = t.run();


    }
}
