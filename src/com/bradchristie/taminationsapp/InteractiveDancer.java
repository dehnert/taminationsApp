package com.bradchristie.taminationsapp;

import java.util.List;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.MotionEvent;
import android.view.View;

public class InteractiveDancer extends Dancer
                               implements SensorEventListener
{

  public double userAngle = 0.0;   //  angle user is holding the device
  //private boolean snapped;
  private PointF leftTouch;
  private PointF leftMove;
  private PointF rightTouch;
  private PointF rightMove;
  private int leftid;
  private int rightid;
  public boolean onTrack;
  public String linaccstr = "";
  private double prevbeat = 0.0;
  private double G;
  private long Gcount = 0;
  private double yspeed;
  private double xspeed;
  private double prevangle;

  public Vector3D vacc;
  public Quaternion qrot;

  // for debugging
  public String debugstr1;
  public String debugstr2;
  public String debugstr3;
  public String debugstr4;
  private double minx,maxx;

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
      //snapped = true;
      prevbeat = beat;
    }
    else {
      //snapped = false;
      //  First save the user's current position
      Matrix savetx = new Matrix(tx);
      //  Compute where the dancer should be
      super.animate(beat);
      Matrix computetx = new Matrix(tx);
      //  Now restore the users' current position
      tx = new Matrix(savetx);

      //  Apply any additional movement and angle from the user
      //  This processes left and right touches
      if (leftMove != null) {
        double dx = -(leftMove.y - leftTouch.y)/20.0;
        double dy = -(leftMove.x - leftTouch.x)/20.0;
        tx.preTranslate(dx, dy);
        leftTouch = leftMove;
      }
      if (rightMove != null) {
        double da = -(rightMove.x - rightTouch.x)/100;
        tx.preRotate(da);
        rightTouch = rightMove;
      }

      //  This processes walking around
      if (qrot != null && vacc != null) {
        debugstr1 = vacc.toString();
        //  Start with three unit vectors
        Vector3D vvx = new Vector3D(1,0,0);
        Vector3D vvy = new Vector3D(0,1,0);
        Vector3D vvz = new Vector3D(0,0,1);
        //  Now rotate each to the device's current orientation
        vvx = vvx.rotate(qrot);
        vvy = vvy.rotate(qrot);
        vvz = vvz.rotate(qrot);
        //  The Z-coord of each vector will give the gravity force in that direction
        Vector3D vgrav = new Vector3D(vvx.z,vvy.z,vvz.z);
        //  Assume that the dancer is standing still until the beat > 0
        //  Use this time to get the G constant according to the device
        if (beat < 0.0) {
          G += vacc.length();
          Gcount++;
          xspeed = 0.0;
          yspeed = 0.0;
          prevangle = Math.atan2(vvy.y, vvy.x);
        }
        else if (Gcount > 0) {
          G = G/Gcount;
          Gcount = 0;
          minx = maxx = 0;
        }
        else {
          G = 0;
          //  Subtract gravity and process the remaining force
          Vector3D vforce = new Vector3D(vacc.x-vgrav.x*G,
                                         vacc.y-vgrav.y*G,
                                         vacc.z-vgrav.z*G);
          debugstr2 = vforce.toString();
          yspeed += vforce.x *(beat-prevbeat);
          float dy = (float)(yspeed*(beat-prevbeat));
          double xforce = Math.signum(vforce.y)*(new Vector3D(0,vforce.y,vforce.z)).length();
          xspeed += xforce*(beat-prevbeat);
          debugstr3 = String.format("%4.2f %4.2f",xspeed,yspeed);
          minx = Math.min(minx, vacc.x);
          maxx = Math.max(maxx,vacc.x);
          debugstr4 = String.format("%4.2f %4.2f",minx,maxx);
          float dx = (float)(xspeed*(beat-prevbeat));
          tx.preTranslate(dx,dy);
          //  Also use orientation to find user's rotation
          double angle = Math.atan2(vvy.y, vvy.x);
          tx.preRotate((float)((angle-prevangle)*180/Math.PI));
          prevangle = angle;
        }
      }

      prevbeat = beat;
      //  See how close the user is to the computed position
      float pu[] = {0.0f, 0.0f};
      tx.mapPoints(pu);
      float pc[] = {0.0f, 0.0f};
      computetx.mapPoints(pc);
      double dsq = (pu[0]-pc[0])*(pu[0]-pc[0]) + (pu[1]-pc[1])*(pu[1]-pc[1]);
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
      /*
      //  Computations for snapping
      //  Get points for each of these positions
      float[] p0 = {0.0f, 0.0f};
      savetx.mapPoints(p0);
      float[] p1 = {0.0f,0.0f};
      computetx.mapPoints(p1);
      float[] p2 = {0.0f,0.0f};
      tx.mapPoints(p2);
      //  Compute the distance between the user's and computed positions
      double dsq = (p1[0]-p2[0])*(p1[0]-p2[0]) + (p1[1]-p2[1])*(p1[1]-p2[1]);
      //  Compute the angle between the user's path and the computed path
      double[] v1 = {p1[0]-p0[0],p1[1]-p0[1]};
      double[] v2 = {p2[0]-p0[0],p2[1]-p0[1]};
      //Log.i(""+(Math.abs(v1[0])+Math.abs(v1[1])),
      //      ""+(Math.abs(v2[0])+Math.abs(v2[1])));
      if (dsq < 0.1 &&
          Math.abs(v1[0])+Math.abs(v1[1]) > 0.0 &&
          Math.abs(v2[0])+Math.abs(v2[1]) > 0.0) {
        double a1 = Math.atan2(v1[1],v1[0]);
        double a2 = Math.atan2(v2[1],v2[0]);
        //Log.i(""+a1,""+a2);
        //  If the angle is small (<45deg) then snap to the computed position
        if (Math.abs(a1-a2) < Math.PI/4) {
          //Log.i("","snapped");
          //tx = computetx;
          snapped = true;
        }
      }
      */
    }
  }


  public void doTouch(View v, MotionEvent m)
  {
    int action = m.getActionMasked();
    if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
      //  Touch down event
      //  Figure out if touching left or right side, and remember the point
      //  Also need to remember the "id" to correlate future move events
      int idx = m.getActionIndex();
      float x = m.getX(idx);
      float y = m.getY(idx);
      if (x < v.getWidth()/2.0) {
        leftTouch = new PointF(x,y);
        leftMove = leftTouch;
        leftid = m.getPointerId(idx);
      } else {
        rightTouch = new PointF(x,y);
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
          leftMove = new PointF(x,y);
        else if (id == rightid)
          rightMove = new PointF(x,y);
      }
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event)
  {
    if (event!=null && event.sensor!=null && event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
      vacc = new Vector3D(event.values[0],event.values[1],event.values[2]);
    }
    if (event!=null && event.sensor!=null && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
      qrot = new Quaternion(event.values[0],event.values[1],event.values[2]);
    }

  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy)
  {
    // we don't care
  }

}
