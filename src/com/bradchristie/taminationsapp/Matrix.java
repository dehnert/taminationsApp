package com.bradchristie.taminationsapp;

public class Matrix extends android.graphics.Matrix
{
  public Matrix()
  {
    super();
  }

  public Matrix(Matrix from)
  {
    super(from);
  }

  public boolean preRotate(double radians)
  {
    return preRotate((float)(Math.toDegrees(radians)));
  }

  public boolean postRotate(double radians)
  {
    return postRotate((float)(Math.toDegrees(radians)));
  }

  public boolean preTranslate(double x, double y)
  {
    return preTranslate((float)x,(float)y);
  }

  public boolean postTranslate(double x, double y)
  {
    return postTranslate((float)x,(float)y);
  }

  public boolean postScale(double d, double e)
  {
    return postScale((float)d,(float)e);
  }



}
