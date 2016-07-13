package com.navinfo.dataservice.engine.edit.zhangyuntao.lu;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;

public class TestIsCCW {

	@Test
	public void IsCCW() throws Exception{
		Coordinate[] ring = new Coordinate[4];
		ring[0] = new Coordinate(0, 0);
		ring[1] = new Coordinate(0, 1);
		ring[2] = new Coordinate(1, 1);
		ring[3] = new Coordinate(1, 0);
		// # of points without closing endpoint
		int nPts = ring.length - 1;

		// check that this is a valid ring - if not, simply return a dummy value
		if (nPts < 3) {
		}

		// algorithm to check if a Ring is stored in CCW order
		// find highest point
		Coordinate hip = ring[0];
		int hii = 0;

		for (int i = 1; i <= nPts; i++) {
			Coordinate p = ring[i];

			if (p.y > hip.y) {
				hip = p;
				hii = i;
			}
		}

		// find different point before highest point
		int iPrev = hii;
		if (iPrev > 0) {
			do {
				iPrev = (iPrev - 1) % nPts;
			} while (ring[iPrev].equals(hip) && iPrev != hii);
		} else // 宋慧星修改
		{
			iPrev = nPts;
			while (ring[iPrev].equals(hip) && iPrev != hii) {
				iPrev = (iPrev - 1) % nPts;
			}
		}
		
		int iNext = hii;
		do {
			iNext = (iNext + 1) % nPts;
		} while (ring[iNext].equals(hip) && iNext != hii);

		Coordinate prev = ring[iPrev];
		Coordinate next = ring[iNext];
		
		double prev2x = prev.x - hip.x;
		double prev2y = prev.y - hip.y;
		double next2x = next.x - hip.x;
		double next2y = next.y - hip.y;

		// compute cross-product of vectors hip->next and hip->prev
		// (e.g. area of parallelogram they enclose)
		Double disc = next2x * prev2y - next2y * prev2x;
		
	}

}
