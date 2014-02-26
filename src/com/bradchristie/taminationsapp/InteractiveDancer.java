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

import java.util.List;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

public class InteractiveDancer extends Dancer
{
  private boolean primaryIsLeft;
  private PointF primaryTouch;
  private PointF primaryMove;
  private PointF secondaryTouch;
  private PointF secondaryMove;
  private Vector3D primaryDirection;
  private int primaryid;
  private int secondaryid;
  public boolean onTrack;

  //  These numbers control the touch sensitivity
  private final double LEFTSENSITIVITY = 0.02;
  private final double RIGHTSENSITIVITY = 0.02;
  private final double DIRECTIONALPHA = 0.9;
  private final double DIRECTIONTHRESHOLD = 0.01;

  public InteractiveDancer(String n, String nc, int g, int c, Matrix mat,
                           Geometry geometry, List<Movement> moves,
                           String primaryControl)
  {
    super(n, nc, g, c, mat, geometry, moves);
    primaryid = -1;
    secondaryid = -1;
    primaryIsLeft = primaryControl.contains("Left");
    float[] vec = {1.0f, 0.0f};
    mat.mapVectors(vec);
  }

  public Matrix computeMatrix(double beat)
  {
    Matrix savetx = new Matrix(tx);
    super.animate(beat);
    Matrix computetx = tx;
    tx = savetx;
    return computetx;
  }

  public void animate(double beat)
  {
    if (beat <= 0.0 || onTrack)
      fillColor = Color.veryBright(drawColor);
    else
      fillColor = Color.GRAY;
    if (tx == null || beat <= -2.0) {
      tx = new Matrix(starttx);
    }
    else {

      //  Apply any additional movement and angle from the user
      //  This processes left and right touches
      if (primaryMove != null) {
        double dx = -(primaryMove.y - primaryTouch.y) * LEFTSENSITIVITY;
        double dy = -(primaryMove.x - primaryTouch.x) * LEFTSENSITIVITY;
        tx.postTranslate(dx, dy);
        primaryTouch = primaryMove;
        if (secondaryMove == null) {
          //  Right finger is up - rotation follows movement
          if (primaryDirection == null)
            primaryDirection = new Vector3D(dx,dy,0);
          else {
            primaryDirection.x = DIRECTIONALPHA * primaryDirection.x +
                              (1-DIRECTIONALPHA) * dx;
            primaryDirection.y = DIRECTIONALPHA * primaryDirection.y +
                              (1-DIRECTIONALPHA) * dy;
          }
          if (primaryDirection.length() >= DIRECTIONTHRESHOLD) {
            float vm[] = {1.0f, 0.0f};
            tx.mapVectors(vm);
            double a1 = Math.atan2(vm[1],vm[0]);
            double a2 = Math.atan2(primaryDirection.y,primaryDirection.x);
            tx.preRotate(a2-a1);
          }
        }
      }
      if (secondaryMove != null) {
        //  Rotation follow right finger
        //  Get the vector of the user's finger
        double dx = -(secondaryMove.y - secondaryTouch.y) * RIGHTSENSITIVITY;
        double dy = -(secondaryMove.x - secondaryTouch.x) * RIGHTSENSITIVITY;
        Vector3D vf = new Vector3D(dx,dy,0);
        //  Get the vector the dancer is facing
        Vector3D vu = tx.direction();
        //  Amount of rotation is z of the cross product of the two
        double da = vu.cross(vf).z;
        tx.preRotate(da);
        secondaryTouch = secondaryMove;
      }
    }
  }


  public void doTouch(View v, MotionEvent m)
  {
    int action = m.getActionMasked();
    float s = 500f/v.getHeight();
    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
      //  Touch down event
      //  Figure out if touching left or right side, and remember the point
      //  Also need to remember the "id" to correlate future move events
      int idx = m.getActionIndex();
      float x = m.getX(idx);
      float y = m.getY(idx);
      if (x > v.getWidth()/2.0 ^ primaryIsLeft) {
        primaryTouch = new PointF(x*s,y*s);
        primaryMove = primaryTouch;
        primaryid = m.getPointerId(idx);
        primaryDirection = null;
      } else {
        secondaryTouch = new PointF(x*s,y*s);
        secondaryMove = secondaryTouch;
        secondaryid = m.getPointerId(idx);
      }
    }
    else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
      //  Touch up event
      //  Stop moving and rotating
      int idx = m.getActionIndex();
      int id = m.getPointerId(idx);
      if (id == primaryid) {
        primaryid = -1;
        primaryMove = null;
      }
      else if (id == secondaryid) {
        secondaryid = -1;
        secondaryMove = null;
      }
    }
    else if (action == MotionEvent.ACTION_MOVE) {
      //  Movements
      //  It could be sending move events for both sides, so need to loop through
      for (int i=0; i<m.getPointerCount(); i++) {
        int id = m.getPointerId(i);
        float x = m.getX(i);
        float y = m.getY(i);
        if (id == primaryid)
          primaryMove = new PointF(x*s,y*s);
        else if (id == secondaryid)
          secondaryMove = new PointF(x*s,y*s);
      }
    }
  }

}
