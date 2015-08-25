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

object Vector3D {

  def angleAngleDiff(a1:Double, a2:Double):Double =
    ((a1-a2 + Math.PI*3) % (Math.PI*2)) - Math.PI

}

class Vector3D(val x:Double, val y:Double, val z:Double = 0) {

  def length:Double = Math.sqrt(x*x+y*y+z*z)

  /**
   * @return  Angle off the X-axis
   */
  def angle:Double = Math.atan2(y,x)

  /**
   *   Returns difference angle between two vectors
   *   in the range of -pi to pi
   */
   def angleDiff(v:Vector3D): Double =
      Vector3D.angleAngleDiff(v.angle, angle)

   def vectorTo(v:Vector3D): Vector3D =
     new Vector3D(v.x-x, v.y-y, v.z-z)

  /**  Computes the cross product with another vector
   *
   * @param vector  2nd vector of cross product
   * @return   this x vector
   */
   def cross(vector:Vector3D): Vector3D =
     new Vector3D(y*vector.z - z*vector.y,
                  z*vector.x - x*vector.z,
                  x*vector.y - y*vector.x);

}
