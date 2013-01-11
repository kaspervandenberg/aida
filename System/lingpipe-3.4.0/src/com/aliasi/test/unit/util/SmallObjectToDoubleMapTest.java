
package com.aliasi.test.unit.util;

import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.SmallObjectToDoubleMap;

import com.aliasi.test.unit.BaseTestCase;

import java.util.Map;
import java.util.HashMap;

public class SmallObjectToDoubleMapTest extends BaseTestCase {

    public void testDotProduct() {
        Map<String,Double> map1 = new HashMap<String,Double>();
        Map<String,Double> map2 = new HashMap<String,Double>();

        assertSmallProduct(map1,map2,0.0);

        map1.put("a",2.0);
        assertSmallProduct(map1,map2,0.0);

        map2.put("b",3.0);
        assertSmallProduct(map1,map2,0.0);

        map1.put("b",5.0);
        assertSmallProduct(map1,map2,15.0);

        map2.put("c",7.0);
        assertSmallProduct(map1,map2,15.0);

        map1.put("d",11.0);
        assertSmallProduct(map1,map2,15.0);

        map1.put("c",13.0);
        assertSmallProduct(map1,map2,106.0);
    }

    void assertSmallProduct(Map<String,Double> map1,
                            Map<String,Double> map2,
                            double expectedVal) {
        SmallObjectToDoubleMap<String> smallMap1
            = new SmallObjectToDoubleMap<String>(map1);

        SmallObjectToDoubleMap<String> smallMap2
            = new SmallObjectToDoubleMap<String>(map2);

        assertEquals(expectedVal,smallMap1.dotProduct(smallMap2),0.0001);
    }

    public void testOne() {
        Map<String,Double> map = new HashMap<String,Double>();
        assertSmall(map);

        map.put("foo",1.0);
        assertSmall(map);

        map.put("bar",-1.0);
        assertSmall(map);
    }

    public void testMultiplication() {
        Map<String,Double> map = new HashMap<String,Double>();
        map.put("a",1.0);
        map.put("b",-2.0);

        SmallObjectToDoubleMap<String> smMap
            = new SmallObjectToDoubleMap<String>(map);

        SmallObjectToDoubleMap<String> multMap = smMap.multiply(7.0);
        assertEquals(7.0,multMap.get("a").doubleValue(),0.0001);
        assertEquals(-14.0,multMap.get("b").doubleValue(),0.0001);
    }

    void assertSmall(Map<String,Double> map) {
        SmallObjectToDoubleMap<String> smallMap
            = new SmallObjectToDoubleMap<String>(map);
        assertFullEquals(map,smallMap);
        assertFullSerialization(smallMap);

        assertEquals(map.size(),smallMap.size());

        assertEquals(map.isEmpty(), smallMap.isEmpty());

        assertEquals(map.keySet(),smallMap.keySet());

        assertEquals(map.entrySet(),smallMap.entrySet());

        for (String key : map.keySet())
            assertEquals(map.get(key), smallMap.get(key));


        for (String s : map.keySet())
            assertEquals(map.get(s).doubleValue(),
                         smallMap.getValue(s));
    }

    void assertSerializable() {

    }


}