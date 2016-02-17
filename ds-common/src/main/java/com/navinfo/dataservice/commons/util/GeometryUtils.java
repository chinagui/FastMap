package com.navinfo.dataservice.commons.util;

import com.vividsolutions.jts.geom.Coordinate;

public class GeometryUtils {
	
	 /**
	  * 是否是逆时针方向
	 * @param ring
	 * @return
	 * @throws Exception 
	 */
	public static boolean IsCCW(Coordinate[] ring) throws Exception{
		 
		// # of points without closing endpoint
        int nPts = ring.length - 1;

        // check that this is a valid ring - if not, simply return a dummy value
        if (nPts < 3)
        {
            return false;
        }

        // algorithm to check if a Ring is stored in CCW order
        // find highest point
        Coordinate hip = ring[0];
        int hii = 0;

        for (int i = 1; i <= nPts; i++)
        {
            Coordinate p = ring[i];

            if (p.y > hip.y)
            {
                hip = p;
                hii = i;
            }
        }

        // find different point before highest point
        int iPrev = hii;
        if (iPrev > 0)
        {
            do
            {
                iPrev = (iPrev - 1) % nPts;
            } while (ring[iPrev].equals(hip) && iPrev != hii);
        }
        else //宋慧星修改
        {
            iPrev = nPts;
            while (ring[iPrev].equals(hip) && iPrev != hii)
            {
                iPrev = (iPrev - 1) % nPts;
            }
        }

        // find different point after highest point
        int iNext = hii;
        do
        {
            iNext = (iNext + 1) % nPts;
        } while (ring[iNext].equals(hip) && iNext != hii);

        Coordinate prev = ring[iPrev];
        Coordinate next = ring[iNext];

        if (prev.equals(hip) || next.equals(hip) || prev.equals(next))
        {
            throw new Exception("degenerate ring (does not contain 3 different points)");
        }

        // translate so that hip is at the origin.
        // This will not affect the area calculation, and will avoid
        // finite-accuracy errors (i.e very small vectors with very large coordinates)
        // This also simplifies the discriminant calculation.
        double prev2x = prev.x - hip.x;
        double prev2y = prev.y - hip.y;
        double next2x = next.x - hip.x;
        double next2y = next.y - hip.y;

        // compute cross-product of vectors hip->next and hip->prev
        // (e.g. area of parallelogram they enclose)
        Double disc = next2x * prev2y - next2y * prev2x;

        /* If disc is exactly 0, lines are collinear.  There are two possible cases:
                (1) the lines lie along the x axis in opposite directions
                (2) the line lie on top of one another
                
                (2) should never happen, so we're going to ignore it!
                    (Might want to assert this)
        
                (1) is handled by checking if next is left of prev ==> CCW
        */
        if (disc == 0.0)
        {
            return (prev.x > next.x); // poly is CCW if prev x is right of next x
        }
        else
        {
            return (disc > 0.0); // if area is positive, points are ordered CCW                 
        }
		 
	 }

}
