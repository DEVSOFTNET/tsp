/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp.engines.crossover;

import junit.framework.TestCase;

import org.saiko.ai.genetics.tsp.City;
import org.saiko.ai.genetics.tsp.TSP;

/**
 * @author dusan.saiko@gmail.com
 *
 * Object TestCase
 * @see org.saiko.ai.genetics.tsp.engines.crossover.GreedyCrossoverEngine
 */
public class GreedyCrossoverEngineTests extends TestCase {

   /**
    * Test routine
    */
   public void testEngine() {
      TSP tsp=new TSP();
      
      City c1=new City(0,tsp.configuration,"a",1,1);
      City c2=new City(1,tsp.configuration,"b",2,1);
      City c3=new City(2,tsp.configuration,"c",2,2);
      City c4=new City(3,tsp.configuration,"d",1,2);
      
      //clone() array operation
      City[] gene=new City[]{
            c1, c2, c3, c4
      };

      City.initDistanceCache(gene.length);
      GreedyCrossoverEngine e=new GreedyCrossoverEngine();
      tsp.configuration.setInitialPopulationSize(10000);
      e.initialize(tsp.configuration,gene);
      for(int i=0; i<50; i++) {
    	  e.nextGeneration();
      }
      assertTrue(e.getBestChromosome().getTotalDistance()==4.0);
   }
}