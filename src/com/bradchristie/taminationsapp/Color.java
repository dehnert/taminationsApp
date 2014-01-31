/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2014 Brad Christie

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

/**
 * Since Android handles colors as packed ints of 4 values,
 * we will do the same here and define static methods that
 * work on ints.
 * @author brad
 *
 */

public class Color extends android.graphics.Color {

  static private float FACTOR = 0.7f;  // from java.awt.Color

  static public int ORANGE = 0xffffc800;

  static public int invert(int c)
  {
    return argb(alpha(c),255-red(c),255-green(c),255-blue(c));
  }

  static public int darker(int c, float f) {
    return argb(
        alpha(c),
        Math.round(red(c)*f),
        Math.round(green(c)*f),
        Math.round(blue(c)*f));
  }

  static public int darker(int c)
  {
    return darker(c,FACTOR);
  }

  static public int brighter(int c)
  {
    return invert(darker(invert(c)));
  }
  static public int brighter(int c, float f)
  {
    return invert(darker(invert(c),f));
  }

  static public int veryBright(int c)
  {
    return brighter(brighter(brighter(brighter(c))));
  }

}
