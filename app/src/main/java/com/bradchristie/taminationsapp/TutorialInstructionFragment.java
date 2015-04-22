/*

    Taminations Square Dance Animations App for Android
    Copyright (C) 2015 Brad Christie

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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class TutorialInstructionFragment extends DialogFragment
               implements DialogInterface.OnClickListener
{
  private String message;
  private String title;
  private TutorialActivity.StartTutorialInstruction starter;

  public void setMessage(String message)
  {
    this.message = message;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setStarter(TutorialActivity.StartTutorialInstruction starter)
  {
    this.starter = starter;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
      // Use the Builder class for convenient dialog construction
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(title)
             .setMessage(message)
             .setPositiveButton("Ok", this);
      // Create the AlertDialog object and return it
      return builder.create();
  }

  public void onClick(DialogInterface dialog, int id)
  {
    if (starter != null)
      starter.start();
  }
}
