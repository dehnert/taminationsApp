/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2013 Brad Christie

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

import android.graphics.Matrix;

public class Movement {

  static public final int NOHANDS = 0;
  static public final int LEFTHAND = 1;
  static public final int RIGHTHAND = 2;
  static public final int BOTHHANDS = 3;
  static public final int GRIPLEFT = 5;
  static public final int GRIPRIGHT = 6;
  static public final int GRIPBOTH =  7;
  static public final int ANYGRIP =  4;

  public double beats;
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

  public Movement(double beats, int hands, double cx1, double cy1,
                  double cx2, double cy2, double x2, double y2)
  {
    this.beats = beats;
    this.hands = hands;
    this.cx1 = this.cx3 = cx1;
    this.cy1 = cy1;
    this.cx2 = this.cx4 = cx2;
    this.cy2 = this.cy4 = cy2;
    this.x2 = this.x4 = x2;
    this.y2 = this.y4 = y2;
    recalculate();
  }

  public Movement(double beats, int hands, double cx1, double cy1,
                  double cx2, double cy2, double x2, double y2,
                  double cx3, double cx4, double cy4, double x4, double y4)

  {
    this.beats = beats;
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

  public Movement(Element elem)
  {
    beats = Double.valueOf(elem.getAttribute("beats"));
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
    Double tt = Math.min(Math.max(0,t),beats);
    return btranslate.translate(tt/beats);
  };

  /**
   * Return a matrix for the rotation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
  public Matrix rotate(double t)
  {
    double tt = Math.min(Math.max(0,t),beats);
    return brotate.rotate(tt/beats);
  };

  public void recalculate()
  {
    this.btranslate = new Bezier(0.0,0.0,cx1,cy1,cx2,cy2,x2,y2);
    this.brotate = new Bezier(0.0,0.0,cx3,0.0,cx4,cy4,x4,y4);
  }

}
