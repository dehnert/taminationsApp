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

import android.graphics.Matrix;

public class Bezier {

  private float x1;
  private float y1;
  private float ctrlx1;
  private float ctrly1;
  private float ctrlx2;
  private float ctrly2;
  private float x2;
  private float y2;
  private float ax;
  private float bx;
  private float cx;
  private float ay;
  private float by;
  private float cy;

  Bezier(float x1, float y1, float ctrlx1, float ctrly1,
         float ctrlx2, float ctrly2, float x2, float y2)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.ctrlx1 = ctrlx1;
    this.ctrly1 = ctrly1;
    this.ctrlx2 = ctrlx2;
    this.ctrly2 = ctrly2;
    this.x2 = x2;
    this.y2 = y2;
    this.calculatecoefficients();
  }

  private void calculatecoefficients()
  {
    cx = 3.0f*(ctrlx1-x1);
    bx = 3.0f*(ctrlx2-ctrlx1) - cx;
    ax = x2 - x1 - cx - bx;

    cy = 3.0f*(ctrly1-y1);
    by = 3.0f*(ctrly2-ctrly1) - this.cy;
    ay = y2 - y1 - cy - by;
  }

  //  Return the movement along the curve given "t" between 0 and 1
  public Matrix translate(float t)
  {
    float x = x1 + t*(cx + t*(bx + t*ax));
    float y = y1 + t*(cy + t*(by + t*ay));
    Matrix retval = new Matrix();
    retval.postTranslate(x,y);
    return retval;
  }

  //  Return the angle of the derivative given "t" between 0 and 1
  public Matrix rotate(float t)
  {

    float x = cx + t*(2.0f*bx + t*3.0f*ax);
    float y = cy + t*(2.0f*by + t*3.0f*ay);
    //  No atan2 in FloatMath
    double theta = Math.atan2(y,x);
    Matrix retval = new Matrix();
    retval.postRotate((float)theta*180f/(float)Math.PI);
    return retval;
  };

  /*
    public String toString()
    {
      return '[ '+this.x1.toFixed(1)+' '+this.y1.toFixed(1)+' '+
                  this.ctrlx1.toFixed(1)+' '+this.ctrly1.toFixed(1)+' '+
                  this.ctrlx2.toFixed(1)+' '+this.ctrly2.toFixed(1)+' '+
                  this.x2.toFixed(1)+' '+this.y2.toFixed(1)+' ]';
    };
 */

}
