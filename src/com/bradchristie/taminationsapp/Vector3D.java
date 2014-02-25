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

public class Vector3D
{
  public double x;
  public double y;
  public double z;

  public Vector3D(double x, double y, double z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3D(double x, double y)
  {
    this.x = x;
    this.y = y;
    this.z = 0;
  }

  public Quaternion postMultiply(Quaternion m)
  {
    return new Quaternion(x*m.w+y*m.z-z*m.y,
                         -x*m.z+y*m.w+z*m.x,
                          x*m.y-y*m.x+z*m.w,
                         -x*m.x-y*m.y-z*m.z);
  }

  public Vector3D rotate(Quaternion qrot)
  {
    Quaternion q = qrot.postMultiply(this).postMultiply(qrot.inverse());
    return new Vector3D(q.x,q.y,q.z);
  }

  public double length()
  {
    return Math.sqrt(x*x+y*y+z*z);
  }

  /**
   * @return  Angle off the X-axis
   */
  public double angle()
  {
    return Math.atan2(y,x);
  }

  /**
   *   Returns difference angle between two vectors
   *   in the range of -pi to pi
   * @param v
   * @return
   */
  public static double angleDiff(double a1, double a2)
  {
    return ((a1-a2 + Math.PI*3) % (Math.PI*2)) - Math.PI;
  }
  public double angleDiff(Vector3D v)
  {
    return angleDiff(v.angle(),angle());
  }

  public Vector3D vectorTo(Vector3D v)
  {
    return new Vector3D(v.x-x,v.y-y,v.z-z);
  }

  /**  Computes the cross product with another vector
   *
   * @param vector  2nd vector of cross product
   * @return   this x vector
   */
  public Vector3D cross(Vector3D vector)
  {
    return new Vector3D(y*vector.z - z*vector.y,
                        z*vector.x - x*vector.z,
                        x*vector.y - y*vector.x);
  }

}
