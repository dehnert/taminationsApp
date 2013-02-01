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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

public class SliderTicView extends View
{
  private float beats = 0f;
  private Rect candim = new Rect();
  private ColorDrawable cd = new ColorDrawable(0xff008000);
  private Paint p = new Paint();
  private float[] parts = null;

  public SliderTicView(Context context, AttributeSet attrs)
  {
    super(context,attrs);
  }

  protected void onDraw(Canvas c)
  {
    float x = 0f;
    //  Clear background
    c.getClipBounds(candim);
    cd.setBounds(candim);
    cd.draw(c);
    if (beats > 0f) {
      //  Draw tic marks
      p.setColor(Color.WHITE);
      p.setStrokeWidth(0f);
      for (float loc=1f; loc<beats; loc+=1f) {
        x = (float)candim.left+(float)candim.width()*loc/beats;
        c.drawLine(x, 0, x, 10, p);
      }
      //  Draw tic labels
      float y = 30f;
      x = (float)candim.left+(float)candim.width()*2f/beats;
      p.setTextSize(20f);
      p.setTextAlign(Paint.Align.CENTER);
      c.drawText("Start",x,y,p);
      x = (float)candim.left+(float)candim.width()*(beats-2f)/beats;
      c.drawText("End",x,y,p);
      if (parts != null) {
        String denom = String.valueOf(parts.length+1);
        for (int i=0; i<parts.length; i++) {
          String numer = String.valueOf(i+1);
          x = (float)candim.left+(float)candim.width()*(2f+parts[i])/beats;
          c.drawText(numer+"/"+denom,x,y,p);
        }
      }
    }
  }

  public void setTics(float b, String partstr)
  {
    beats = b;
    if (partstr.length() > 0) {
      String[] t = partstr.split(";");
      parts = new float[t.length];
      float s = 0f;
      for (int i=0; i<t.length; i++) {
        float p = Float.valueOf(t[i]);
        parts[i] = p + s;
        s += p;
      }
    }
    invalidate();
  }
}
