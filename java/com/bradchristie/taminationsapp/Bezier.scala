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

class Bezier(val x1:Double, val y1:Double,
             val ctrlx1:Double, val ctrly1:Double,
             val ctrlx2:Double, val ctrly2:Double,
             val x2:Double, val y2:Double)
{
  private val cx = 3.0*(ctrlx1-x1)
  private val bx = 3.0*(ctrlx2-ctrlx1) - cx
  private val ax = x2 - x1 - cx - bx

  private val cy = 3.0*(ctrly1-y1)
  private val by = 3.0*(ctrly2-ctrly1) - cy
  private val ay = y2 - y1 - cy - by

  //  Return the movement along the curve given "t" between 0 and 1
  def translate(t:Double):Matrix = {
    val x = x1 + t*(cx + t*(bx + t*ax))
    val y = y1 + t*(cy + t*(by + t*ay))
    val retval = new Matrix()
    retval.postTranslate(x,y)
    retval
  }

  def rotate(t:Double):Matrix = {
    val x = cx + t*(2.0*bx + t*3.0*ax)
    val y = cy + t*(2.0*by + t*3.0*ay)
    val theta = Math.atan2(y,x)
    val retval = new Matrix()
    retval.postRotate(theta)
    retval
  }


}
