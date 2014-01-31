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

import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public abstract class Geometry implements Cloneable
{
  static final public int BIGON = 1;
  static final public int SQUARE = 2;
  static final public int HEXAGON = 3;
  /**
   * rotnum can range from 0 to symmetry-1
   */
  public int rotnum;
  /**
   * used for computing dancer path
   */
  protected double prevangle = 0.0;

  public Geometry(int r)
  {
    rotnum = r;
  }
  public Geometry clone()
  {
    Geometry retval = null;
    try {
      retval = (Geometry)super.clone();
    } catch (CloneNotSupportedException e) {
      // can never happen because this class implements Cloneable
      e.printStackTrace();
    }
    return retval;
  }

  /**
   * Generate a transform to apply to a dancer's start position
   * @param sym
   * @return
   */
  abstract public Matrix startMatrix(Matrix mat);
  /**
   * Convert transform for a dancer's current position
   * @param sym
   * @return
   */
  abstract public Matrix pathMatrix(Matrix starttx, Matrix tx, double b);

  /**
   * Draw a dancer-sized grid of the specific geometry
   * @param g  Geometry
   * @param c  Canvas to draw grid on
   */
  static public void drawGrid(int g, Canvas c)
  {
    if (g == BIGON)
      BigonGeometry.drawGrid(c);
    else if (g == SQUARE)
      SquareGeometry.drawGrid(c);
    else if (g == HEXAGON)
      HexagonGeometry.drawGrid(c);
  }

  /**
   * Factory method to get a geometry object
   * @param sym  One of BIGON, SQUARE, HEXAGON
   * @return  Geometry object to compute symmetric dancer locations
   */
  static public Vector<Geometry> getGeometry(int sym)
  {
    Vector<Geometry> retval = new Vector<Geometry>();
    if (sym == BIGON)
      retval.add(new BigonGeometry(0));
    else if (sym == SQUARE) {
      retval.add(new SquareGeometry(0));
      retval.add(new SquareGeometry(1));
    }
    else if (sym == HEXAGON) {
      retval.add(new HexagonGeometry(0));
      retval.add(new HexagonGeometry(1));
      retval.add(new HexagonGeometry(2));
    }
    return retval;
  }

  /**
   * @return  a Paint object for drawing grid lines
   */
  static protected Paint gridPaint()
  {
    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    p.setARGB(255,192,192,192);
    p.setStyle(Style.STROKE);
    p.setStrokeWidth(0f);
    return p;
  }

}

/////  Specific Geometry classes  //////
class BigonGeometry extends Geometry
{
  public BigonGeometry(int r)
  {
    super(r);
  }

  @Override
  public Matrix startMatrix(Matrix mat)
  {
    float[] matvals = new float[9];
    mat.getValues(matvals);
    double x = matvals[Matrix.MTRANS_X];
    double y = matvals[Matrix.MTRANS_Y];
    double r = Math.sqrt(x*x+y*y);
    double startangle =
        Math.atan2(matvals[Matrix.MSKEW_Y],matvals[Matrix.MSCALE_Y]);
    double angle = Math.atan2(y,x)+Math.PI;
    double bigangle = angle*2.0f-Math.PI;
    x = r*Math.cos(bigangle);
    y = r*Math.sin(bigangle);
    startangle += angle;
    Matrix retval = new Matrix();
    retval.postRotate(startangle);
    retval.postTranslate(x,y);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, double beat)
  {
    //  Get dancer's start angle and current angle
    float[] matvals = new float[9];
    starttx.getValues(matvals);
    double x = matvals[Matrix.MTRANS_X];
    double y = matvals[Matrix.MTRANS_Y];
    double a0 = Math.atan2(y,x);
    tx.getValues(matvals);
    x = matvals[Matrix.MTRANS_X];
    y = matvals[Matrix.MTRANS_Y];
    double a1 = Math.atan2(y,x);
    if (beat <= 0.0)
      prevangle = a1;
    double wrap = Math.round((a1-prevangle)/(Math.PI*2));
    a1 -= wrap*Math.PI*2;
    double a2 = a1 - a0;
    Matrix m = new Matrix();
    m.postRotate(a2);
    prevangle = a1;
    return m;
  }

