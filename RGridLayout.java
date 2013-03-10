package eu.gressly.gui.rgrid;

import java.awt.*;
import java.util.*;

/****************************************************************************
* <code>                          RGridLayout
* </code> 
* Implements a relative Gridlayout. It is a Grid where the rows and cols are
* not all the same width and height. It is not possible to span Components over
* multiple rows or cols.
* <pre>
*  Example:
*    myPanel.setLayout(new RGridLayout("10 + P,*", "10%, 50,*, M"));
* </pre>
* This example constructs a Table with 2 columns and 4 rows.
* The first column has a with of 10 pixels plus the maximal preferred size of 
*     all components of this column (P).
* The second column uses the rest of the container.
* The first row uses 10% of the flexible height.
* The second row is exactly 50 pixels high.
* The third row uses 90% (the rest of the flexible part) in height.
* The last row uses the bigest "getMinimumSize()" of all components in this row.<br />
* Lets have a look at the details.
* <ul>
*   <li>30  -> Any fixed number (except %) defines a fixed width in pixels. <br />
*   <li>10% -> Any Percentage value is claculated of the flexible remaining part.
*   <li>P   -> Take maximum over all "prefferedSize()"s of all components in the corresponding 
*              row (column).
*   <li>M   -> Same as P, but use "minimumSize()"
*   <li>*   -> The rest of the space goes into this component.
*   <li>+   -> all of the above attributes can be added. "10+P+3%" for example means:
*              10 Pixel + maximal preferred Size + 3% of flexible part.
* </ul>
* 
* @author Philipp Gressly (phi@gressly.ch) http://www.gressly.ch/schulung
* @version 0.99 -- 2001-Nov-30 08:02
*/
/* History: 2001-Nov-30 08:02 (first implementations)
*
*  Bugs   : none known.
*****************************************************************************/
public class RGridLayout implements LayoutManager
{

  static final int DIM_COLS = 0;
  static final int DIM_ROWS = 1;

  AdditiveAttr colAttrs[];
  AdditiveAttr rowAttrs[];
  Component allComps[][]; // col , row
  
