package com.cmpe277.personalassistant;


//<editor-fold defaultstate="collapsed" desc="Imports">
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//</editor-fold>


//Class - Main Activity 
public class MainActivity extends Activity implements OnInitListener {

	private static final int REQUEST_CODE_FOR_VOICE_RECOGNITION = 1001;
	private EditText edtInstructions;
	private ImageButton imgBtnMic;
	private TextView txtCommandResults;
	private TextToSpeech txt2Speech;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edtInstructions = (EditText) findViewById(R.id.etTextHint);
		imgBtnMic = (ImageButton) findViewById(R.id.btSpeak);
		txtCommandResults = (TextView) findViewById(R.id.tvResult);
		txt2Speech = new TextToSpeech(this,this);
		
		if(isVoiceRecoAvailable()){

			imgBtnMic.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {

					if (view == imgBtnMic)
					{
						Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
						intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Talk to me :)");
						startActivityForResult(intent, REQUEST_CODE_FOR_VOICE_RECOGNITION);
					}
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean isVoiceRecoAvailable() {

		PackageManager packageMgr = getPackageManager();
		List<ResolveInfo> speechActivity = packageMgr.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (speechActivity.size() == 0) {
			imgBtnMic.setEnabled(false);
			txtCommandResults.setText("Voice recognizer not present");
			displayToastMessage("Voice recognizer not present");
			return false;
		}
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_FOR_VOICE_RECOGNITION){

			if(resultCode == RESULT_OK) { // result from text to speech is successful

				ArrayList<String> txt2SpeechWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				if (!txt2SpeechWords.isEmpty()) {
					
					txtCommandResults.setText(txt2SpeechWords.get(0).toString());
					

					if (txt2SpeechWords.get(0).contains("search")||txt2SpeechWords.get(0).contains("what is")||txt2SpeechWords.get(0).contains("who is")||txt2SpeechWords.get(0).contains("how do")) {

						String v2txt = txt2SpeechWords.get(0);

						v2txt = v2txt.replace("search","");

						txt2Speech.speak("searching "+ v2txt, TextToSpeech.QUEUE_FLUSH, null);

						Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
						search.putExtra(SearchManager.QUERY, v2txt);
						startActivity(search);
					} else if (txt2SpeechWords.get(0).contains("call")) {

						String v2txt = txt2SpeechWords.get(0);
						v2txt = v2txt.replace("call","");
						
						txt2Speech.speak("Calling "+ v2txt, TextToSpeech.QUEUE_FLUSH, null);

						Intent search = new Intent(Intent.ACTION_DIAL);
						search.putExtra(SearchManager.QUERY, v2txt);
						startActivity(search);
					}else if (txt2SpeechWords.get(0).contains("camera")) {

						String v2txt = txt2SpeechWords.get(0);
						v2txt = v2txt.replace("camera","");

						txt2Speech.speak("Opening Camera", TextToSpeech.QUEUE_FLUSH, null);
						
						Intent camera = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivity(camera);

					}else if (txt2SpeechWords.get(0).contains("event")) {

						String v2txt = txt2SpeechWords.get(0);
						v2txt = v2txt.replace("event","");
						
						txt2Speech.speak("Creating Event "+ v2txt , TextToSpeech.QUEUE_FLUSH, null);
						

						Intent intent = new Intent(Intent.ACTION_INSERT);
						intent.setType("vnd.android.cursor.item/event");
						intent.putExtra(Events.TITLE, v2txt);
						intent.setData(CalendarContract.Events.CONTENT_URI);
						startActivity(intent); 
					}
					else  if(txt2SpeechWords.get(0).contains("message")){

						String v2txt = txt2SpeechWords.get(0);
						v2txt = v2txt.replace("message","");
						
						txt2Speech.speak("composing message" , TextToSpeech.QUEUE_FLUSH, null);
						

						Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
						smsIntent.setData(Uri.parse("sms:"));
						smsIntent.putExtra("sms_body",v2txt);
						startActivity(smsIntent);

					} else if (txt2SpeechWords.get(0).contains("open")){

						String v2txt = txt2SpeechWords.get(0);
						v2txt = v2txt.replace("open","");
						v2txt = v2txt.replace(" ","");
						v2txt = v2txt.replace("","");
						
						List<ApplicationInfo> applicationsInfo;
						PackageManager manager = getPackageManager();
						boolean isMatch = false;
						ApplicationInfo appInfo =null;
						String appName = null;

						try {
							applicationsInfo = manager.getInstalledApplications(0);
							if (applicationsInfo == null)
								throw new PackageManager.NameNotFoundException();

							for(int i=0;i<applicationsInfo.size();i++) {
								appInfo = applicationsInfo.get(i);

								appName = (String) manager.getApplicationLabel(appInfo);

								if(appName.toLowerCase().contains(v2txt.toLowerCase())){
									isMatch =true;
									break;
								}
							}
							if(isMatch){
								txt2Speech.speak("Opening application "+appName , TextToSpeech.QUEUE_FLUSH, null);
								
								Intent i = manager.getLaunchIntentForPackage(appInfo.packageName);
								i.addCategory(Intent.CATEGORY_LAUNCHER);
								startActivity(i);
							}else{
								txt2Speech.speak("I'm sorry, cannot find application "+v2txt , TextToSpeech.QUEUE_FLUSH, null);
								displayToastMessage("App was not found");
							}


						} catch (PackageManager.NameNotFoundException e) {

						}
					}else{
						txt2Speech.speak("I don't understand, can you please try again", TextToSpeech.QUEUE_FLUSH, null);
						displayToastMessage("I don't understand, can you please try again");
					}


				}
				//Result code for various error.
			}else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
				displayToastMessage("Audio Error");
			}else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
				displayToastMessage("Client Error");
			}else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
				displayToastMessage("Network Error");
			}else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
				displayToastMessage("No Match");
			}else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
				displayToastMessage("Server Error");
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	void displayToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}


}


