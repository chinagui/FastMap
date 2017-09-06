package com.navinfo.dataservice.engine.edit.utils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneNode;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.lc.LcNode;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuNode;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
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
    public static Map<ObjType, Class<IRow>> NODE_TYPES = new HashMap() {{
        put(ObjType.RDNODE, RdNode.class);
        put(ObjType.RWNODE, RwNode.class);
        put(ObjType.LCNODE, LcNode.class);
        put(ObjType.LUNODE, LuNode.class);
        put(ObjType.ADNODE, AdNode.class);
        put(ObjType.ZONENODE, ZoneNode.class);
    }};

    /**
     * 接边操作线
     */
    public static Map<ObjType, Class<IRow>> LINK_TYPES = new HashMap() {{
        put(ObjType.RDLINK, RdLink.class);
        put(ObjType.RWLINK, RwLink.class);
        put(ObjType.LCLINK, LcLink.class);
        put(ObjType.LULINK, LuLink.class);
        put(ObjType.ADLINK, AdLink.class);
        put(ObjType.ZONELINK, ZoneLink.class);
    }};

    /**
     * 接边操作面
     */
    public static Map<ObjType, Class<IRow>> FACE_TYPES = new HashMap() {{
        put(ObjType.LCFACE, LcFace.class);
        put(ObjType.LUFACE, LuFace.class);
        put(ObjType.ADFACE, AdFace.class);
        put(ObjType.ZONEFACE, ZoneFace.class);
    }};

    /**
     * RDLINK相关关系要素
     */
    public static List<ObjType> CRF_TYPES = new ArrayList() {{
        add(ObjType.RDINTER);
        add(ObjType.RDROAD);
        add(ObjType.RDOBJECT);
    }};

    public final static Map<ObjType, Class> OBJ_TYPE_CLASS_MAP = new HashMap() {{
        put(ObjType.RDNODE, RdLink.class);
        put(ObjType.RWNODE, RwLink.class);
        put(ObjType.LCNODE, LcLink.class);
        put(ObjType.LUNODE, LuLink.class);
        put(ObjType.ADNODE,  AdLink.class);
        put(ObjType.ZONENODE, ZoneLink.class);

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
