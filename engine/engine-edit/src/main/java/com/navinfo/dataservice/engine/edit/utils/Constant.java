package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwNode;

import java.util.*;

/**
 * @Title: Constant
 * @Package: com.navinfo.dataservice.engine.edit.utils
 * @Description: 常量
 * @Author: Crayeres
 * @Date: 2017/4/10
 * @Version: V1.0
 */
public class Constant {

    /**
     * 几何坐标保留精度位数
     */
    public final static Integer BASE_PRECISION = 5;

    /**
     * 基本扩大系数
     */
    public final static Integer BASE_EXPAND = 100000;

    /**
     * 基本缩小系数
     */
    public final static double BASE_SHRINK = 0.00001;

    /**
     * 最短允许LINK长度
     */
    public final static double MIN_LINK_LENGTH = 2L;

    /**
     * 接边操作点
     */
    public static List<ObjType> NODE_TYPES = new ArrayList() {{
        add(ObjType.RDNODE);
        add(ObjType.RWNODE);
        add(ObjType.LCNODE);
        add(ObjType.LUNODE);
        add(ObjType.ADNODE);
        add(ObjType.ZONENODE);
    }};

    /**
     * 接边操作线
     */
    public static List<ObjType> LINK_TYPES = new ArrayList() {{
        add(ObjType.RDLINK);
        add(ObjType.RWLINK);
        add(ObjType.LCLINK);
        add(ObjType.LULINK);
        add(ObjType.ADLINK);
        add(ObjType.ZONELINK);
    }};

    /**
     * 接边操作面
     */
    public static List<ObjType> FACE_TYPES = new ArrayList() {{
        add(ObjType.LCFACE);
        add(ObjType.LUFACE);
        add(ObjType.ADFACE);
        add(ObjType.ZONEFACE);
    }};

    /**
     * RDLINK相关关系要素
     */
    public static List<ObjType> RDLINK_REF_OBJECT = new ArrayList() {{
        add(ObjType.RDELECTRONICEYE);
    }};

    public final static Map<ObjType, Class> OBJ_TYPE_CLASS_MAP = new HashMap() {{
        put(ObjType.RDLINK, RdNode.class);
        put(ObjType.RWLINK, RwNode.class);
        put(ObjType.LCLINK, LcNode.class);
        put(ObjType.LULINK, LuNode.class);
        put(ObjType.ADLINK, AdNode.class);
        put(ObjType.ZONELINK, ZoneNode.class);

        put(ObjType.LCFACETOPO, LcLink.class);
        put(ObjType.LUFACETOPO, LuLink.class);
        put(ObjType.ADFACETOPO, AdLink.class);
        put(ObjType.ZONEFACETOPO, ZoneLink.class);

    }};
}
