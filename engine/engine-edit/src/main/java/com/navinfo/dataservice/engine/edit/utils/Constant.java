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
    public static Map<ObjType, Class<? extends IRow>> NODE_TYPES = new HashMap<>();

    static {
        NODE_TYPES.put(ObjType.RDNODE, RdNode.class);
        NODE_TYPES.put(ObjType.RWNODE, RwNode.class);
        NODE_TYPES.put(ObjType.LCNODE, LcNode.class);
        NODE_TYPES.put(ObjType.LUNODE, LuNode.class);
        NODE_TYPES.put(ObjType.ADNODE, AdNode.class);
        NODE_TYPES.put(ObjType.ZONENODE, ZoneNode.class);
    }

    /**
     * 接边操作线
     */
    public static Map<ObjType, Class<? extends IRow>> LINK_TYPES = new HashMap<>();

    static {
        LINK_TYPES.put(ObjType.RDLINK, RdLink.class);
        LINK_TYPES.put(ObjType.RWLINK, RwLink.class);
        LINK_TYPES.put(ObjType.LCLINK, LcLink.class);
        LINK_TYPES.put(ObjType.LULINK, LuLink.class);
        LINK_TYPES.put(ObjType.ADLINK, AdLink.class);
        LINK_TYPES.put(ObjType.ZONELINK, ZoneLink.class);
    }

    /**
     * 接边操作面
     */
    public static Map<ObjType, Class<? extends IRow>> FACE_TYPES = new HashMap<>();

    static {
        FACE_TYPES.put(ObjType.LCFACE, LcFace.class);
        FACE_TYPES.put(ObjType.LUFACE, LuFace.class);
        FACE_TYPES.put(ObjType.ADFACE, AdFace.class);
        FACE_TYPES.put(ObjType.ZONEFACE, ZoneFace.class);
    }

    /**
     * RDOBJECT相关关系要素
     */
    public static List<ObjType> CRF_INTER = new ArrayList<>();

    static {
        // INTER
        CRF_INTER.add(ObjType.RDINTER);
        CRF_INTER.add(ObjType.RDINTERNODE);
        CRF_INTER.add(ObjType.RDINTERLINK);
    }

    public static List<ObjType> CRF_ROAD = new ArrayList<>();

    static {
        // ROAD
        CRF_ROAD.add(ObjType.RDROAD);
        CRF_ROAD.add(ObjType.RDROADLINK);
    }

    public static List<ObjType> CRF_OBJECT = new ArrayList<>();

    static {
        // OBJECT
        CRF_OBJECT.add(ObjType.RDOBJECT);
        CRF_OBJECT.add(ObjType.RDOBJECTNAME);
        CRF_OBJECT.add(ObjType.RDOBJECTINTER);
        CRF_OBJECT.add(ObjType.RDOBJECTNODE);
        CRF_OBJECT.add(ObjType.RDOBJECTLINK);
        CRF_OBJECT.add(ObjType.RDOBJECTROAD);
    }

    public static List<ObjType> CRF_TYPES = new ArrayList<>();

    static {
        CRF_TYPES.addAll(CRF_INTER);
        CRF_TYPES.addAll(CRF_ROAD);
        CRF_TYPES.addAll(CRF_OBJECT);
    }

    public final static Map<ObjType, Class<? extends IRow>> OBJ_TYPE_CLASS_MAP = new HashMap<>() ;

    static {
        // NODE
        OBJ_TYPE_CLASS_MAP.put(ObjType.RDNODE, RdLink.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.RWNODE, RwLink.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.LCNODE, LcLink.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.LUNODE, LuLink.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.ADNODE,  AdLink.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.ZONENODE, ZoneLink.class);
        // LINK
        OBJ_TYPE_CLASS_MAP.put(ObjType.RDLINK, RdNode.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.RWLINK, RwNode.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.LCLINK, LcNode.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.LULINK, LuNode.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.ADLINK, AdNode.class);
        OBJ_TYPE_CLASS_MAP.put(ObjType.ZONELINK, ZoneNode.class);
    }
}
