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

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.w3c.dom.Element;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import java.io.File;
import java.io.IOException;

public class SequenceActivity extends RotationActivity
  implements RecognitionListener
{

  private static final String CALL_SEARCH = "call";
  private CallsAdapter ca;
  private SpeechRecognizer sr;
  private ListView lv;
  private AnimationView av;

  /**
   *    Called once after Activity is started
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sequence);
    setTitle("Taminations");
  }

  /**
   *   Called whenever this activity is re-displayed
   */
  @Override
  protected void onResume() {
    super.onResume();

    ca = new CallsAdapter(this);
    lv = (ListView)findViewById(R.id.sequenceListView);
    lv.setAdapter(ca);

    try {
      Assets assets = new Assets(this);
      File assetsDir = assets.syncAssets();
      sr = defaultSetup()
              .setAcousticModel(new File(assetsDir, "en-us-ptm"))
              .setDictionary(new File(assetsDir, "8059.dic"))
              // Threshold to tune for keyphrase to balance between false alarms and misses
              .setKeywordThreshold(1e-45f)
              // Use context-independent phonetic search,
              // context-dependent is too slow for mobile
              .setBoolean("-allphone_ci", true)
              .getRecognizer();
      // Create language model search
      File languageModel = new File(assetsDir, "8059.lm");
      sr.addNgramSearch(CALL_SEARCH, languageModel);
      sr.addListener(this);
      startSequence();
      sr.startListening(CALL_SEARCH);
    } catch (IOException e) {
      Log.e("Setup failed:", e.toString());
    }
  }

  private void startSequence() {
    av = (AnimationView)findViewById(R.id.animationview);
    Element f = Tamination.getFormation(this,"Static Square");
    av.setFormation(f);
  }

  @Override
  public void onPause()
  {
    super.onPause();
    sr.stop();
    sr.shutdown();
    sr = null;
  }

  @Override
  public void onBeginningOfSpeech() {
  }

  @Override
  public void onPartialResult(Hypothesis hypothesis) {
  }

  /**
   * We stop recognizer here to get a final result
   */
  @Override
  public void onEndOfSpeech() {
    sr.stop();
  }

  @Override
  public void onResult(Hypothesis hypothesis) {
    if (hypothesis != null) {
      ca.add(hypothesis.getHypstr());
      lv.smoothScrollToPosition(ca.getCount() - 1);
      if (sr != null)
        sr.startListening(CALL_SEARCH);
    }
  }

  @Override
  public void onError(Exception error) {
    //  ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
  }

  @Override
  public void onTimeout() { }

  private class CallsAdapter extends ArrayAdapter<String>
  {
    public CallsAdapter(Context context) {
      super(context, R.layout.item_sequence);
    }
  }
}
