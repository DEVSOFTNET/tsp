/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.saiko.ai.genetics.tsp.engines.crossover.GreedyCrossoverEngine;
import org.saiko.ai.genetics.tsp.engines.crossoverHibrid2opt.GreedyCrossoverHibrid2OptEngine;
import org.saiko.ai.genetics.tsp.engines.simpleUnisexMutator.SimpleUnisexMutatorEngine;
import org.saiko.ai.genetics.tsp.engines.simpleUnisexMutatorHibrid2Opt.SimpleUnisexMutatorHibrid2OptEngine;

/**
 * @author dusan.saiko@gmail.com
 * 
 *         Main class for representation of the traveling salesman problem.
 */
public class TSP {

    /**
     * Application configuration
     */
    public final TSPConfiguration configuration = new TSPConfiguration();

    /**
     * Thread of running computations
     */
    protected Thread runingThread;

    /**
     * TPS engines which are able to solve the problem
     */
    protected static final Class<TSPEngine> engines[] = new Class[] {
	    SimpleUnisexMutatorEngine.class, GreedyCrossoverEngine.class,
	    GreedyCrossoverHibrid2OptEngine.class,
	    SimpleUnisexMutatorHibrid2OptEngine.class, };

    /**
     * available map files
     */
    protected final static String mapFiles[] = { "cities_020", "cities_050",
	    "cities_100", "cities_150", "cities_192", null, "square_15x15",
	    "triangle_15x15", "circle_150", "circle_120", "full_circle_305",
	    "spiral_263", "line_100", };

    /**
     * selected map file
     */
    protected String mapFile = mapFiles[2];

    /**
     * synchronization mutex
     */
    protected final static Object mutex = new Object();

    /**
     * generated serialVersionUID
     */
    protected static final long serialVersionUID = 8917595268427032741L;

    /**
     * Cities definition. cities are loaded from definition file which
     * coordinates are writen in S-JTSK format
     */
    protected City cities[] = null;

    /**
     * pause flag - pause is required
     */
    protected volatile boolean pauseRequestFlag = false;

    /**
     * stop flag - stop is required
     */
    protected volatile boolean stopRequestFlag = false;

    /**
     * started flag - the computation is running
     */
    protected volatile boolean startedFlag = false;

    /**
     * best chromosome of population (to draw ...)
     */
    protected TSPChromosome bestChromosome;

    /**
     * Computation start time
     */
    protected long startTime = 0;

    /**
     * Total running time (ms)
     */
    protected long runTime = 0;

    /**
     * Selected engine class
     */
    protected Class<TSPEngine> engineClass = engines[3];

    /**
     * Engine instance from engineClass
     */
    TSPEngine engine;

    /**
     * Engine class name (short form)
     */
    String engineName;

    /**
     * The count of generation which give the same best cost result
     */
    int bestCostAge;

    /**
     * generation counter
     */
    int generation = 0;

    /**
     * cost of best chromosome
     */
    double bestCost = 0;

