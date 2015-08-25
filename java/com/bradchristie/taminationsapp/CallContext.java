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

import org.w3c.dom.Element;

public class CallContext
{

  public Dancer dancers[];
  public Dancer actives[];
  public String callname;

  public CallContext(CallContext from) {
    dancers = new Dancer[from.dancers.length];
    for (int i=0; i<from.dancers.length; i++)
      dancers[i] = new Dancer(from.dancers[i]);
  }

  public CallContext(Dancer[] dancers) {
    dancers = new Dancer[dancers.length];
    for (int i=0; i<dancers.length; i++)
      dancers[i] = new Dancer(dancers[i]);
  }

  //  other constructors?? //

  /**
   *   Append the result of processing this CallContext to it source.
   *   The CallContext must have been previously cloned from the source.
   */
  public void appendToSource()
  {
    for (Dancer d : dancers) {
      //d.clonedFrom.path.add(d.path);
      d.clonedFrom.animateToEnd();
    }
  }

  public void applyCalls(String... calls)
  {
    for (String s : calls) {
      CallContext ctx = new CallContext(this);
      ctx.interpretCall(s);
      ctx.performCall();
      ctx.appendToSource();
    }
  }

  /**
   * This is the main loop for interpreting a call
   * @param calltext  One complete call, lower case, words separated by single spaces
   */
  public void interpretCall(String calltext)
  {
    CallError err = new CallNotFoundError();
    //  Clear out any previous paths from incomplete parsing
    for (Dancer d : dancers)
      d.path = new Path();
    callname = "";
    //  If a partial interpretation is found (like 'boys' of 'boys run')
    //  it gets popped off the front and this loop interprets the rest
    while (calltext.length() > 0) {

    }
  }

  //  Given a context and string, try to find an XML animation
  //  If found, the call is added to the context
  public boolean matchXMLcall(String calltext)
  {
    return true;
  }

  /**
   *   Reads an XML formation and returns array of the dancers
   * @param formation   XML formation element
   * @return Array of dancers
   */
  public Dancer[] getDancers(Element formation)
  {
    return null;
  }

  // ... lots more stuff ...


  public void performCall()
  {

  }


}
