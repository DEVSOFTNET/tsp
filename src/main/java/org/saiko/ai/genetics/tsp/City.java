/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

/**
 * @author dusan.saiko@gmail.com
 * 
 *       City definition for traveling salesman problem. City has basic
 *       properties as x and y coordinates and name and some functionality to
 *       get the distance to other cities
 */
public class City {

    /**
     * X coordinate of the city. It could be S-JTSK coordinate [m].
     */
    protected int x;

    /**
     * Y coordinate of the city. It could be S-JTSK coordinate [m].
     */
    protected int y;

    /**
     * X coordinate of the city - original value in sjtsk coordinates. It could
     * be S-JTSK coordinate [m].
     */
    final protected int SJTSKX;

    /**
     * Y coordinate of the city - original value in sjtsk coordinates. It could
     * be S-JTSK coordinate [m].
     */
    final protected int SJTSKY;

    /**
     * city name
     */
    protected String name;

    /**
     * numeric id of the city - index of city in the original arrays of cities
     * main characteristic is, that id is less then the length of the city array
     */
    protected int id;

    /**
     * start city flag 0 city from which the salesman starts
     */
    protected boolean startCity = false;

    /**
     * cache for distances to other cities the cities are indexed beginning from
     * 0 and this index is written into id property this id is then used as
     * index into distanceCache array this chache holds distances from [id1]
     * city to [id2] city
     */
    static double distanceCache[][] = null;

    /**
     * configuration parameters of application
     * 
     * @see TSPConfiguration
     */
    protected TSPConfiguration configuration;

    /**
     * Constructor for the city object
     * 
     * @param id
     *            int id of city (its index)
     * @param configuration
     *            configuration parameters of application
     * @param name
     *            - name of the city
     * @param x
     *            - X coordinate of the city [S-JTSK - [m]]
     * @param y
     *            - Y coordinate of the city [S-JTSK - [m]]
     * @see TSPConfiguration
     */
    public City(int id, TSPConfiguration configuration, String name, int x, int y) {
	this.id = id;
	this.x = x;
	this.y = y;
	this.SJTSKX = x;
	this.SJTSKY = y;
	this.name = name;
	this.configuration = configuration;
    }

    /**
     * initializes the distance cache for know number of cities
     * 
     * @param length
     *            - length of the cache = number of cities
     */
    static synchronized public void initDistanceCache(int length) {
	distanceCache = new double[length][length];
	// reset the cache to -1
	for (int i = 0; i < length; i++) {
	    for (int j = 0; j < length; j++) {
		distanceCache[i][j] = -1;
	    }
	}
    }

    /**
     * Computes distance over two cities. If coorfinates are in S-JTSK, then
     * this distance is in meters. Uses the cache to hold the distances between
     * two cities without having to compute them every time
     * 
     * @param otherCity
     * @param useCache
     *            - true if the cache should be used
     * @return distance between the two cities.
     */
    public double distance(City otherCity, boolean useCache) {

	if (useCache == false) {
	    return distance(otherCity.getX(), otherCity.getY());
	}

	int id1 = this.id;
	int id2 = otherCity.id;
	if (id1 == id2)
	    return 0.0;

	if (id1 > id2) {
	    int swap = id1;
	    id1 = id2;
	    id1 = swap;
	}

	// distance is cached in the 2 dimensional array
	// we order the indexes of cities, so B->A is computed as A->B - it
	// saves us half of combinations
	double distance = distanceCache[id1][id2];
	if (distance == -1) {
	    // no distance found in cache, compute it
	    distance = distance(otherCity.getX(), otherCity.getY());
	    distanceCache[id1][id2] = distance;
	}
	return distance;
    }

    /**
     * Computes distance over two cities. If coorfinates are in S-JTSK, then
     * this distance is in meters. Uses the cache to hold the distances between
     * two cities without having to compute them every time
     * 
     * @param otherCity
     * @return distance between the two cities.
     */
    public double distance(City otherCity) {
	return distance(otherCity, true);
    }

    /**
     * @param otherCity
     * @return cost for traveling to otherCity from this. It may differ from
     *         distance.
     */
    public double cost(City otherCity) {
	double distance = distance(otherCity);
	if (configuration.isRmsCost()) {
	    return distance * distance;
	}
	return distance;
    }

    /**
     * Computes distance from point If coorfinates are in S-JTSK, then this
     * distance is in meters. This city has to have the same coordinate system
     * like a given point.
     * 
     * @param pX
     * @param pY
     * @return distance between this city and some point in the world.
     */
    protected double distance(int pX, int pY) {
	double dx = this.x - pX;
	double dy = this.y - pY;
	double distance = Math.sqrt(dx * dx + dy * dy);
	return distance;
    }

    /**
     * @return Returns the name of the city
     */
    public String getName() {
	return name;
    }

    /**
     * @return Returns the x coordinate of city
     */
    public int getX() {
	return x;
    }

    /**
     * @return Returns the y coordinate of the city
     */
    public int getY() {
	return y;
    }

    /**
     * @return Returns the x coordinate of city - original value in sjtsk
     *         coordinates
     */
    public int getSJTSKX() {
	return SJTSKX;
    }

    /**
     * @return Returns the y coordinate of the city - original value in sjtsk
     *         coordinates
     */
    public int getSJTSKY() {
	return SJTSKY;
    }

    /**
     * @return Name of city with coordinates
     */
    @Override
    public String toString() {
	return name + ": [" + x + ";" + y + "]";
    }

    /**
     * return numeric id of the city - index of city in the original arrays of
     * cities main characteristic is, that id is less then the length of the
     * city array
     * 
     * @return id of the city.
     */
    public int getId() {
	return id;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + SJTSKX;
	result = prime * result + SJTSKY;
	result = prime * result + id;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + x;
	result = prime * result + y;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	City other = (City) obj;
	if (SJTSKX != other.SJTSKX)
	    return false;
	if (SJTSKY != other.SJTSKY)
	    return false;
	if (id != other.id)
	    return false;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (x != other.x)
	    return false;
	if (y != other.y)
	    return false;
	return true;
    }

}