package com.navinfo.dataservice.engine.limit.operation.limit.scplateresface.create;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.Result;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresFace;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.util.*;


class Line2Face {

    private TreeMap<String, LineInfo> lineStorage = new TreeMap<>();

    private Map<String, Set<String>> pointStorage = new HashMap<>();

    void createFace(List<ScPlateresFace> faces, Result result) throws Exception {

        if (faces.size() < 1) {

            return;
        }

        init(faces);

        LineInfo firstLine = lineStorage.firstEntry().getValue();

        List<List<Coordinate>> allCoordinates = new ArrayList<>();

        List<Coordinate> polygonCoordinate = Arrays.asList(firstLine.getCoordinates());

        getCoordinate(firstLine, firstLine.sPoint, allCoordinates);

        if (allCoordinates.size() != faces.size()) {
            throw new Exception(" 临时几何不闭合，errcode：003");
        }

        for (List<Coordinate> coordinates : allCoordinates) {
            polygonCoordinate.addAll(1, coordinates);
        }

        if (!polygonCoordinate.get(0).equals(polygonCoordinate.get(polygonCoordinate.size() - 1))) {
            throw new Exception(" 临时几何不闭合，errcode：004");
        }

        Geometry geometry = JtsGeometryFactory.createPolygon(polygonCoordinate
                .toArray(new Coordinate[polygonCoordinate.size()]));

        firstLine.getFace().changedFields().put("geometry", GeoTranslator.jts2Geojson(geometry));

        result.insertObject(firstLine.getFace(), ObjStatus.UPDATE, firstLine.getLineId());

        lineStorage.remove(firstLine.getLineId());

        for (LineInfo line : lineStorage.values()) {
            result.insertObject(line.getFace(), ObjStatus.DELETE, line.getLineId());
        }
    }

    private void init(List<ScPlateresFace> faces) throws Exception {

        for (ScPlateresFace face : faces) {
            LineInfo line = new LineInfo(face);

            lineStorage.put(line.getLineId(), line);

            if (!pointStorage.containsKey(line.getSPoint())) {
                pointStorage.put(line.getSPoint(), new HashSet<String>());
            }

            if (!pointStorage.containsKey(line.getEPoint())) {
                pointStorage.put(line.getEPoint(), new HashSet<String>());
            }

            pointStorage.get(line.getSPoint()).add(line.getLineId());
            pointStorage.get(line.getEPoint()).add(line.getLineId());

            if (pointStorage.get(line.getSPoint()).size() > 2
                    || pointStorage.get(line.getEPoint()).size() > 2) {

                throw new Exception("临时几何" + line.getLineId() + "连接了多个临时几何");
            }
        }

        for (Map.Entry<String, Set<String>> entry : pointStorage.entrySet()) {
            if (entry.getValue().size() != 2) {
                throw new Exception(" 临时几何不闭合，errcode：001");
            }
        }
    }

    private void getCoordinate(LineInfo currLine, String pointFlag, List<List<Coordinate>> allCoordinates) {

        LineInfo relationLine = getRelationLine(pointFlag, currLine.getLineId());

        if (relationLine == null) {
            return;
        }

        List<Coordinate> Coordinates = Arrays.asList(relationLine.getCoordinates());

        allCoordinates.add(Coordinates);

        String nextPointFlag = currLine.sPoint.equals(pointFlag) ? currLine.ePoint : currLine.sPoint;

        getCoordinate(relationLine, nextPointFlag, allCoordinates);
    }

    private LineInfo getRelationLine(String pointFlag, String currLineId) {

        String relationLineId = null;

        for (String lineId : pointStorage.get(pointFlag)) {

            if (!lineId.equals(currLineId)) {

                relationLineId = lineId;

                break;
            }
        }

        if (relationLineId != null && lineStorage.containsKey(relationLineId)) {

            return lineStorage.get(relationLineId);
        }

        return null;
    }

    private class LineInfo {

        private ScPlateresFace face;

        LineInfo(ScPlateresFace face) {

            this.face = face;

            this.sPoint = face.getGeometry().getCoordinate().toString();

            this.ePoint = face.getGeometry().getCoordinates()[face.getGeometry().getCoordinates().length - 1].toString();
        }

        private String sPoint;
        private String ePoint;

        String getSPoint() {
            return sPoint;
        }

        String getEPoint() {
            return ePoint;
        }

        String getLineId() {
            return face.getGeometryId();
        }

        Coordinate[] getCoordinates() {
            return face.getGeometry().getCoordinates();
        }

        ScPlateresFace getFace() {
            return face;
        }
    }
}
