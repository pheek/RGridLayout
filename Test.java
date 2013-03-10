package eu.gressly.gui.rgrid;

import javax.swing.*;
//import com.gressly.phi.awt.*;


/****************************************************************************
* <code>                          Test
* </code> 
* ...
*
* @author Philipp Gressly (phi@gressly.ch) http://www.gressly.ch/schulung
* @version 0.99 -- 2002-Jan-18 15:51
*/
/* History: 2002-Jan-18 15:51 (first implementations)
*
*  Bugs   : none known.
*****************************************************************************/
public class Test extends JPanel
{


  /****************************************************************************
  * <code>                          Test
  * </code> 
  * 
  *
  * @param 
  * @return 
  */
  /* History: 2002-Jan-18 15:51 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  public  Test()
  {
    setLayout(new RGridLayout("P + 10, *", "30, 40 + *"));
    add(new JButton("JButton 1"), "text1");
    add(new JButton("JButton 2"), "text2");
    add(new JButton("JButton 3"), "text3");
    add(new JButton("JButton 4"), "text4");
  }  // end method: Test

  /****************************************************************************
  * <code>                          main
  * </code> 
  * 
  *
  * @param 
  * @return 
  */
  /* History: 2002-Jan-18 15:51 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  public static void main(String[] args)
  {
    JFrame f = new JFrame("Test Test");
    Test testTest = new Test();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(testTest);
    f.setSize(300, 300);
    f.setVisible(true);
  } // end main 



}  // end class: Test
