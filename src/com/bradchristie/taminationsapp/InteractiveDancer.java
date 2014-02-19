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

  private PointF leftTouch;
  private PointF leftMove;
  private PointF rightTouch;
  private PointF rightMove;
  private int leftid;
  private int rightid;
  public boolean onTrack;

  //  These numbers control the touch sensitivity
  private final double LEFTSENSITIVITY = 0.02;
  private final double RIGHTSENSITIVITY = 0.02;

  public InteractiveDancer(String n, String nc, int g, int c, Matrix mat,
                           Geometry geometry, List<Movement> moves)
  {
    super(n, nc, g, c, mat, geometry, moves);
    leftid = -1;
    rightid = -1;
    float[] vec = {1.0f, 0.0f};
    mat.mapVectors(vec);
  }

  public void animate(double beat)
  {
    fillColor = Color.veryBright(drawColor);
    onTrack = true;
    if (beat <= -2.0) {
      tx = new Matrix(starttx);
    }
    else {
      //  First save the user's current position
      Matrix savetx = new Matrix(tx);
      //  Compute and rememeber where the dancer should be
      super.animate(beat);
      Matrix computetx = new Matrix(tx);
      //  Now restore the user's current position
      tx = savetx;

      //  Apply any additional movement and angle from the user
      //  This processes left and right touches
      if (leftMove != null) {
        double dx = -(leftMove.y - leftTouch.y) * LEFTSENSITIVITY;
        double dy = -(leftMove.x - leftTouch.x) * LEFTSENSITIVITY;
        tx.preTranslate(dx, dy);
        leftTouch = leftMove;
      }
      if (rightMove != null) {
        double da = -(rightMove.x - rightTouch.x) * RIGHTSENSITIVITY;
        tx.preRotate(da);
        rightTouch = rightMove;
      }

      //  See how close the user is to the computed position
      //  Compute difference in location
      float pu[] = {0.0f, 0.0f};
      tx.mapPoints(pu);
      float pc[] = {0.0f, 0.0f};
      computetx.mapPoints(pc);
      double dsq = (pu[0]-pc[0])*(pu[0]-pc[0]) + (pu[1]-pc[1])*(pu[1]-pc[1]);
      //  Compute difference in angle
      float vu[] = {1.0f, 0.0f};
      tx.mapVectors(vu);
      float vc[] = {1.0f, 0.0f};
      computetx.mapVectors(vc);
      //  Cheap way of testing for diff angle < 45 degrees
      double vsq = (vu[0]-vc[0])*(vu[0]-vc[0]) + (vu[1]-vc[1])*(vu[1]-vc[1]);
      if (dsq > 1.0 || vsq > (2.0-Math.sqrt(2.0))) {
        onTrack = false;
        fillColor = Color.GRAY;
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
      if (x < v.getWidth()/2.0) {
        leftTouch = new PointF(x*s,y*s);
        leftMove = leftTouch;
        leftid = m.getPointerId(idx);
      } else {
        rightTouch = new PointF(x*s,y*s);
        rightMove = rightTouch;
        rightid = m.getPointerId(idx);
      }
    }
    else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
      //  Touch up event
      //  Stop moving and rotating
      int idx = m.getActionIndex();
      int id = m.getPointerId(idx);
      if (id == leftid) {
        leftid = -1;
      }
      else if (id == rightid) {
        rightid = -1;
      }
    }
    else if (action == MotionEvent.ACTION_MOVE) {
      //  Movements
      //  It could be sending move events for both sides, so need to loop through
      for (int i=0; i<m.getPointerCount(); i++) {
        int id = m.getPointerId(i);
        float x = m.getX(i);
        float y = m.getY(i);
        if (id == leftid)
          leftMove = new PointF(x*s,y*s);
        else if (id == rightid)
          rightMove = new PointF(x*s,y*s);
      }
    }
  }

}
