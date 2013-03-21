package com.bradchristie.taminationsapp;

import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public abstract class Geometry
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
  protected float prevangle = 0.0f;

  public Geometry(int r)
  {
    rotnum = r;
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
  abstract public Matrix pathMatrix(Matrix starttx, Matrix tx, float b);

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
    p.setARGB(255,0,0,0);
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
    float x = matvals[Matrix.MTRANS_X];
    float y = matvals[Matrix.MTRANS_Y];
    float r = MathF.sqrt(x*x+y*y);
    float startangle =
        MathF.toDegrees(MathF.atan2(matvals[Matrix.MSKEW_Y],matvals[Matrix.MSCALE_Y]));
    float angle = MathF.atan2(y,x)+MathF.PI;
    float bigangle = angle*2.0f-MathF.PI;
    x = r*MathF.cos(bigangle);
    y = r*MathF.sin(bigangle);
    startangle += MathF.toDegrees(angle);
    Matrix retval = new Matrix();
    retval.postRotate(startangle);
    retval.postTranslate(x,y);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, float beat)
  {
    //  Get dancer's start angle and current angle
    float[] matvals = new float[9];
    starttx.getValues(matvals);
    float x = matvals[Matrix.MTRANS_X];
    float y = matvals[Matrix.MTRANS_Y];
    float a0 = MathF.atan2(y,x);
    tx.getValues(matvals);
    x = matvals[Matrix.MTRANS_X];
    y = matvals[Matrix.MTRANS_Y];
    float a1 = MathF.atan2(y,x);
    if (beat <= 0.0)
      prevangle = a1;
    float wrap = MathF.round((a1-prevangle)/(MathF.PI*2.0f));
    a1 -= wrap*MathF.PI*2;
    float a2 = a1 - a0;
    Matrix m = new Matrix();
    m.postRotate(MathF.toDegrees(a2));
    prevangle = a1;
    return m;
  }

  static public void drawGrid(Canvas c)
  {
    Paint pline = gridPaint();
    for (float x1=-7.5f; x1<=7.5f; x1+=1.0f) {
      Path points = new Path();
      points.moveTo(MathF.abs(x1),0.0f);
      for (float y1=0.2f; y1<=7.5f; y1+=0.2f) {
        float a = 2.0f*MathF.atan2(y1,x1);
        float r = MathF.sqrt(x1*x1+y1*y1);
        float x = r*MathF.cos(a);
        float y = r*MathF.sin(a);
        points.lineTo(x,y);
      }
      c.drawPath(points,pline);
      Matrix m = new Matrix();
      m.postScale(-1.0f,1.0f);
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
    retval.postRotate(180f*rotnum);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, float b)
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
    //  TODO use radians instead of degrees?
    float a = 120f*rotnum;
    float[] matvals = new float[9];
    mat.getValues(matvals);
    float x = matvals[Matrix.MTRANS_X];
    float y = matvals[Matrix.MTRANS_Y];
    float r = MathF.sqrt(x*x+y*y);
    float startangle =
        MathF.toDegrees(MathF.atan2(matvals[Matrix.MSKEW_Y],matvals[Matrix.MSCALE_Y]));
    float angle = MathF.toDegrees(MathF.atan2(y,x));
    float dangle = angle < 0.0f ? -(180f+angle)/3f : (180f-angle)/3f;
    x = r * MathF.cos(MathF.toRadians(angle+dangle+a));
    y = r * MathF.sin(MathF.toRadians(angle+dangle+a));
    startangle += a + dangle;
    Matrix retval = new Matrix();
    retval.postRotate(startangle);
    retval.postTranslate(x,y);
    return retval;
  }

  @Override
  public Matrix pathMatrix(Matrix starttx, Matrix tx, float beat)
  {
    //  Get dancer's start angle and current angle
    float[] matvals = new float[9];
    starttx.getValues(matvals);
    float x = matvals[Matrix.MTRANS_X];
    float y = matvals[Matrix.MTRANS_Y];
    float a0 = MathF.atan2(y,x);
    tx.getValues(matvals);
    x = matvals[Matrix.MTRANS_X];
    y = matvals[Matrix.MTRANS_Y];
    float a1 = MathF.atan2(y,x);
    //  Correct for wrapping around +/- pi
    if (beat <= 0.0f)
      prevangle = a1;
    float wrap = MathF.round((a1-prevangle)/(MathF.PI*2.0f));
    a1 -= wrap*MathF.PI*2.0f;
    float a2 = -(a1-a0)/3.0f;
    Matrix m = new Matrix();
    m.postRotate(MathF.toDegrees(a2));
    prevangle = a1;
    return m;
  }

  static public void drawGrid(Canvas c)
  {
    //  Hex grid
    Paint pline = gridPaint();
    for (float x0=0.5f; x0<=8.5f; x0+=1.0f) {
      Path points = new Path();
      // moveto 0, x0
      points.moveTo(0.0f,x0);
      for (float y0=0.5f; y0<=8.5f; y0+=0.5f) {
        float a = MathF.atan2(y0,x0)*2f/3f;
        float r = MathF.sqrt(x0*x0+y0*y0);
        float x = r*MathF.sin(a);
        float y = r*MathF.cos(a);
        // lineto x,y
        points.lineTo(x,y);
      }
      //  rotate and reflect the result
      Path p = new Path();
      for (float a=0.0f; a<6.0f; a+=1.0f) {
        Matrix m = new Matrix();
        m.postRotate(30.0f+a*60.0f);
        points.transform(m,p);
        c.drawPath(p,pline);
        m.postScale(1.0f,-1.0f);
        points.transform(m,p);
        c.drawPath(p,pline);
      }
    }

  }

}