  /****************************************************************************
  * <code>                          RGridLayout
  * </code> 
  * 
  * @param colAttrString defining the widths  of the cols. Details see above.
  * @param rowAttrString defining the heights of the rows. Details see above
  */
  /* History: 2001-Nov-30 08:02 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  public  RGridLayout(String colAttrString, String rowAttrString)
  {
    this.colAttrs = parseAttrString(colAttrString);
    this.rowAttrs = parseAttrString(rowAttrString);    
  }  // end method: RGridLayout

  /****************************************************************************
  * <code>                          parseAttrString
  * </code> 
  * Parse an attr eg: "30%,P+10+20%,M,10,*"
  *
  * @param attrString
  * @return AdditiveAttr array
  */
  /* History: 2001-Nov-30 08:33 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private AdditiveAttr[] parseAttrString(String attrString)
  {
    //AdditiveAttr [] aaArray;
    
    ArrayList<AdditiveAttr> aaVec = new ArrayList<AdditiveAttr>();
    StringTokenizer st = new StringTokenizer(attrString, ",;|");
    while(st.hasMoreTokens()) {
      String addAttrString = st.nextToken();
      AdditiveAttr aa = new AdditiveAttr(addAttrString);
      aaVec.add(aa);
    }

    AdditiveAttr arr[] = new AdditiveAttr[aaVec.size()];    
    for(int i = 0; i < aaVec.size(); i++) {
      arr[i] = (AdditiveAttr) aaVec.get(i);
    }
    
    return arr;
  }  // end method: parseAttrString


  // no - ops for Layout Manager:
  @Override
public void addLayoutComponent(String name, Component comp) 
  { System.out.println("Adding with parameters is not yet implemented. You called: " + name);
  }

  @Override
public void removeLayoutComponent(Component comp) 
  { }
 
  /****************************************************************************
  * <code>                          preferredLayoutSize
  * </code> 
  * Returns the size of the container.
  *
  * @param Container target
  * @return size of the container.
  */
  /* History: 2001-Nov-30 08:49 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  @Override
public Dimension preferredLayoutSize(Container target)
  {
      synchronized (target.getParent().getTreeLock()) {
	LayoutManager tmp = target.getLayout();
	target.setLayout(null); // otherwise the container asks here recursively.
	Dimension dim = target.getPreferredSize();
	target.setLayout(tmp);
	return dim;
      }

  }  // end method: preferredLayoutSize


  /****************************************************************************
  * <code>                          minimumLayoutSize
  * </code> 
  * Returns the minimum size of the containing container.
  *
  * @param Container target
  * @return size of the container.
  */
  /* History: 2001-Nov-30 08:49 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  @Override
public Dimension minimumLayoutSize(Container target)
  {
      synchronized (target.getParent().getTreeLock()) {
	LayoutManager tmp = target.getLayout();
	target.setLayout(null); // otherwise the container asks here recursively.
	Dimension dim = target.getMinimumSize();
	target.setLayout(tmp);
	return dim;
      }

  }  // end method: minimumLayoutSize

  
  /****************************************************************************
  * <code>                          layoutContainer
  * </code> 
  * Layout the container acording to the given Attributes for row and col.
  *
  * @param target to lay out
  */
  /* History: 2001-Nov-30 09:11 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  @Override
public void layoutContainer(Container target)
  {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      int ncomponents = target.getComponentCount();
      int ncols = colAttrs.length;
      int nrows = rowAttrs.length;

      if (ncomponents == 0) {
	return;
      }

      int wTotal = target.getWidth() - (insets.left + insets.right);
      int hTotal = target.getHeight() - (insets.top + insets.bottom);

      getAllComps(ncols, nrows, target);
      calcEffectiveValues(colAttrs, wTotal, DIM_COLS);
      calcEffectiveValues(rowAttrs, hTotal, DIM_ROWS);
      setAllCompBounds(ncols, nrows);
    }
  }  // end method: layoutContainer


  /****************************************************************************
  * <code>                          setAllCompBounds
  * </code> 
  * Set all component Bounds (using "setBounds()")
  *
  */
  /* History: 2001-Nov-30 11:28 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private void setAllCompBounds(int ncols, int nrows)
  {
    int top = 0;
    
    for(int r = 0; r < nrows; r ++) {
      int left = 0;
      int height = rowAttrs[r].value;
      for(int c = 0; c < ncols; c++) {
	int width = colAttrs[c].value;
	Component actComp = allComps[c][r];
	if(null != actComp)
	  actComp.setBounds(left, top, width, height);
	left += width;
      }
      top += height;
    }  
  }  // end method: setAllCompBounds

  
  /****************************************************************************
  * <code>                          getAllComps
  * </code> 
  */
  /* History: 2001-Nov-30 09:41 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private void getAllComps(int ncols, int nrows, Container target)
  {
    //int maxtot = ncols * nrows;
    allComps = new Component[ncols][nrows];
    int i = 0;
    for(int r = 0; r < nrows; r ++)  {
      for(int c = 0; c < ncols; c ++)	{
	try {
	  Component comp = target.getComponent(i);
	  i ++;
	  allComps[c][r] = comp;
	} catch (ArrayIndexOutOfBoundsException aie) {
	}
      }
    }
    // col attr components
    for(int c = 0; c < ncols; c ++) {
      for(int r = 0; r < nrows; r ++) {
	if(null != allComps[c][r]) {
	  colAttrs[c].allComps.add(allComps[c][r]);
	  rowAttrs[r].allComps.add(allComps[c][r]);
	}
      }
    }
  }  // end method: getAllComps


  /****************************************************************************
  * <code>                          calcEffectiveValues
  * </code> 
  */
  /* History: 2001-Nov-30 09:49 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private void calcEffectiveValues(AdditiveAttr additiveAttrs[], int totalSize, int dim)
  {
    fillDependant(additiveAttrs, dim);
    int rest = calcRemainder(additiveAttrs, totalSize);
    calcStarPercentages(additiveAttrs);
    spreadRemainder(additiveAttrs, rest);
    sumOverAdditiveArray(additiveAttrs);
  }  // end method: calcEffectiveValues


  /****************************************************************************
  * <code>                          fillDependant
  * </code> 
  * Fill Component Pref- and Minimum Size values.
  */
  /* History: 2001-Nov-30 09:59 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private void fillDependant(AdditiveAttr additiveAttrs[], int dim)
  {
    for(int i = 0; i < additiveAttrs.length; i++) {
      additiveAttrs[i].fillDependant(dim);
    }
  }  // end method: fillDependant

  /****************************************************************************
  * <code>                          calcRemainder
  * </code> 
  * @return 
  */
  /* History: 2001-Nov-30 10:33 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private int calcRemainder(AdditiveAttr additiveAttrs[], int totalSize)
  {
    int remainder = totalSize;
    for(int i = 0; i < additiveAttrs.length; i++) {
      remainder -= additiveAttrs[i].fixedSize();
    }
    return remainder;
  }  // end method: calcRemainder


  /****************************************************************************
  * <code>                          calcStarPercentages
  * </code> 
  * Calculate percentages for all remaining stars.
  *
  */
  /* History: 2001-Nov-30 11:02 (first implementations)
  *
  *  Bugs   : none known.
  *****************************************************************************/
  private void calcStarPercentages(AdditiveAttr additiveAttrs[])
  {
    int starCount = 0;
    double sumPercentages = 0;
    for(int i = 0; i < additiveAttrs.length; i++) {
      starCount += additiveAttrs[i].starCount();
      sumPercentages += additiveAttrs[i].sumPercentages();
    }
    double starPercentage = (100.0 - sumPercentages) / starCount;
    for(int i = 0; i < additiveAttrs.length; i++)
      additiveAttrs[i].setStarPercentage(starPercentage);
  }  // end method: calcStarPercentages

