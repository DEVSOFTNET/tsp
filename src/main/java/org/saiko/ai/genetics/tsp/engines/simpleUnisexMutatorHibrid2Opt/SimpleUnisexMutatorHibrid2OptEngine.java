/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp.engines.simpleUnisexMutatorHibrid2Opt;

import org.saiko.ai.genetics.tsp.City;
import org.saiko.ai.genetics.tsp.TSPChromosome;
import org.saiko.ai.genetics.tsp.engines.crossoverHibrid2opt.GreedyCrossoverHibrid2OptEngine;
import org.saiko.ai.genetics.tsp.engines.simpleUnisexMutator.SimpleUnisexMutatorEngine;

/**
 * @author dusan.saiko@gmail.com
 *
 * Class witch extends SimpleUnisexMutatorEngine and adds hibrid heuristics
 * to solve the Traveling Salesman Problem
 *
 * For heuristics, the 2opt mutation is used, as described at http://www.gcd.org/sengoku/docs/arob98.pdf
 * 
 * @see #getChild(TSPChromosome)
 * @see org.saiko.ai.genetics.tsp.engines.crossover.GreedyCrossoverEngine
 * @see org.saiko.ai.genetics.tsp.TSPEngine
 * @see org.saiko.ai.genetics.tsp.engines.crossover.GreedyCrossoverEngine
 */
public class SimpleUnisexMutatorHibrid2OptEngine extends SimpleUnisexMutatorEngine {

   /**
    * Creates new randomly mutated chromosome from its parent.
    * This is the most simple unisex genetic mutation algorithm,
    * but this algorithm is combined with 2opt heuristics
    * @param parent
    */
   @Override
   protected void getChild(TSPChromosome parent) {

      //clone the cities to new array
      City child1[]=parent.getCities().clone();
      
      //aply random swaping to cities
      mutate(child1);

      //addon
      GreedyCrossoverHibrid2OptEngine.heuristics2opt(child1);
      
      //add new chromosome to population
      population.add(new TSPChromosome(child1));
   }
}