package com.navinfo.dataservice.engine.limit.search;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;
import com.navinfo.dataservice.engine.limit.glm.iface.IRenderParam;

public class RenderParam implements IRenderParam {

    public RenderParam() {

    }

    private double mpx;
    private double mpy;

    private int x;
    private int y;
    private int z;
    private int gap;

    private String wkt = null;

    public void setX(int x) {

        this.x = x;

        mpx = MercatorProjection.tileXToPixelX(x);
    }

    public void setY(int y) {
        this.y = y;

        mpy = MercatorProjection.tileYToPixelY(y);
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    @Override
    public double getMPX() {
        return mpx;
    }

    @Override
    public double getMPY() {
        return mpy;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public int getGap() {
        return gap;
    }

    @Override
    public String getWkt() {
        return wkt == null ? MercatorProjection.getWktWithGap(x, y, z, gap) : wkt;
    }
}
