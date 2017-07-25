package com.navinfo.dataservice.dao.fcc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: SolrQueryUtils.java
 * @author y
 * @date 2017-1-18 下午5:29:50
 * @Description: solr查询公共类-一些公共的东西放在这
 *
 */
public class SolrQueryUtils {

    public static Set<String> notDisplayTipTpye=new HashSet<String>();

    public static String NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL = "";
    public static String NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001 = "";

    static{

        /**
         notDisplayTipTpye.add("1108");//减速带
         notDisplayTipTpye.add("1110");// 卡车限制
         notDisplayTipTpye.add("1113");//车道限速
         notDisplayTipTpye.add("1114");//卡车限速
         notDisplayTipTpye.add("1115");//车道变化点
         notDisplayTipTpye.add("1204");//可逆车道
         notDisplayTipTpye.add("1303");//卡车交限
         notDisplayTipTpye.add("1306");//路口语音引导
         notDisplayTipTpye.add("1307");//自然语音引导
         notDisplayTipTpye.add("1308");//禁止卡车驶入
         notDisplayTipTpye.add("1310");//公交车道
         notDisplayTipTpye.add("1311");//可变导向车道
         notDisplayTipTpye.add("1401");//方向看板
         notDisplayTipTpye.add("1402");//Real Sign
         notDisplayTipTpye.add("1406");//实景图
         notDisplayTipTpye.add("1409");//普通路口模式图
         //20170213 屏蔽Tips类型取消窄道tips，增加可变限速tips
         //		notDisplayTipTpye.add("1513");//窄道
         notDisplayTipTpye.add("1112");//可变限速
         notDisplayTipTpye.add("1707");//里程桩
         **/

        //20170720屏蔽增加至38种Tips，屏蔽Tips类型以后配置到元数据库，此为临时解决方案
        notDisplayTipTpye.add("1103");
        notDisplayTipTpye.add("1108");
        notDisplayTipTpye.add("1110");
        notDisplayTipTpye.add("1112");
        notDisplayTipTpye.add("1113");
        notDisplayTipTpye.add("1114");
        notDisplayTipTpye.add("1115");
        notDisplayTipTpye.add("1117");
        notDisplayTipTpye.add("1204");
        notDisplayTipTpye.add("1212");
        notDisplayTipTpye.add("1213");
        notDisplayTipTpye.add("1303");
        notDisplayTipTpye.add("1306");
        notDisplayTipTpye.add("1307");
        notDisplayTipTpye.add("1308");
        notDisplayTipTpye.add("1310");
        notDisplayTipTpye.add("1311");
        notDisplayTipTpye.add("1401");
        notDisplayTipTpye.add("1402");
        notDisplayTipTpye.add("1406");
        notDisplayTipTpye.add("1409");
        notDisplayTipTpye.add("1518");
        notDisplayTipTpye.add("1519");
        notDisplayTipTpye.add("1707");
        notDisplayTipTpye.add("1708");
        notDisplayTipTpye.add("2002");
        notDisplayTipTpye.add("2201");
        notDisplayTipTpye.add("2202");
        notDisplayTipTpye.add("2203");
        notDisplayTipTpye.add("2204");
        notDisplayTipTpye.add("8003");
        notDisplayTipTpye.add("8004");
        notDisplayTipTpye.add("8005");
        notDisplayTipTpye.add("8006");
        notDisplayTipTpye.add("8007");
        notDisplayTipTpye.add("8008");
        notDisplayTipTpye.add("8009");
        notDisplayTipTpye.add("8010");

        if(notDisplayTipTpye.size()!=0){

            StringBuilder builder= new StringBuilder();

            builder.append(" s_sourceType not in (");

            if (notDisplayTipTpye.size() > 0) {

                int i=0;
                for (String type:notDisplayTipTpye) {
                    if (i > 0) {
                        builder.append(",");
                    }
                    builder.append("'");
                    builder.append(type);
                    builder.append("'");
                    i++;
                }
                NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001 = builder.toString()+",'8001')";
                builder.append(")");
            }else{
                NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQLWith8001="s_sourceType != '8001'";
            }

            NOT_DISPLAY_TIP_FOR_315_TYPES_FILER_SQL=builder.toString();

        }
    }


}