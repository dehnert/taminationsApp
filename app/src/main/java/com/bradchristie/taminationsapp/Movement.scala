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

package com.bradchristie.taminationsapp

import org.w3c.dom.Element

object Movement {
  val LEFTHAND:Int = 1
  val RIGHTHAND:Int = 2
  val BOTHHANDS:Int = 3
  val GRIPLEFT:Int = 5
  val GRIPRIGHT:Int = 6

  /**
   *   Translates a string describing hand use to one of the
   *   int constants above
   * @param h  String from XML hands parameter
   * @return   int constant used in this class
   */
  def getHands(h:String):Int =
    Map("none"->0, "left"->1, "right"->2, "both"->3, "anygrip"->4,
        "gripleft"->5, "gripright"->6, "gripboth"->7)(h)

  def apply(beats:Double, hands:Int,
     cx1:Double, cy1:Double, cx2:Double, cy2:Double, x2:Double, y2:Double,
     cx3:Double, cx4:Double, cy4:Double, x4:Double, y4:Double) =
    new Movement(beats,hands,cx1,cy1,cx2,cy2,x2,y2,cx3,cx4,cy4,x4,y4,beats)

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
  def apply(beats:Double, hands:Int,
     cx1:Double, cy1:Double, cx2:Double, cy2:Double, x2:Double, y2:Double) =
    new Movement(beats,hands,cx1,cx2,cy2,cy2,x2,y2,cx1,cx2,cy2,x2,y2,beats)

  /**
   * Construct a Movement from the attributes of an XML movement
   * @param elem from xml
   */
   def apply(elem:Element) =
     if (elem.hasAttribute("cx3"))
       new Movement(elem.getAttribute("beats").toDouble,
            Movement.getHands(elem.getAttribute("hands")),
            elem.getAttribute("cx1").toDouble,
            elem.getAttribute("cy1").toDouble,
            elem.getAttribute("cx2").toDouble,
            elem.getAttribute("cy2").toDouble,
            elem.getAttribute("x2").toDouble,
            elem.getAttribute("y2").toDouble,
            elem.getAttribute("cx3").toDouble,
            elem.getAttribute("cx4").toDouble,
            elem.getAttribute("cy4").toDouble,
            elem.getAttribute("x4").toDouble,
            elem.getAttribute("y4").toDouble,
            elem.getAttribute("beats").toDouble)
     else
       new Movement(elem.getAttribute("beats").toDouble,
            Movement.getHands(elem.getAttribute("hands")),
            elem.getAttribute("cx1").toDouble,
            elem.getAttribute("cy1").toDouble,
            elem.getAttribute("cx2").toDouble,
            elem.getAttribute("cy2").toDouble,
            elem.getAttribute("x2").toDouble,
            elem.getAttribute("y2").toDouble,
            elem.getAttribute("cx1").toDouble,
            elem.getAttribute("cx2").toDouble,
            elem.getAttribute("cy2").toDouble,
            elem.getAttribute("x2").toDouble,
            elem.getAttribute("y2").toDouble,
            elem.getAttribute("beats").toDouble)

}

  /**  Constructor for a movement where the dancer does not face the direction
   *   of travel.  Two Bezier curves are used, one for travel and one for
   *   facing direction.
   *
   * @param fullbeats  Timing
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
   * @param beats  Where to stop for a clipped movement
   */
class Movement(val fullbeats:Double, val hands:Int,
     val cx1:Double, val cy1:Double, val cx2:Double, val cy2:Double, val x2:Double, val y2:Double,
     val cx3:Double, val cx4:Double, val cy4:Double, val x4:Double, val y4:Double,
     val beats:Double) {

    val btranslate = new Bezier(0.0,0.0,cx1,cy1,cx2,cy2,x2,y2)
    val brotate = new Bezier(0.0,0.0,cx3,0.0,cx4,cy4,x4,y4)

  /**
   * Return a matrix for the translation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
   def translate(t:Double = beats):Matrix = {
      val tt = Math.min(Math.max(0,t),fullbeats)
      btranslate.translate(tt/fullbeats)
    }

  /**
   * Return a matrix for the rotation part of this movement at time t
   * @param t  Time in beats
   * @return   Matrix for using with canvas
   */
   def rotate(t:Double = beats):Matrix = {
     val tt = Math.min(Math.max(0,t),fullbeats)
     brotate.rotate(tt / fullbeats)
   }

   /**
    * Return a new movement by changing the beats
    */
   def time(b:Double):Movement =
     new Movement(b,hands,cx1,cy1,cx2,cy2,x2,y2,cx3,cx4,cy4,x4,y4,b)

   /**
    * Return a new movement by changing the hands
    */
   def handy(h:Int):Movement =
     new Movement(beats,h,cx1,cy1,cx2,cy2,x2,y2,cx3,cx4,cy4,x4,y4,beats)

   /**
    * Return a new Movement scaled by x and y factors.
    * If y is negative hands are also switched.
    */
   def scale(x:Double, y:Double):Movement =
     new Movement(beats,
         if (y < 0 && hands == Movement.RIGHTHAND) Movement.LEFTHAND
           else if (y < 0 && hands == Movement.LEFTHAND) Movement.RIGHTHAND
           else hands,  // what about GRIPLEFT, GRIPRIGHT?
         cx1*x,cy1*y,cx2*x,cy2*y,x2*x,y2*y,cx3*x,cx4*x,cy4*y,x4*x,y4*y,beats)
   /**
    * Return a new Movement with the end point shifted by x and y
    */
   def skew(x:Double, y:Double):Movement =
     new Movement(beats,hands,cx1,cy1,
         cx2+x,cy2+y,x2+x,y2+y,cx3,cx4,cy4,x4,y4,beats)

   def reflect:Movement = scale(1,-1)

   def clip(b:Double):Movement =
     new Movement(beats,hands,cx1,cy1,cx2,cy2,x2,y2,cx3,cx4,cy4,x4,y4,b)
}
