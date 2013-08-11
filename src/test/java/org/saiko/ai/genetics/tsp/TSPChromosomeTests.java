/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import junit.framework.TestCase;

/**
 * @author dusan.saiko@gmail.com
 *
 * TSPChromosome TestCase
 * @see org.saiko.ai.genetics.tsp.TSPChromosome
 */
public class TSPChromosomeTests extends TestCase {

   /**
    * Test routine
    */
   public void testChromosome() {
      TSPChromosome c=new TSPChromosome(new TSP().cities);
      assertTrue(c.totalCost>=c.totalDistance && c.totalDistance>0);
      
   }
}