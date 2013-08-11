/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import junit.framework.TestCase;

/**
 * @author Dusan Saiko (dusan@saiko.cz)
 * City TestCase
 * @see org.saiko.ai.genetics.tsp.City
 */
public class CityTests extends TestCase {

   /**
    * Test routine
    */
   public void testCity() {
      TSP tsp=new TSP();
      
      City c1=new City(0,tsp.configuration,"x",2,1);
      City c2=new City(1,tsp.configuration,"y",2,2);
      
      City.initDistanceCache(2);
      
      assertTrue(c1.distance(c2)==c2.distance(c1));
      assertTrue(c1.distance(c1)==0);
      assertTrue(c2.distance(c2)==0);
      assertTrue(c1.distance(c2)==1);

      assertTrue(c1.cost(c2)==c2.cost(c1));
      assertTrue(c1.cost(c1)==0);
      assertTrue(c2.cost(c2)==0);
      assertTrue(c1.cost(c2)==1);
      
      assertTrue(c1.equals(c1));
      assertTrue(c2.equals(c2));
      assertTrue(!c1.equals(c2));
      assertTrue(!c2.equals(c1));
      
      //clone() array operation
      City[] gene1=new City[]{
            c1, c1, c2, c2, c1, c2, c1, c2, c2, c1
      };
      
      City[] gene2=gene1.clone();
      
      assertTrue(gene1!=gene2);
      for(int i=0; i<gene1.length; i++) {
         assertTrue(gene1[i].equals(gene2[i]));
         assertTrue(gene1[i]==gene2[i]);
      }
   }
}