  void spreadRemainder(AdditiveAttr additiveAttr[], int remainder) 
  {
    for(int i = 0; i < additiveAttr.length; i++)
      additiveAttr[i].spreadRemainder(remainder);
  }
  

  void sumOverAdditiveArray(AdditiveAttr additiveAttrs[]) 
  {
    for(int i = 0; i < additiveAttrs.length; i++)
      additiveAttrs[i].sumValues();
  }
} // end class RGridLayout


///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////  AdditiveAttr  ////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////

class AdditiveAttr
{
  PrimitiveAttr primitiveAttr[];
  ArrayList<Component> allComps = new ArrayList<Component>();
  
  int value; // claculated value
  
  // split "+" attrs
  // eg: "P+30+*" is an AdditiveAttr
  AdditiveAttr (String attrString)
  {
    ArrayList<PrimitiveAttr> paVec = new ArrayList<PrimitiveAttr>();
    StringTokenizer st = new StringTokenizer(attrString, "+");
    while(st.hasMoreTokens()) {
      String as = st.nextToken().trim();
      paVec.add(new PrimitiveAttr(as));
    }
    primitiveAttr = new PrimitiveAttr[paVec.size()];
    for(int i = 0; i < paVec.size(); i++) 
      {
	primitiveAttr[i] = (PrimitiveAttr) paVec.get(i);
      }
    
  }  

  void fillDependant(int dim)
  {
    for(int i = 0; i < primitiveAttr.length; i++) {
      primitiveAttr[i].fillDependant(allComps, dim);
    }  
  }
  
  // return the sum of fixed amounts : AT_FIXED, AT_PREF and AT_MIN
  int fixedSize() 
  {
    int fs = 0;
    for(int i = 0; i < primitiveAttr.length; i ++) {
      fs += primitiveAttr[i].fixedSize();
    }
    return fs;
  }
  
  int starCount() 
  {
    int sc = 0;
    for(int i = 0; i < primitiveAttr.length; i++) {
      sc += primitiveAttr[i].starCount();
    }
    return sc;
  }

