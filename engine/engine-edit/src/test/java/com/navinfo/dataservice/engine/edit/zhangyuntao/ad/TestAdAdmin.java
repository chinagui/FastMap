package com.navinfo.dataservice.engine.edit.zhangyuntao.ad;

import com.navinfo.dataservice.engine.edit.InitApplication;
import com.navinfo.dataservice.engine.edit.zhangyuntao.eleceye.TestUtil;
import org.junit.Test;

/**
 * Created by chaixin on 2016/11/22 0022.
 */
public class TestAdAdmin extends InitApplication {
    @Override
    public void init() {
        super.initContext();
    }


    @Test
    public void delete() {
        String parameter = "{\"command\":\"DELETE\",\"dbId\":17,\"type\":\"ADNODE\",\"objId\":203846}";
        TestUtil.run(parameter);
    }

    @Test
    public void move() {
        String requester = "{\"command\":\"UPDATE\",\"type\":\"ADADMINGROUP\",\"dbId\":13,\"data\":{\"groupTree\":{\"regionId\":1273," +
                "\"name\":\"中国大陆\",\"group\":{\"groupId\":248,\"regionIdUp\":1273,\"rowId\":\"4CD2A8FD7D310C91E0530100007F75A5\"}," +
                "\"children\":[{\"regionId\":580,\"name\":\"北京市\",\"group\":{\"groupId\":114,\"regionIdUp\":580," +
                "\"rowId\":\"4CD2A8FD7AE00C91E0530100007F75A5\"},\"part\":{\"groupId\":40,\"regionIdDown\":580," +
                "\"rowId\":\"4CD2A8FDBC870C91E0530100007F75A5\"},\"children\":[{\"regionId\":2338,\"name\":\"石景山区\"," +
                "\"group\":{\"groupId\":0,\"regionIdUp\":2338,\"rowId\":\"B44681AB359F4D92A0AB1D5A69BD4ECB\"},\"part\":{\"groupId\":114," +
                "\"regionIdDown\":2338,\"rowId\":\"4CD2A8FDC7A30C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":3240," +
                "\"name\":\"房山区\",\"group\":{\"groupId\":401000117,\"regionIdUp\":3240,\"rowId\":\"6AAD11020E83441DBE9758916E2B1CCD\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":3240,\"rowId\":\"4CD2A8FDBB8C0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":3241,\"name\":\"大兴区\",\"group\":{\"groupId\":501000097,\"regionIdUp\":3241," +
                "\"rowId\":\"A7AD0D2144AA40A89B5C7E872C4599C4\"},\"part\":{\"groupId\":286,\"regionIdDown\":3241," +
                "\"rowId\":\"4CD2A8FDBB8F0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":3249,\"name\":\"昌平区\"," +
                "\"group\":{\"groupId\":510000132,\"regionIdUp\":3249,\"rowId\":\"22427D62DD7446C0B5A9A7B8D54284FA\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":3249,\"rowId\":\"4CD2A8FDBB990C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":3250,\"name\":\"怀柔区\",\"group\":{\"groupId\":503000114,\"regionIdUp\":3250," +
                "\"rowId\":\"84B2C264F18A4BB48E0C6FE19C6C7B28\"},\"part\":{\"groupId\":114,\"regionIdDown\":3250," +
                "\"rowId\":\"4CD2A8FDBB9B0C91E0530100007F75A5\"},\"children\":[{\"regionId\":510000011,\"name\":\"雁塔区\"," +
                "\"part\":{\"groupId\":503000114,\"regionIdDown\":510000011,\"rowId\":\"9B9347A7B70E4E67B9541A9539E2C7AA\"," +
                "\"objType\":\"update\"},\"children\":[]},{\"regionId\":510000011,\"name\":\"雁塔区\",\"part\":{\"groupId\":503000114," +
                "\"regionIdDown\":510000011,\"rowId\":\"9B9347A7B70E4E67B9541A9539E2C7AA\",\"objType\":\"update\"},\"children\":[]}]}," +
                "{\"regionId\":1028,\"name\":\"密云区\",\"group\":{\"groupId\":503000113,\"regionIdUp\":1028," +
                "\"rowId\":\"997B886A45DB4629A104B3BE9190AC99\"},\"part\":{\"groupId\":286,\"regionIdDown\":1028," +
                "\"rowId\":\"4CD2A8FDC81C0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1878,\"name\":\"海淀区\"," +
                "\"group\":{\"groupId\":504000124,\"regionIdUp\":1878,\"rowId\":\"AA31E4C039654C3A94AAF49BD0086A11\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":1878,\"rowId\":\"4CD2A8FDC5CD0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":164,\"name\":\"门头沟区\",\"group\":{\"groupId\":410000112,\"regionIdUp\":164," +
                "\"rowId\":\"DB544331DB4147E093CA3DB903E6BA20\"},\"part\":{\"groupId\":114,\"regionIdDown\":164," +
                "\"rowId\":\"4CD2A8FDC23D0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":174,\"name\":\"平谷区\"," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":174,\"rowId\":\"4CD2A8FDC24A0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":170,\"name\":\"顺义区\",\"part\":{\"groupId\":114,\"regionIdDown\":170," +
                "\"rowId\":\"4CD2A8FDC2450C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":579,\"name\":\"西城区\"," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":579,\"rowId\":\"4CD2A8FDBC850C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1013,\"name\":\"朝阳区\",\"part\":{\"groupId\":114,\"regionIdDown\":1013," +
                "\"rowId\":\"4CD2A8FDBE4F0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1021,\"name\":\"延庆区\"," +
                "\"part\":{\"groupId\":286,\"regionIdDown\":1021,\"rowId\":\"4CD2A8FDC44A0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1876,\"name\":\"丰台区\",\"part\":{\"groupId\":114,\"regionIdDown\":1876," +
                "\"rowId\":\"4CD2A8FDC5CA0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1879,\"name\":\"东城区\"," +
                "\"group\":{\"groupId\":400000125,\"regionIdUp\":1879,\"rowId\":\"F89B3A9A8CB44D828A8ED95E1FE73BCD\"}," +
                "\"part\":{\"groupId\":286,\"regionIdDown\":1879,\"rowId\":\"4CD2A8FDC5D00C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1421,\"name\":\"北京市区\",\"group\":{\"groupId\":286,\"regionIdUp\":1421," +
                "\"rowId\":\"4CD2A8FD7B6E0C91E0530100007F75A5\"},\"part\":{\"groupId\":114,\"regionIdDown\":1421," +
                "\"rowId\":\"4CD2A8FDBFE90C91E0530100007F75A5\"},\"children\":[{\"regionId\":2338,\"name\":\"石景山区\"," +
                "\"group\":{\"groupId\":0,\"regionIdUp\":2338,\"rowId\":\"B44681AB359F4D92A0AB1D5A69BD4ECB\"},\"part\":{\"groupId\":114," +
                "\"regionIdDown\":2338,\"rowId\":\"4CD2A8FDC7A30C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":3240," +
                "\"name\":\"房山区\",\"group\":{\"groupId\":401000117,\"regionIdUp\":3240,\"rowId\":\"6AAD11020E83441DBE9758916E2B1CCD\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":3240,\"rowId\":\"4CD2A8FDBB8C0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":3241,\"name\":\"大兴区\",\"group\":{\"groupId\":501000097,\"regionIdUp\":3241," +
                "\"rowId\":\"A7AD0D2144AA40A89B5C7E872C4599C4\"},\"part\":{\"groupId\":286,\"regionIdDown\":3241," +
                "\"rowId\":\"4CD2A8FDBB8F0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":3249,\"name\":\"昌平区\"," +
                "\"group\":{\"groupId\":510000132,\"regionIdUp\":3249,\"rowId\":\"22427D62DD7446C0B5A9A7B8D54284FA\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":3249,\"rowId\":\"4CD2A8FDBB990C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":3250,\"name\":\"怀柔区\",\"group\":{\"groupId\":503000114,\"regionIdUp\":3250," +
                "\"rowId\":\"84B2C264F18A4BB48E0C6FE19C6C7B28\"},\"part\":{\"groupId\":114,\"regionIdDown\":3250," +
                "\"rowId\":\"4CD2A8FDBB9B0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1028,\"name\":\"密云区\"," +
                "\"group\":{\"groupId\":503000113,\"regionIdUp\":1028,\"rowId\":\"997B886A45DB4629A104B3BE9190AC99\"}," +
                "\"part\":{\"groupId\":286,\"regionIdDown\":1028,\"rowId\":\"4CD2A8FDC81C0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1878,\"name\":\"海淀区\",\"group\":{\"groupId\":504000124,\"regionIdUp\":1878," +
                "\"rowId\":\"AA31E4C039654C3A94AAF49BD0086A11\"},\"part\":{\"groupId\":114,\"regionIdDown\":1878," +
                "\"rowId\":\"4CD2A8FDC5CD0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":164,\"name\":\"门头沟区\"," +
                "\"group\":{\"groupId\":410000112,\"regionIdUp\":164,\"rowId\":\"DB544331DB4147E093CA3DB903E6BA20\"}," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":164,\"rowId\":\"4CD2A8FDC23D0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":174,\"name\":\"平谷区\",\"part\":{\"groupId\":114,\"regionIdDown\":174," +
                "\"rowId\":\"4CD2A8FDC24A0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":170,\"name\":\"顺义区\"," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":170,\"rowId\":\"4CD2A8FDC2450C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":579,\"name\":\"西城区\",\"part\":{\"groupId\":114,\"regionIdDown\":579," +
                "\"rowId\":\"4CD2A8FDBC850C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1013,\"name\":\"朝阳区\"," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":1013,\"rowId\":\"4CD2A8FDBE4F0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1021,\"name\":\"延庆区\",\"part\":{\"groupId\":286,\"regionIdDown\":1021," +
                "\"rowId\":\"4CD2A8FDC44A0C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1876,\"name\":\"丰台区\"," +
                "\"part\":{\"groupId\":114,\"regionIdDown\":1876,\"rowId\":\"4CD2A8FDC5CA0C91E0530100007F75A5\"},\"children\":[]}," +
                "{\"regionId\":1879,\"name\":\"东城区\",\"group\":{\"groupId\":400000125,\"regionIdUp\":1879," +
                "\"rowId\":\"F89B3A9A8CB44D828A8ED95E1FE73BCD\"},\"part\":{\"groupId\":286,\"regionIdDown\":1879," +
                "\"rowId\":\"4CD2A8FDC5D00C91E0530100007F75A5\"},\"children\":[]},{\"regionId\":1422,\"name\":\"通州区\"," +
                "\"part\":{\"groupId\":286,\"regionIdDown\":1422,\"rowId\":\"4CD2A8FDBFEB0C91E0530100007F75A5\"}," +
                "\"children\":[]}]}]}]}},\"rowId\":\"5B86BA9069C442E6A96C6B439FD62833\",\"subtaskId\":1}";
        TestUtil.run(requester);
    }
}
