package com.navinfo.dataservice.api.es.iface;

import com.navinfo.dataservice.api.es.model.TrackPoint;

import java.util.List;

public interface EsApi {
    public void insert(List<TrackPoint> trackPoints)throws Exception;
}
