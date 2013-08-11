/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

/**
 * @author dusan.saiko@gmail.com
 *
 * Interface definition for Traveling salesman genetic engine
 */
public interface TSPEngine {
   
   /**
    * Initialize engine for given population size and list of cities.
    * Can be calledseveral times to reinitialize engine.
    * @param appConfiguration
    * @param cities
    * @see TSPConfiguration
    */
   public void initialize(TSPConfiguration appConfiguration, City cities[]);
   
   
   /**
    * @return current population size from the engine. the population could be growing.
    */
   public int  getPopulationSize();
   
   /**
    * @return the best chromosome from the population
    */
   public TSPChromosome getBestChromosome();
   
   /**
    * Make new generation of population.
    * the genetics principles are left to responsibility of engine
    */
   public void nextGeneration();
}
