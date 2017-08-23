package com.navinfo.dataservice.engine.es;

import com.navinfo.dataservice.api.es.iface.EsApi;
import com.navinfo.dataservice.api.es.model.TrackPoint;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("editApi")
public class EsApiImpl implements EsApi {
    EsController controller = new EsController();
    @Override
    public void insert(List<TrackPoint> trackPoints) throws Exception {
        controller.insert(trackPoints);
    }
}
