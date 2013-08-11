/*
 * Copyright (c) 2013 dusan.saiko@gmail.com
 */
package org.saiko.ai.genetics.tsp;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import junit.framework.TestCase;

/**
 * @author dusan.saiko@gmail.com
 *
 * TSPMenu TestCase
 * @see org.saiko.ai.genetics.tsp.TSPMenu
 */
public class TSPMenuTests extends TestCase {

   /**
    * Test for menubar characteristics
    */
   public void testMenuBar() {
      JMenuBar menu=new TSPMenu(new TSP()).createMenuBar().parent.gui.getJMenuBar();
      
      //go through all menu items and check if all menu has at least one submenu
      for(Component m:menu.getComponents()) {
         assertTrue(m instanceof JMenu);
         assertTrue(((JMenu)m).getMenuComponentCount()>0);
      }
      
      //go through all menu items again
      List<Component> componentsToCheck=new ArrayList<>();
      componentsToCheck.addAll(Arrays.asList(menu.getComponents()));
      while(componentsToCheck.size()>0) {
         Component c=componentsToCheck.remove(0);
         if(!(c instanceof JMenuItem)) {
            continue;
         }
         
         JMenuItem m=(JMenuItem)c;

         //check that all menu item have text
         assertTrue(m.getText()!=null && m.getText().trim().length()>0);
         
         if(m instanceof JMenu) {
            assertTrue(((JMenu)m).getMenuComponentCount()>0);
            componentsToCheck.addAll(Arrays.asList(((JMenu)m).getMenuComponents()));
         } else {
            //check that all menu items have actionListener
            assertTrue(m.getText(),m.getActionListeners().length>0);
         }
      }      
   }
}