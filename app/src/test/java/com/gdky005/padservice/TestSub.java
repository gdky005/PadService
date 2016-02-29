package com.gdky005.padservice;

import junit.framework.TestCase;

/**
 * Created by WangQing on 16/2/29.
 */
public class TestSub extends TestCase {

    public void testCa() {
        String start = "asd";
        String end = "zxc";

        String rex = "asd123fdafsafdddddddazxc";

        System.out.println(rex.substring(start.length(), rex.length() - end.length()));

    }
}
