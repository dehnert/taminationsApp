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

  public float beats;
  public int hands;
  public float cx1;
  public float cy1;
  public float cx2;
  public float cy2;
  public float x2;
  public float y2;
  public float cx3;
  public float cx4;
  public float cy4;
  public float x4;
  public float y4;
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

  public Movement(float beats, int hands, float cx1, float cy1,
                  float cx2, float cy2, float x2, float y2)
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

  public Movement(float beats, int hands, float cx1, float cy1,
                  float cx2, float cy2, float x2, float y2,
                  float cx3, float cx4, float cy4, float x4, float y4)

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
    beats = Float.valueOf(elem.getAttribute("beats"));
    hands = getHands(elem.getAttribute("hands"));
    cx1 = Float.valueOf(elem.getAttribute("cx1"));
    cy1 = Float.valueOf(elem.getAttribute("cy1"));
    cx2 = Float.valueOf(elem.getAttribute("cx2"));
    cy2 = Float.valueOf(elem.getAttribute("cy2"));
    x2 = Float.valueOf(elem.getAttribute("x2"));
    y2 = Float.valueOf(elem.getAttribute("y2"));
    if (elem.hasAttribute("cx3")) {
      this.cx3 = Float.valueOf(elem.getAttribute("cx3"));
      this.cx4 = Float.valueOf(elem.getAttribute("cx4"));
      this.cy4 = Float.valueOf(elem.getAttribute("cy4"));
      this.x4 = Float.valueOf(elem.getAttribute("x4"));
      this.y4 = Float.valueOf(elem.getAttribute("y4"));
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
  public Matrix translate(float t)
  {
    float tt = Math.min(Math.max(0,t),beats);
    return btranslate.translate(tt/beats);
  };

  /**
   * Return a matrix for the rotation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
  public Matrix rotate(float t)
  {
    float tt = Math.min(Math.max(0,t),beats);
    return this.brotate.rotate(tt/beats);
  };

  public void recalculate()
  {
    this.btranslate = new Bezier(0f,0f,cx1,cy1,cx2,cy2,x2,y2);
    this.brotate = new Bezier(0f,0f,cx3,0f,cx4,cy4,x4,y4);
  }

  public String toString()
  {
    return beats+" "+x2+" "+y2;
  }
}
