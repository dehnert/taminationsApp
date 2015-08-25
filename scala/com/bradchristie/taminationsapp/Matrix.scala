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

object Matrix {
  val IDENTITY:android.graphics.Matrix = new android.graphics.Matrix()
}

class Matrix(m:android.graphics.Matrix = new android.graphics.Matrix())
      extends android.graphics.Matrix(m)
{

  def copy:Matrix = new Matrix(this)

  def preRotate(radians:Double): Boolean =
    super.preRotate(Math.toDegrees(radians).toFloat)

  def postRotate(radians:Double): Boolean =
    super.postRotate(Math.toDegrees(radians).toFloat)

  def postTranslate(x:Double, y:Double): Boolean =
    super.postTranslate(x.toFloat,y.toFloat)

  def postScale(d:Double, e:Double): Boolean =
    super.postScale(d.toFloat,e.toFloat)

  def location: Vector3D = {
    val vm = Array(0.0f, 0.0f)
    super.mapPoints(vm)
    new Vector3D(vm(0),vm(1))
  }

  def direction: Vector3D = {
    val vm = Array(1.0f, 0.0f)
    super.mapVectors(vm)
    new Vector3D(vm(0),vm(1))
  }

  def angle: Double = {
    val vm = Array(1.0f, 0.0f)
    super.mapVectors(vm)
    Math.atan2(vm(1),vm(0))
  }

  def preConcat(m:Matrix):Matrix = {
    val m2:Matrix = copy
    (m2:android.graphics.Matrix).preConcat(m)
    m2
  }

}
