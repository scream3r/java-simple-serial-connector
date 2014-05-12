/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.scream3r.jssc;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author mvogt
 */
public class TestSerialPortList extends TestCase {
    
    public void testSerialPortListing() {
        String[] out = SerialPortList.getPortNames();
        System.out.println(Arrays.toString(out));
        
        // if we reached this point, loading the dynamic lib worked.
        assertTrue(true);
    }
}
