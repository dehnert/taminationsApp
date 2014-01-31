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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

public class SliderTicView extends View
{
  private double beats = 0f;
  private Rect candim = new Rect();
  private ColorDrawable cd = new ColorDrawable(0xff008000);
  private Paint p = new Paint();
  private double[] parts = null;

  public SliderTicView(Context context, AttributeSet attrs)
  {
    super(context,attrs);
  }

  protected void onDraw(Canvas c)
  {
    double x = 0.0;
    //  Clear background
    c.getClipBounds(candim);
    cd.setBounds(candim);
    cd.draw(c);
    if (beats > 0.0) {
      //  Draw tic marks
      p.setColor(Color.WHITE);
      p.setStrokeWidth(0f);
      for (double loc=1.0; loc<beats; loc+=1.0) {
        x = candim.left+candim.width()*loc/beats;
        c.drawLine((float)x, 0f, (float)x, 10f, p);
      }
      //  Draw tic labels
      double y = 30.0;
      x = candim.left+candim.width()*2f/beats;
      p.setTextSize(20f);
      p.setTextAlign(Paint.Align.CENTER);
      c.drawText("Start",(float)x,(float)y,p);
      x = candim.left+candim.width()*(beats-2.0)/beats;
      c.drawText("End",(float)x,(float)y,p);
      if (parts != null) {
        String denom = String.valueOf(parts.length+1);
        for (int i=0; i<parts.length; i++) {
          String numer = String.valueOf(i+1);
          x = candim.left+candim.width()*(2.0+parts[i])/beats;
          c.drawText(numer+"/"+denom,(float)x,(float)y,p);
        }
      }
    }
  }

  public void setTics(double b, String partstr)
  {
    beats = b;
    if (partstr.length() > 0) {
      String[] t = partstr.split(";");
      parts = new double[t.length];
      double s = 0f;
      for (int i=0; i<t.length; i++) {
        double p = Float.valueOf(t[i]);
        parts[i] = p + s;
        s += p;
      }
    }
    invalidate();
  }
}