  static public void drawGrid(Canvas c)
  {
    Paint pline = gridPaint();
    for (double x1=-7.5f; x1<=7.5f; x1+=1.0f) {
      Path points = new Path();
      points.moveTo((float)Math.abs(x1),0.0f);
      for (float y1=0.2f; y1<=7.5f; y1+=0.2f) {
        double a = 2.0f*Math.atan2(y1,x1);
        double r = Math.sqrt(x1*x1+y1*y1);
        double x = r*Math.cos(a);
        double y = r*Math.sin(a);
        points.lineTo((float)x,(float)y);
      }
      c.drawPath(points,pline);
      Matrix m = new Matrix();
      m.postScale(-1.0,1.0);
      points.transform(m);
      c.drawPath(points,pline);
    }

  }

}

//////////////////////////////////////
class SquareGeometry extends Geometry
{
  public SquareGeometry(int r)
  {
    super(r);
  }

  @Override
  public Matrix startMatrix(Matrix mat)
  {
    Matrix retval = new Matrix(mat);
    retval.postRotate(Math.PI*rotnum);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, double b)
  {
    //  No additional transform needed for squares
    return new Matrix();
  }

  static public void drawGrid(Canvas c)
  {
    Paint pline = gridPaint();
    for (float x=-7.5f; x<=7.5f; x+=1.0f)
      c.drawLine(x,-7.5f,x,7.5f,pline);
    for (float y=-7.5f; y<=7.5f; y+=1.0f)
      c.drawLine(-7.5f,y,7.5f,y,pline);
  }

}

///////////////////////////////////////
class HexagonGeometry extends Geometry
{
  public HexagonGeometry(int r)
  {
    super(r);
  }

  @Override
  public Matrix startMatrix(Matrix mat)
  {
    double a = (Math.PI*2/3)*rotnum;
    float[] matvals = new float[9];
    mat.getValues(matvals);
    double x = matvals[Matrix.MTRANS_X];
    double y = matvals[Matrix.MTRANS_Y];
    double r = Math.sqrt(x*x+y*y);
    double startangle =
        Math.atan2(matvals[Matrix.MSKEW_Y],matvals[Matrix.MSCALE_Y]);
    double angle = Math.atan2(y,x);
    double dangle = angle < 0.0 ? -(Math.PI+angle)/3 : (Math.PI-angle)/3;
    x = r * Math.cos(angle+dangle+a);
    y = r * Math.sin(angle+dangle+a);
    startangle += a + dangle;
    Matrix retval = new Matrix();
    retval.postRotate(startangle);
    retval.postTranslate(x,y);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, double beat)
  {
    //  Get dancer's start angle and current angle
    float[] matvals = new float[9];
    starttx.getValues(matvals);
    double x = matvals[Matrix.MTRANS_X];
    double y = matvals[Matrix.MTRANS_Y];
    double a0 = Math.atan2(y,x);
    tx.getValues(matvals);
    x = matvals[Matrix.MTRANS_X];
    y = matvals[Matrix.MTRANS_Y];
    double a1 = Math.atan2(y,x);
    //  Correct for wrapping around +/- pi
    if (beat <= 0.0)
      prevangle = a1;
    double wrap = Math.round((a1-prevangle)/(Math.PI*2));
    a1 -= wrap*Math.PI*2;
    double a2 = -(a1-a0)/3;
    Matrix m = new Matrix();
    m.postRotate(a2);
    prevangle = a1;
    return m;
  }

  static public void drawGrid(Canvas c)
  {
    //  Hex grid
    Paint pline = gridPaint();
    for (double x0=0.5; x0<=8.5; x0+=1) {
      Path points = new Path();
      // moveto 0, x0
      points.moveTo(0.0f,(float)x0);
      for (double y0=0.5; y0<=8.5; y0+=0.5) {
        double a = Math.atan2(y0,x0)*2/3;
        double r = Math.sqrt(x0*x0+y0*y0);
        double x = r*Math.sin(a);
        double y = r*Math.cos(a);
        // lineto x,y
        points.lineTo((float)x,(float)y);
      }
      //  rotate and reflect the result
      Path p = new Path();
      for (double a=0.0; a<6.0; a+=1.0) {
        Matrix m = new Matrix();
        m.postRotate(Math.PI/6+a*Math.PI/3);
        points.transform(m,p);
        c.drawPath(p,pline);
        m.postScale(1.0,-1.0);
        points.transform(m,p);
        c.drawPath(p,pline);
      }
    }

  }

}
