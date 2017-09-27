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

        String parameter = "{ \"command\": \"CREATE\", \"type\": \"SCPLATERESINFO\", infos: [{ infoIntelId: \"1\", infoCode: \"123\", adminCode: \"123\", url: \"123\", newsTime: \"123\", infoContent: \"123\", condition: \"1\", complete: 1, memo: \"123\" }] }";
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

    @Test
    public void create02() throws Exception {

        String parameter = "{ \"command\": \"CREATE\", \"type\": \"SCPLATERESINFO\", infos: [{ infoIntelId: \"ABC1528015\", infoCode: \"S0502815854\", adminCode: \"11000\", url: \"WWW.ACBCDEEEEE.COM\", newsTime: \"20170925\", infoContent: \"增加永久限行\", condition: \"S\", complete: 1, memo: \"00000\" }] }";




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

    @Test
    public void create03() throws Exception {

        String parameter = "{ \"command\": \"UPDATE\", \"type\": \"SCPLATERESGROUP\",objId: \"S1100000011\", data: { infoIntelId: \"ABC1528010\", groupType: 2 ,principle:\"3\" ,\"objStatus\":\"UPDATE\"} }";
        Transaction t = new Transaction(parameter);

        String msg = t.run();


    }
}
