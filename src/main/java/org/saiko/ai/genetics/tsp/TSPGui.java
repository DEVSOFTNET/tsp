/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author dusan.saiko@gmail.com
 *
 * GUI for representation of the traveling salesman problem.
 */
public class TSPGui extends JFrame {

   /**
    * generated serialVersionUID
    */
   protected static final long serialVersionUID =8917595268427032741L;

   /**
    * graphical window size. the window will be not resizable
    */
   protected static final Dimension windowSize      =new Dimension(785,580);

   /**
    * Menu handler class
    */
   protected TSPMenu menu;
   
   /**
    * virtual coordinates of the city maps 
    * the virtual coordinates will be translated to screen coordinates before
    * displaying
    * X0 - X minimum 
    */
   protected int virtualX0;
   /**
    * virtual coordinates of the city maps 
    * the virtual coordinates will be translated to screen coordinates before
    * displaying
    * X1 - X maximum 
    */
   protected int virtualX1;
   /**
    * virtual coordinates of the city maps 
    * the virtual coordinates will be translated to screen coordinates before
    * displaying
    * Y0 - Y minimum 
    */
   protected int virtualY0;
   /**
    * virtual coordinates of the city maps 
    * the virtual coordinates will be translated to screen coordinates before
    * displaying
    * Y1 - Y maximum 
    */
   protected int virtualY1;
   
   /**
    * JPanel for placing the map of the cities
    * This panel takes center of the display window
    */
   protected JPanel cityMap;
   
   /**
    * border of graphical window - x coordiates need border for label of city name displaying,
    * y coordinates for label and for status bar
    */
   protected int border;
   
   /**
    * scale of virtual coordinates into screen coordinates. 
    */
   protected double scale;   

   /**
    * status bar component displayed at the bottom of GUI
    */
   protected JTextField statusBar;
 
   /**
    * Bounds of client graphics space
    */
   protected Rectangle clientBounds;
   
   /**
    * Bounds of the map area (clientBounds - status ...)
    */
   protected Rectangle mapDisplayBounds;

   /**
    * Application title
    */
   protected final static String appTitle="Traveling salesman problem";

   /**
    * height of the status window
    */
   protected int statusHeight;


   /**
	 * parent application object 
	 */
	protected TSP parent;
   
   /**
    * class constructor
    * @param parent - parent application object
    */
   public TSPGui(TSP parent) {
      super();
      this.parent=parent;
   }

   
   /**
    * show the window
    */
   public void init() {
	  menu=new TSPMenu(parent);
	   
	  setIconImage(new ImageIcon(this.getClass().getResource("/logo16.gif")).getImage());
	  setTitle(appTitle+" v " + AppVersion.getAppVersion(this.getClass()));
	      
      //window properties
      setSize(windowSize);
      setResizable(false);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      //create menu
      menu.createMenuBar();

      setVisible(true);

      //create and show the content
      putComponents();


      statusBar.setText("Ready");
      
      setVisible(true);
      
      invalidate();
      repaint(); 
   }
   

   /**
    * Places map and status bar on the window
    */
   public void putComponents() {
	  getContentPane().setBackground(Color.WHITE);
      getContentPane().setLayout(null);
      statusBar=new JTextField("Initializing ...");
      getContentPane().add(statusBar);
      clientBounds=getContentPane().getBounds();

      //compute status height from font
      statusHeight=(int)(1.5*statusBar.getFontMetrics(statusBar.getFont()).getHeight());
      
      statusBar.setBounds(
            0,
            clientBounds.height-statusHeight,
            clientBounds.width,
            statusHeight
      );
      
      //create map
      createCityMap(false);
   }

   
   /**
    * computes virtual coordinates bounds from the list of cities
    * through these virtual coordinates the recomputation into screen 
    * coordinates is made
    */
   private void computeVirtualBounds() {
      virtualX0=-1;
      virtualX1=-1;
      virtualY0=-1;
      virtualY1=-1;
      for(City city: parent.cities) {
         if(virtualX0==-1 || city.x<virtualX0) virtualX0=city.x;
         if(virtualX1==-1 || city.x>virtualX1) virtualX1=city.x;
         if(virtualY0==-1 || city.y<virtualY0) virtualY0=city.y;
         if(virtualY1==-1 || city.y>virtualY1) virtualY1=city.y;
      }

      //compute border for map according to the font height
      border=(int)(2.0*getGraphics().getFontMetrics().getHeight());
      
      //scale between virtual and screen distances
      scale=Math.min(
            (mapDisplayBounds.width-2.0*border)/(virtualX1-virtualX0),
            (mapDisplayBounds.height-2.0*border)/(virtualY1-virtualY0));
      
   }
   

   
   /**
    * transforms virtual X into screen X according to the screen dimensions and 
    * virtual bounds of the city map.
    * @param virtualX
    * @return X coordinate transormed into screen dimension
    */
   protected int transformVirtualX(int virtualX) {
      int x=(int)(border+scale*(virtualX-virtualX0));
      return x;
   }
   
   /**
    * transforms virtual Y into screen Y according to the screen dimensions and 
    * virtual bounds of the city map
    * @param virtualY
    * @return Y coordinate transormed into screen dimension
    */
   protected int transformVirtualY(int virtualY) {
      int y=(int)(border/2+scale*(virtualY-virtualY0));
      return y;
   }
   
