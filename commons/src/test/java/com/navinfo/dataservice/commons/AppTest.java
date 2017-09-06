package com.navinfo.dataservice.commons;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.navinfo.dataservice.commons.util.DateUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    public static void main(String[] args) throws Exception {
    	String startWorkDay="20170906";
		String endWorkDay="20170908";
		Timestamp start = DateUtils.stringToTimestamp(startWorkDay, DateUtils.DATE_YMD);
		Timestamp end = DateUtils.stringToTimestamp(endWorkDay, DateUtils.DATE_YMD);
		Date startDate = DateUtils.stringToDate(startWorkDay, DateUtils.DATE_YMD);
		long days=DateUtils.diffDay(start, end);
		System.out.println(days);
    	Date date=DateUtils.stringToDate(startWorkDay, DateUtils.DATE_YMD);
		Date date2 = DateUtils.addDay(date,Integer.valueOf(String.valueOf(days)));
		System.out.println(DateUtils.dateToString(date2, DateUtils.DATE_YMD));
	}
}
