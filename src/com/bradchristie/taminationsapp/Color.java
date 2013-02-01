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

  static public int darker(int c) {
    return argb(
        alpha(c),
        Math.round(red(c)*FACTOR),
        Math.round(green(c)*FACTOR),
        Math.round(blue(c)*FACTOR));
  }

  //  not sure if this is ok ..
  static public int rotate(int c) {
    int retval = 0;
    if (c == RED)
      retval = CYAN;
    else if (c == LTGRAY)
      retval = LTGRAY;
    else
      retval = BLUE;
    return retval;
  }
}
