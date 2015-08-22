/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2015 Brad Christie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.bradchristie.taminationsapp;

import org.w3c.dom.Element;

public class Movement {

  static public final int LEFTHAND = 1;
  static public final int RIGHTHAND = 2;
  static public final int BOTHHANDS = 3;
  static public final int GRIPLEFT = 5;
  static public final int GRIPRIGHT = 6;
  //static public final int GRIPBOTH =  7;
  //static public final int ANYGRIP =  4;

  public double beats;
  public double fullbeats;
  public int hands;
  public double cx1;
  public double cy1;
  public double cx2;
  public double cy2;
  public double x2;
  public double y2;
  public double cx3;
  public double cx4;
  public double cy4;
  public double x4;
  public double y4;
  public Bezier btranslate;
  public Bezier brotate;

  /**
   *   Translates a string describing hand use to one of the
   *   int constants above
   * @param h  String from XML hands parameter
   * @return   int constant used in this class
   */
  static public int getHands(String h)
  {
    final String[] handnames = {
      "none", "left", "right", "both", "anygrip", "gripleft", "gripright", "gripboth"
    };
    int retval = 0;
    for (int i=0; i<handnames.length; i++)
      if (h.equals(handnames[i]))
        retval = i;
    return retval;
  }

  /**
   * Constructor for a movement where the dancer always faces
   * the direction of travel, so only one Bezier curve is needed
   * @param beats  Timing
   * @param hands  One of the const ints above
   *               X and Y values for start of curve are always 0, 0
   * @param cx1    X value for 1st control point
   * @param cy1    Y value for 1st control point
   * @param cx2    X value for 2nd control point
   * @param cy2    Y value for 2nd control point
   * @param x2     X value for end of curve
   * @param y2     Y value for end of curve
   */
  public Movement(double beats, int hands, double cx1, double cy1,
                  double cx2, double cy2, double x2, double y2)
  {
    this.beats = this.fullbeats = beats;
    this.hands = hands;
    this.cx1 = this.cx3 = cx1;
    this.cy1 = cy1;
    this.cx2 = this.cx4 = cx2;
    this.cy2 = this.cy4 = cy2;
    this.x2 = this.x4 = x2;
    this.y2 = this.y4 = y2;
    recalculate();
  }

  /**  Constructor for a movement where the dancer does not face the direction
   *   of travel.  Two Bezier curves are used, one for travel and one for
   *   facing direction.
   *
   * @param beats  Timing
   * @param hands  One of the const ints above
   *     Next set of parameters are for direction of travel
   *     X and Y values for start of curve are always 0,0
   * @param cx1    X value for 1st control point
   * @param cy1    Y value for 1st control point
   * @param cx2    X value for 2nd control point
   * @param cy2    Y value for 2nd control point
   * @param x2     X value for end of curve
   * @param y2     Y value for end of curve
   *     Next set of parameters are for facing direction
   *     X and Y values for start of curve, as well as Y value for 1st control
   *     point, are all 0
   * @param cx3    X value for 1st control point
   * @param cx4    X value for 2nd control point
   * @param cy4    Y value for 2nd control point
   * @param x4     X value for end of curve
   * @param y4     Y value for end of curve
   */
  public Movement(double beats, int hands, double cx1, double cy1,
                  double cx2, double cy2, double x2, double y2,
                  double cx3, double cx4, double cy4, double x4, double y4)

  {
    this.beats = this.fullbeats = beats;
    this.hands = hands;
    this.cx1 = cx1;
    this.cx2 = cx2;
    this.cy2 = cy2;
    this.x2 = x2;
    this.y2 = y2;
    this.cx3 = cx3;
    this.cx4 = cx4;
    this.cy4 = cy4;
    this.x4 = x4;
    this.y4 = y4;
    recalculate();
  }

  /**
   * Clone a movement
   */
  public Movement(Movement m)
  {
    beats = fullbeats = m.beats;
    hands = m.hands;
    cx1 = m.cx1;
    cx2 = m.cx2;
    cy2 = m.cy2;
    x2 = m.x2;
    y2 = m.y2;
    cx3 = m.cx3;
    cx4 = m.cx4;
    cy4 = m.cy4;
    x4 = m.x4;
    y4 = m.y4;
    recalculate();
  }

  /**
   * Construct a Movement from the attributes of an XML movement
   * @param elem from xml
   */
  public Movement(Element elem)
  {
    beats = fullbeats = Double.valueOf(elem.getAttribute("beats"));
    hands = getHands(elem.getAttribute("hands"));
    cx1 = Double.valueOf(elem.getAttribute("cx1"));
    cy1 = Double.valueOf(elem.getAttribute("cy1"));
    cx2 = Double.valueOf(elem.getAttribute("cx2"));
    cy2 = Double.valueOf(elem.getAttribute("cy2"));
    x2 = Double.valueOf(elem.getAttribute("x2"));
    y2 = Double.valueOf(elem.getAttribute("y2"));
    if (elem.hasAttribute("cx3")) {
      this.cx3 = Double.valueOf(elem.getAttribute("cx3"));
      this.cx4 = Double.valueOf(elem.getAttribute("cx4"));
      this.cy4 = Double.valueOf(elem.getAttribute("cy4"));
      this.x4 = Double.valueOf(elem.getAttribute("x4"));
      this.y4 = Double.valueOf(elem.getAttribute("y4"));
    } else {
      cx3 = cx1;
      cx4 = cx2;
      cy4 = cy2;
      x4 = x2;
      y4 = y2;
    }
    recalculate();
  }

  /**
   * Return a matrix for the translation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
  public Matrix translate(double t)
  {
    Double tt = Math.min(Math.max(0,t),fullbeats);
    return btranslate.translate(tt/fullbeats);
  }
  public Matrix translate()
  {
    return translate(beats);
  }

  /**
   * Return a matrix for the rotation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
  public Matrix rotate(double t)
  {
    double tt = Math.min(Math.max(0,t),fullbeats);
    return brotate.rotate(tt / fullbeats);
  }
  public Matrix rotate()
  {
    return rotate(beats);
  }

  public void scale(double x, double y)
  {
    btranslate = new Bezier(0,0,btranslate.ctrlx1*x,
            btranslate.ctrly1*y,
            btranslate.ctrlx2*x,
            btranslate.ctrly2*y,
            btranslate.x2*x,
            btranslate.y2*y);
    brotate = new Bezier(0,0,brotate.ctrlx1*x,
            brotate.ctrly1*y,
            brotate.ctrlx2*x,
            brotate.ctrly2*y,
            brotate.x2*x,
            brotate.y2*y);
    if (y < 0) {
      if (this.hands == Movement.LEFTHAND)
        this.hands = Movement.RIGHTHAND;
      else if (this.hands == Movement.RIGHTHAND)
        this.hands = Movement.LEFTHAND;
    }
  }

  public void skew(double x, double y)
  {
    btranslate = new Bezier(0,0,btranslate.ctrlx1,
            btranslate.ctrly1,
            btranslate.ctrlx2+x,
            btranslate.ctrly2+y,
            btranslate.x2+x,
            btranslate.y2+y);
  }

  public void reflect()
  {
    scale(1,-1);
  }

  public void clip(double b)
  {
    if (b > 0 && b < fullbeats)
      beats = b;
  }

  /**
   *   Recalculate the Bezier curves from the points
   *   Must be called whenever the points are changed
   */
  public void recalculate()
  {
    this.btranslate = new Bezier(0.0,0.0,cx1,cy1,cx2,cy2,x2,y2);
    this.brotate = new Bezier(0.0,0.0,cx3,0.0,cx4,cy4,x4,y4);
  }

}
