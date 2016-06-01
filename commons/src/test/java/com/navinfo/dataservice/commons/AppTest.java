package com.navinfo.dataservice.commons;

import java.util.ArrayList;
import java.util.List;

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
    public static void main(String[] args) {
		List<String> list1 =  new ArrayList<String>();
		List<String> list2 =  new ArrayList<String>();
		list1.add("AAA");
		list1.add("BBB");
		list1.add("CCC");
		list2.add("CCC");
		list2.add("DDDD");
		list2.add("FFF");
		list2.retainAll(list1);
		System.out.println(list2);
	}
}