   /**
    * creates map of cities in the form of jLabel and jTextFields components
    * @param reloadCities - should be cities reloaded ? (needed for changing the map)
    */
   protected void createCityMap(boolean reloadCities) {

      synchronized(TSP.mutex) {
         
         parent.bestChromosome=null;
         statusBar.setText("Loading ...");
         if(reloadCities) {
        	 parent.cities=null;
        	 parent.loadCities(null,true);
         }
         
         //remove cityMap panel, if already exists (selection of new map)
         if(cityMap!=null) {
            getContentPane().remove(cityMap);
         }
         cityMap=null;
         
         //create cityMap panel
         createMapPanel();
         getContentPane().add(cityMap);
         cityMap.setBounds(
               0,
               0,
               clientBounds.width,
               clientBounds.height-statusHeight
         );
         mapDisplayBounds=cityMap.getBounds();
         
         //place cities into map panel
         placeCities();
      }
      invalidate();
      repaint();
      statusBar.setText("Ready");
   }

   /**
    * Creates map panel with city and path draw functionality 
    */
   private void createMapPanel() {
      cityMap=new JPanel() {
         /** serialVersionUID  */
        private static final long serialVersionUID =4509704840522450842L;

        @Override
        public void paint(Graphics g) {
           //antialiazing feature - slows the graphics quite down
           if(parent.configuration.antialiasing) {
               Graphics2D g2 = (Graphics2D)g;
               g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
               g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);                 
               
           }

           //paint the components first
           //(cities)
           super.paint(g);
           
           //paint the paths from best chromosome
           if(parent.bestChromosome!=null) {
               g.setColor(Color.DARK_GRAY);
               
               //draw the paths for the best chromozone
               City bestCities[]=parent.bestChromosome.cities;
               for(int i=0; i < bestCities.length-1; i++) {
                  City city1=bestCities[i];
                  City city2=bestCities[i+1];
                  g.drawLine(
                        transformVirtualX(city1.getX()),
                        transformVirtualY(city1.getY()),
                        transformVirtualX(city2.getX()),
                        transformVirtualY(city2.getY())
                  );
               }
               //draw path back to city 0
              City city1=bestCities[bestCities.length-1];
              City city2=bestCities[0];
              g.drawLine(
                    transformVirtualX(city1.getX()),
                    transformVirtualY(city1.getY()),
                    transformVirtualX(city2.getX()),
                    transformVirtualY(city2.getY())
              );
            } //if bestChromosome!=null
         } // paint()        
     }; //new JPanel() {}
     cityMap.setOpaque(false);
     cityMap.setLayout(null); //free layout 
   }
   
   /**
    * place cities at cityMap JPanel 
    * city icon is JLabel component and city jabel is JTextField 
    */
   private void placeCities() {

      computeVirtualBounds();
      
      //place all cities
      for(int i=0; i<parent.cities.length; i++) {

         //place city icon
         JLabel cityIcon=new JLabel() {
            /** serialVersionUID */
            private static final long serialVersionUID =2395710541880785590L;

            @Override
            public void paint(Graphics g) {
               g.setColor(getBackground());
               if(parent.configuration.antialiasing) {
                  Graphics2D g2 = (Graphics2D)g;
                  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                  g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);                 
                  
               }
               if(getText()==null || getText().length()==0) {
                  g.fillOval(0,0,4,4);
               } else {
                  g.fillRect(0,0,5,5);
               }
            }
         };
         
         //set the color for city
         Color color=Color.decode("0x00aa00");
         if(parent.cities[i].startCity) {
            color=Color.decode("0xff0000");
            //helper - set some text for icon on the first city
            cityIcon.setText("FIRST"); 
         }
         cityIcon.setBackground(color);
         
         //cityIcon.setBackground(color);
         cityIcon.setBounds(transformVirtualX(parent.cities[i].getX())-2,transformVirtualY(parent.cities[i].getY())-2,5,5);
         cityMap.add(cityIcon);

         //place label and center it
         //if name is not empty
         if(parent.cities[i].getName()!=null && parent.cities[i].getName().trim().length()>0) {
	         JTextField nameLabel=new JTextField(parent.cities[i].getName()) {
	            /** serialVersionUID **/
	            private static final long serialVersionUID =-1495233060258473325L;
	
	            @Override
	            public void paintComponent(Graphics g) {
	               if(parent.configuration.antialiasing) {
	                  Graphics2D g2 = (Graphics2D)g;
	                  g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	                  g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);                 
	                  
	               }
	               super.paintComponent(g);
	             }
	         };
	         nameLabel.setEditable(false);
	         Font font=new Font("Sans",Font.BOLD,9);
	         nameLabel.setFont(font);
	         nameLabel.setBorder(null);
	         nameLabel.setBackground(null);
	         nameLabel.setOpaque(false);
	         nameLabel.setForeground(Color.LIGHT_GRAY);
	        
	         //center the label position
	         Rectangle2D fm=font.getStringBounds(parent.cities[i].getName(),new FontRenderContext( this.getGraphicsConfiguration().getDefaultTransform(),false,true));
	         nameLabel.setBounds(
	               transformVirtualX(parent.cities[i].getX())-(int)fm.getWidth()/2,
	               transformVirtualY(parent.cities[i].getY()),
	               (int)(fm.getWidth()*1.1),
	               (int)(fm.getHeight()*1.05));
	         cityMap.add(nameLabel);
         } //if name is not empty
      }      
   }
   
}