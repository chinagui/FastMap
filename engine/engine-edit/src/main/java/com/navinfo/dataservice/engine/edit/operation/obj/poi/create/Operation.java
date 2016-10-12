package com.navinfo.dataservice.engine.edit.operation.obj.poi.create;

import java.sql.Connection;

import com.navinfo.dataservice.bizcommons.service.PidService;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

/**
 * @author 赵凯凯
 * @version V1.0
 * @Title: Operation.java
 * @Description: 新增行政区划代表点操作类
 * @date 2016年6月15日 下午2:31:50
 */
public class Operation implements IOperation {

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;

        this.conn = conn;

    }

    @Override
    public String run(Result result) throws Exception {

        IxPoi ixPoi = new IxPoi();

        String msg = null;

        // 构造几何对象
        JSONObject geoPoint = new JSONObject();

        geoPoint.put("type", "Point");

        geoPoint.put("coordinates", new double[]{command.getLongitude(), command.getLatitude()});

        // 根据经纬度计算图幅ID
        String meshIds[] = CompGeometryUtil.geo2MeshesWithoutBreak(GeoTranslator.geojson2Jts(geoPoint, 1, 5));

        if (meshIds.length > 1) {
            throw new Exception("不能在图幅线上创建POI");
        }
        if (meshIds.length == 1) {
            ixPoi.setMeshId(Integer.parseInt(meshIds[0]));
        }
        ixPoi.setPid(PidUtil.getInstance().applyPoiPid());
        result.setPrimaryPid(ixPoi.getPid());
        ixPoi.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
        ixPoi.setxGuide(command.getXguide());
        ixPoi.setyGuide(command.getYguide());
        ixPoi.setLinkPid(command.getLinkPid());

        ixPoi.setKindCode(command.getKindCode());

        //计算poi在link的位置信息（side）
        RdLinkSelector selector = new RdLinkSelector(conn);
        RdLink link = (RdLink) selector.loadById(command.getLinkPid(), true, true);

        JSONObject geojson = new JSONObject();

        geojson.put("type", "Point");

        geojson.put("coordinates", new double[]{command.getXguide(), command.getYguide()});

        Geometry nearestPointGeo = GeoTranslator.geojson2Jts(geojson, 100000, 0);

        int side = GeometryUtils.calulatPointSideOflink(ixPoi.getGeometry(), link.getGeometry(), nearestPointGeo);

        ixPoi.setSide(side);

        result.insertObject(ixPoi, ObjStatus.INSERT, ixPoi.getPid());

        generationIxPoiName(ixPoi, result);

        return msg;
    }

    /**
     * 创建POI是如果传递Name参数则建立IxPoiName数据
     *
     * @param poi    新创建POI对象
     * @param result 结果集
     * @throws Exception
     */
    private void generationIxPoiName(IxPoi poi, Result result) throws Exception {
        String name = command.getName();
        if (StringUtils.isEmpty(name))
            return;

        IxPoiName ixPoiName = new IxPoiName();
        ixPoiName.setPid(PidUtil.getInstance().applyPoiNameId());
        ixPoiName.setPoiPid(poi.pid());
        ixPoiName.setName(name);
        ixPoiName.setNameType(2);
        ixPoiName.setLangCode("CHI");
        result.insertObject(ixPoiName, ObjStatus.INSERT, poi.pid());
    }
}