    /**
     * loads cities from selected map
     * 
     * @param citiesToLoad
     *            - not null if we just want to set some exact cities
     * @param initDiscanceCache
     *            - true if we want to initialize the distance cache of cities
     * @see City#initDistanceCache(int)
     */
    protected void loadCities(City[] citiesToLoad, boolean initDiscanceCache) {
	try {
	    cities = citiesToLoad;
	    if (cities == null) {
		List<City> c = new ArrayList<>();

		// get the stream
		// first try with only file name in mapFile
		try (BufferedReader reader = new BufferedReader(
			new InputStreamReader(TSP.class.getClassLoader()
				.getResourceAsStream(mapFile + ".csv"), "UTF-8"))) {

		    // read the data
		    String line;
		    Pattern patternLine = Pattern
			    .compile("\"(.*?)\",(.*?),(.*)"); //$NON-NLS-1$
		    int count = 0;
		    int id = 0;
		    while ((line = reader.readLine()) != null) {
			count++;
			if (count == 1)
			    continue;
			Matcher regex = patternLine.matcher(line);
			if (regex.find()) {
			    String name = regex.group(1).trim();
			    Integer x = new Integer(regex.group(2));
			    Integer y = new Integer(regex.group(3));
			    // city.id is the unique index of city starting from
			    // 0
			    City city = new City(id, configuration, name, x, y);
			    id++;
			    c.add(city);
			}
		    }

		}
		cities = c.toArray(new City[] {});
	    }

	    // set the first city from file as start city
	    cities[0].startCity = true;

	    // find the biggest X
	    int x1 = 0;
	    for (City city : cities) {
		if (city.x > x1)
		    x1 = city.x;
	    }
	    // rotate around y access (characteristics of coordinates)
	    // initialize
	    for (City city : cities) {
		city.x = Math.abs(x1 - city.x);
		// init the distance cache of the city for known number of
		// cities
	    }
	    if (initDiscanceCache) {
		City.initDistanceCache(cities.length);
	    }

	} catch (Throwable e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * class constructor
     */
    public TSP() {
	this(true);
    }

    /**
     * class constructor
     * 
     * @param loadCities
     *            - should the cities be loaded at initialization ?
     */
    public TSP(boolean loadCities) {
	if (loadCities) {
	    // load cities - the selected map has default value
	    loadCities(null, true);
	}
    }

    /**
     * Gui for TSP
     */
    TSPGui gui;

    /**
     * show the window
     */
    public void start() {
	if (!configuration.console) {
	    gui = new TSPGui(this);
	    gui.init();
	} else {
	    run();
	}
    }

    /**
     * runs the genetic computation of selected engine starts in new thread
     * 
     * @see TSPMenu#actionStart(ActionEvent)
     */
    protected void run() {
	try {
	    // initialize variables
	    generation = 0;

	    startTime = System.currentTimeMillis();
	    engine = engineClass.newInstance();
	    engine.initialize(configuration, cities);
	    engineName = engine.getClass().getSimpleName();

	    bestCostAge = 0;

	    double previewCost = 0;
	    double previewDrawCost = 0;
	    long previewDrawTime = 0;
	    bestCost = 0;

	    // status refreshing thread
	    Thread statusThread = new Thread() {
		@Override
		public void run() {
		    while (!stopRequestFlag) {
			runTime = System.currentTimeMillis() - startTime;
			setStatus(engine);
			try {
			    Thread.sleep(configuration.console ? 15000 : 1000);
			} catch (Exception e) {
			    /** nop **/
			}
		    }
		}
	    };
	    statusThread.start();

	    // repeat the evolotion until stop is required
	    while (!stopRequestFlag) {
		// if pause, then wait
		if (generation % 3 == 0) {
		    if (!configuration.console && pauseRequestFlag) {
			gui.statusBar.setText("Pause; "
				+ gui.statusBar.getText());
			while (pauseRequestFlag) {
			    try {
				Thread.sleep(1000);
			    } catch (InterruptedException foo) { /* none */
			    }
			}
		    } // pause
		} // check for pause

		// get best chromosome
		bestChromosome = engine.getBestChromosome();
		bestCost = bestChromosome.getTotalDistance();
		if (previewCost == bestCost) {
		    bestCostAge++;
		} else {
		    bestCostAge = 0;
		}
		if (bestCostAge >= configuration.maxBestCostAge) {
		    stopRequestFlag = true;
		}
		previewCost = bestCost;

		long currentTime = System.currentTimeMillis();
		// once in 5 seconds repaint map graphics
		if (previewDrawTime < currentTime - 1000 || pauseRequestFlag) {
		    previewDrawTime = currentTime;
		    if (previewDrawCost != bestCost && !configuration.console) {
			gui.cityMap.repaint();
			previewDrawCost = bestCost;
		    }
		}

		engine.nextGeneration();

		generation++;
	    } // while ! stop

	    // stop actions
	    runTime = System.currentTimeMillis() - startTime;
	    statusThread.interrupt();
	    // wait a second
	    try {
		Thread.sleep(500);
	    } catch (Throwable e) { /* nop */
	    }

	    // refresh status line
	    setStatus(engine);
	    if (!configuration.console) {
		gui.statusBar.setText("Finished; " + gui.statusBar.getText());
	    }

	    stopRequestFlag = false;
	    startedFlag = false;
	    pauseRequestFlag = false;

	    // reenable menu items
	    if (!configuration.console) {
		gui.menu.resetMenu();
		gui.menu.menuItemPDFReport.setEnabled(true);
		gui.menu.menuItemXMLReport.setEnabled(true);
		gui.menu.menuItemXML2PDFReport.setEnabled(true);

		gui.repaint();
	    }
	} catch (Throwable e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    /**
     * set information at status bar
     * 
     * @param engine
     */
    void setStatus(TSPEngine engine) {
	String statusText = String
		.format("%s: cities: %s; time: %s; generation: %s; population: %s; best distance: %s; best age: %s;",
			engineName, cities.length, runTime / 1000, generation,
			engine.getPopulationSize(), (int) bestCost, bestCostAge);
	if (configuration.console) {
	    System.out.println(statusText);
	} else {
	    gui.statusBar.setText(statusText);
	}
    }

    /**
     * Main - starts the application ig text or graphics mode for all the
     * parameters, run this application with /? Possible command line parameters
     * are --console mandatory to set the output to console, no graphics
     * displayed --map=NAME where name is name of resource with .csv format you
     * can use only number part (020, 050...) as map name for build in maps
     * --priority=N where N in <1..10>; DEFAULT 5 --engine=N where N is the
     * index of engine to use; DEFAULT engine is GreedyCrossoverHibrid2OptEngine
     * --rms=T where T in <true,false> - computes RMS cost from distance;
     * DEFAULT false --population=N where N is the initial population size.
     * DEFAULT 1000 --max=N where N is the max number of the same best result;
     * DEFAULT 100 --growth=N where N is population growth. DEFAULT 0.0075
     * --mutation=N where N is mutation ratio. DEFAULT 0.5 --xml=FILE.xml where
     * FILE is output name for XML report file. DEFAULT
     * tsp_report_yyyy_MM_dd_HH_mm.xml See the gui interface help, documentation
     * or http://www.saiko.cz/ai/tsp/ for detailed information. Example:
     * --console --map=192 --priority=1 --engine=3 --rms=false --population=200
     * --growth=0.01 --max=200 --mutation=0.5 --console --help
     * 
     * @param args
     *            - command line arguments
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {

	// process command line
	if (args != null && args.length > 0) {
	    // display ussage ?
	    boolean error = false;
	    String errorMessage = "";

	    int paramPriority = Thread.NORM_PRIORITY;
	    String paramMap = "/cities_050.csv";
	    Class<TSPEngine> paramEngine = engines[3];
	    boolean paramRms = false;
	    int paramPopulation = 1000;
	    int paramMax = 100;
	    double paramMutation = 0.5;
	    double paramGrowth = 0.0075;
	    String paramXMLFileName = "tsp_report_"
		    + new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(Calendar
			    .getInstance().getTime()) + ".xml";

	    try {
		for (int i = 0; i < args.length; i++) {
		    String param = args[i].toLowerCase();
		    while (param.startsWith("-")) {
			param = param.substring(1);
		    }
		    if (param.startsWith("priority=")) {
			param = param.substring(param.lastIndexOf('=') + 1);
			paramPriority = Integer.parseInt(param);
		    } else if (param.equals("console")) {
			// nothing
		    } else if (param.startsWith("map=")) {
			param = param.substring(param.lastIndexOf('=') + 1);

			String[] possiblePaths = new String[] { param,
				"/" + param, "/" + param + ".csv",
				"/cities_" + param + ".csv",
				"/cities_" + param,
				"/cities_0" + param + ".csv",
				"/cities_0" + param, };
			for (String resName : possiblePaths) {
			    if (TSP.class.getResourceAsStream(resName) != null) {
				paramMap = resName;
				break;
			    }
			}

			if (paramMap == null) {
			    error = true;
			}

		    } else if (param.startsWith("engine=")) {
			int engineIndex = Integer.parseInt(param
				.substring(param.lastIndexOf('=') + 1));
			paramEngine = engines[engineIndex];
			@SuppressWarnings("unused")
			TSPEngine engineInterface = paramEngine.newInstance();
		    } else if (param.startsWith("population=")) {
			paramPopulation = Integer.parseInt(param
				.substring(param.lastIndexOf('=') + 1));
		    } else if (param.startsWith("max=")) {
			paramMax = Integer.parseInt(param.substring(param
				.lastIndexOf('=') + 1));
		    } else if (param.startsWith("growth=")) {
			paramGrowth = Double.parseDouble(param.substring(param
				.lastIndexOf('=') + 1));
		    } else if (param.startsWith("mutation=")) {
			paramMutation = Double.parseDouble(param
				.substring(param.lastIndexOf('=') + 1));
		    } else if (param.startsWith("rms=")) {
			paramRms = Boolean.parseBoolean(param.substring(param
				.lastIndexOf('=') + 1));
		    } else if (param.startsWith("xml=")) {
			paramXMLFileName = param.substring(param
				.lastIndexOf('=') + 1);
		    } else {
			error = true;
			break;
		    }
		}
	    } catch (Throwable e) {
		error = true;
	    }

	    if (error) {
		String ussage = "Error in command line parameters.\n"
			+ errorMessage
			+ "\nOptions: \n"
			+ "--console     mandatory, sets the output to console, no graphics displayed.\n"
			+ "--map=NAME    where name is name of resource with .csv format.\n"
			+ "                build in maps:\n"
			+ "                  /cities_020.csv\n"
			+ "                  /cities_050.csv <- DEFAULT\n"
			+ "                  /cities_100.csv\n"
			+ "                  /cities_150.csv\n"
			+ "                  /cities_192.csv\n"
			+ "                you can use only number part (020, 050...) as map name for build in maps.\n"
			+ "--priority=N  where N in <1..10>; DEFAULT 5\n"
			+ "--engine=N    where N is the index of engine to use; DEFAULT engine is GreedyCrossoverHibrid2OptEngine\n"
			+ "                build in engines:\n";
		int i = 0;
		for (Class<TSPEngine> engineClass : engines) {
		    ussage += "                  " + i + ": "
			    + engineClass.getSimpleName() + "\n";
		    i++;
		}
		ussage += "--rms=T       where T in <true,false> - computes RMS cost from distance; DEFAULT false\n"
			+ "--population=N where N is the initial population size. DEFAULT 1000.\n"
			+ "--max=N       where N is the max number of the same best result; DEFAULT 100\n"
			+ "--growth=N    where N is population growth. DEFAULT 0.0075\n"
			+ "--mutation=N  where N is mutation ratio. DEFAULT 0.5\n"
			+ "--xml=FILE.xml where FILE is output name for XML report file. \n"
			+ "              DEFAULT tsp_report_yyyy_MM_dd_HH_mm.xml\n"
			+ "\n"
			+ "See the gui interface help, documentation or http://www.saiko.cz/ai/tsp/ for detailed information.\n"
			+ "Example:       --console --map=192 --priority=1 --engine=3 --rms=false --population=200 --growth=0.01 --max=200 --mutation=0.5 \n"
			+ "               --console\n"
			+ "               --help\n";
		System.err.println(ussage);
		System.exit(-1);
	    }

	    // display used parameters
	    System.out.println("Priority: " + paramPriority);
	    System.out.println("Map: " + paramMap);
	    System.out.println("Engine: " + paramEngine);
	    System.out.println("RMS: " + paramRms);
	    System.out.println("Population: " + paramPopulation);
	    System.out.println("Mutation: " + paramMutation);
	    System.out.println("Growth: " + paramGrowth);
	    System.out.println("Max best age: " + paramMax);
	    System.out.println("XML report file: " + paramXMLFileName);
	    System.out.println("Initializing ...");

	    // set the parameters
	    TSP tsp = new TSP(false);
	    tsp.configuration.console = true;
	    tsp.configuration.initialPopulationSize = paramPopulation;
	    tsp.configuration.maxBestCostAge = paramMax;
	    tsp.configuration.rmsCost = paramRms;
	    tsp.configuration.mutationRatio = paramMutation;
	    tsp.configuration.populationGrow = paramGrowth;
	    tsp.configuration.threadPriority = paramPriority;
	    tsp.engineClass = paramEngine;
	    tsp.engine = paramEngine.newInstance();
	    tsp.mapFile = paramMap;

	    // load the map
	    tsp.loadCities(null, true);

	    System.out.println("Running ...");
	    tsp.start();

	    System.out.println("Writing the report ...");
	    new TSPMenu(tsp).actionXMLReport(paramXMLFileName);

	    System.out.println("Finished.");
	    System.exit(0);
	} // end command line processing
	else {
	    // run the program in GUI
	    new TSP().start();
	}
    }

    public static void openFile(File file) {

	try {
	    Desktop.getDesktop().open(file);
	} catch (Throwable e) {
	    String os = System.getProperty("os.name").toLowerCase();

	    boolean win = (os.indexOf("windows") != -1 || os.indexOf("nt") != -1);
	    boolean lin = (os.indexOf("linux") != -1);

	    if (lin) {
		try {
		    Runtime.getRuntime().exec(
			    new String[] { "gnome-open", file.toString() });
		} catch (Throwable e2) {
		    try {
			Runtime.getRuntime().exec(
				new String[] { "xdg-open", file.toString() });
		    } catch (IOException e1) {
			e1.printStackTrace();
		    }
		}
	    } else if (win) {
		try {
		    Runtime.getRuntime().exec(
			    new String[] { "cmd", "/c", "start",
				    file.toString() });
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }
}