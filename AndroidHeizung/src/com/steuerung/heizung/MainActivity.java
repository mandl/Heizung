/*
    Heizung
    
    Copyright (C) 2016 Mandl

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


package com.steuerung.heizung;



import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	public static final String PREFS_NAME = "HeizungPrefsFile";

	private TextView battView;
	private EditText btneditRunTime;
	private EditText btneditVerbrauch;
	private ToggleButton mbtnEinAus;
	private ToggleButton mDayNight;
	
	private  CheckBox btnStoerung;
		

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			boolean stoerung = false;
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String string = bundle.getString(SteuerungService.STAT_BAT);
				String string2 = bundle
						.getString(SteuerungService.STAT_RUNTIME);
				String string3 = bundle
						.getString(SteuerungService.STAT_VERBRAUCH);
				String strStarts = bundle
						.getString(SteuerungService.STAT_STARTS);
				String strDates = bundle
						.getString(SteuerungService.STAT_DATE);
				
				stoerung = bundle.getBoolean(SteuerungService.STAT_STOERUNG);
				
				battView.setText(string + " Volt");
				btneditDate.setText(strDates);
				btneditRunTime.setText(string2 + " h   "+ strStarts+ " Starts");
				btneditVerbrauch.setText(string3 + " Liter");
				
				btnStoerung.setChecked(stoerung);
							
				
			}
		}
	};

	private EditText btneditDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Restore preferences
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		btneditRunTime = (EditText) findViewById(R.id.editRunTime);
		btneditVerbrauch = (EditText) findViewById(R.id.editVerbrauch);
		btneditDate = (EditText) findViewById(R.id.EditTextDatum);
		
		mbtnEinAus = (ToggleButton) findViewById(R.id.toggleButtonOnOff);
		mDayNight = (ToggleButton) findViewById(R.id.tagNachtOnOff);
		
		battView = (TextView) findViewById(R.id.textBatt);

		mbtnEinAus.setChecked(sharedPrefs.getBoolean("pref_HeizungEin", false));
		mDayNight.setChecked(sharedPrefs.getBoolean("pref_DayNight", false));
		
		
		btnStoerung= (CheckBox)findViewById(R.id.checkBoxStoerung);
		
		doBindService();

		if (sharedPrefs.getBoolean("pref_HeizungEin", false)) {
			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_HEIZUNG_AN);
			startService(i);
		}
		if (sharedPrefs.getBoolean("pref_autoFreitag", false)) {
			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_FREITAG_AN);
			startService(i);
		}
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void OnTime(View v) {

		Intent i = new Intent(getBaseContext(), SteuerungService.class);
		i.setAction(SteuerungService.ACTION_LAUNCH_ALARM);
		startService(i);

	}

	public void ToggleClickedOnOff(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {

			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_HEIZUNG_AN);
			startService(i);

		} else {
			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_HEIZUNG_AUS);
			startService(i);

		}
	}
	public void DayNightOnOff(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {

			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_DAY_NIGHT_ON);
			startService(i);

		} else {
			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_DAY_NIGHT_OFF);
			startService(i);
		}
	}		

	/*public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {

			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_FREITAG_AN);
			startService(i);

		} else {
			Intent i = new Intent(getBaseContext(), SteuerungService.class);
			i.setAction(SteuerungService.ACTION_FREITAG_AUS);
			startService(i);

		}
	}*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_settings:
			Intent i = new Intent(this, UserSettingActivity.class);
			startActivity(i);
			break;
		}
		return true;
	}

	@Override
	protected void onStart() {
		Log.i("MANDL", "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(receiver,
				new IntentFilter(SteuerungService.INFORM_APP));
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("MANDL", "onStop");
		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = sharedPrefs.edit();
		// editor.putBoolean("pref_autoFreitag", btnFreitag.isChecked());
		editor.putBoolean("pref_HeizungEin", mbtnEinAus.isChecked());
		editor.putBoolean("pref_DayNight", mDayNight.isChecked());

		// Commit the edits!
		editor.commit();
	}
	
	

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent i = new Intent(getBaseContext(), SteuerungService.class);
		i.setAction(SteuerungService.STAT_SHUTDOWN);
		startService(i);
		doUnbindService();
		Log.i("MANDL", "onDestroy");
	}

	private boolean mIsBound = false;

	private SteuerungService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((SteuerungService.LocalBinder) service)
					.getService();

			// Tell the user about this for our demo.
			Toast.makeText(MainActivity.this, R.string.servicestart,
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
			Toast.makeText(MainActivity.this, R.string.servicestop,
					Toast.LENGTH_SHORT).show();
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(MainActivity.this, SteuerungService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {

		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

}