  double sumPercentages() 
  {
    double sp = 0.0;
    for(int i = 0; i < primitiveAttr.length; i++) {
      sp += primitiveAttr[i].sumPercentages();
    }
    return sp;
  }
  
  void setStarPercentage(double starPercentage) 
  {
    for(int i = 0; i < primitiveAttr.length; i++) {
      primitiveAttr[i].setStarPercentage(starPercentage);
    }
  }
  
  void spreadRemainder(int remainder)
  {
    for(int i = 0; i < primitiveAttr.length; i++) {
      primitiveAttr[i].spreadRemainder(remainder);
    }
  }
  
  void sumValues()
  {
    this.value = 0;
    for(int i = 0; i < primitiveAttr.length; i++) {
      value += primitiveAttr[i].value;
    }
  }
}

///////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////  PrimitiveAttr ////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////



class PrimitiveAttr 
{
  // fixed sizes and max over components
  public static final int AT_FIXED = 1; // 65
  public static final int AT_PREF  = 2; // P
  public static final int AT_MIN   = 3; // M

  // relative sizes
  public static final int AT_PERC  = 4; // 40%
  public static final int AT_REST  = 5; // *
  
  int type; 
  double param = 0;      // for AT_FIXED, AT_PERC
  int value = 0;      // calculated value  

  PrimitiveAttr (String paString) 
  {
    if(paString.indexOf("p") >= 0 || paString.indexOf("P") >= 0) // pref
      {
	type = AT_PREF;
	return;
      }

    if(paString.indexOf("m") >= 0 || paString.indexOf("M") >= 0) // min
      {
	type = AT_MIN;
	return;
      }

    if(paString.indexOf("*") >= 0) // rest
      {
	type = AT_REST;
	return;
      }
    if(paString.indexOf("%") >= 0) // percentage
      {
	type = AT_PERC;
	String numberOnly = paString.replace('%', ' ').trim();
    	try {
	  param = (double) Integer.parseInt(numberOnly);
	} catch (NumberFormatException nex) {
	  throw new IllegalArgumentException("Parse Number error in RGridLayout: (" + numberOnly + ")");
	}
	return;
      }
    // number only
    type = AT_FIXED;
    try {
      param = (double) Integer.parseInt(paString);
      value = (int) param;
    } catch (NumberFormatException nex) {
      throw new IllegalArgumentException("Parse Number error in RGridLayout: (" + paString + ")");
    }
  }

  void fillDependant(ArrayList<Component> allComps, int dim) 
  {
    if(type == AT_PREF || type == AT_MIN) 
    {
      this.value = 0;
      Iterator<Component> it = allComps.iterator();
      while(it.hasNext()) {
	Component actComp = (Component) it.next();
	Dimension cmpSize;
	
	if(type == AT_PREF) 
	  cmpSize = actComp.getPreferredSize();
	else // AT_MIN
	  cmpSize = actComp.getMinimumSize();
	int t;
	if(dim == RGridLayout.DIM_COLS)
	  t = (int) cmpSize.getWidth();
	else // DIM_ROWS
	  t = (int) cmpSize.getHeight();
      
	if(t > this.value)
	  this.value = t;
      }
    } 
  } // end fillDepandant

  // return the sum of fixed amounts : AT_FIXED, AT_PREF and AT_MIN
  int fixedSize() 
  {
    if(type == AT_FIXED || type == AT_PREF || type == AT_MIN) 
      return value;
    else
      return 0;
  }

  int starCount() 
  {
    return type == AT_REST ? 1 : 0;
  }
  
  // claculate the sum of the given (not *) percentages
  double sumPercentages() 
  {
    if(type == AT_PERC)
      return param;
    else
      return 0.0;
  }

  void setStarPercentage(double starPercentage)
  {
    if(type == AT_REST)
      param = starPercentage;
  }
  
  void spreadRemainder(int remainder) 
  {
    if(type == AT_PERC || type == AT_REST) {
      value = (int) ((remainder * param) / 100.0);
    }
  }
   
}
