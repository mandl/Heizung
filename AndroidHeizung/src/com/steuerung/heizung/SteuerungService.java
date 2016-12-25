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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.steuerung.heizung.state.TimerService;
import com.steuerung.heizung.state.heizungimpl.HeizungStatemachine;
import com.steuerung.heizung.state.heizungimpl.IHeizungStatemachine.SCIHeizungListener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SteuerungService extends Service implements SCIHeizungListener {

	private boolean SimualtionMode = false;

	private static Timer timer = new Timer();

	private PowerManager.WakeLock mwl;

	private String Datestr = "yyyy-MM-dd";
	public SimpleDateFormat sdf2 = new SimpleDateFormat(Datestr, Locale.US);

	private int Stoerung = 0;
	private boolean PowerFailsenden;
	public long mRunTime;
	public String mRunTimeDate;

	public long mBrennerStarts;

	private int Heizunglaeuft = 0;
	private Date startTime;
	private Date stopTime;
	private boolean dayNightActiv;

	private double Spannung_Akku = 0;
	private boolean mUndervoltageSend = false;
	private double temp_pt1000 = 0;
	private OnSharedPreferenceChangeListener listener;

	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss ", Locale.US);

	public static final String ACTION_LAUNCH_ALARM = "launch_alarm";
	public static final String ACTION_HEIZUNG_AN = "heizung_an";
	public static final String ACTION_HEIZUNG_AUS = "heizung_aus";
	public static final String ACTION_FREITAG_AN = "freitag_an";
	public static final String ACTION_FREITAG_AUS = "freitag_aus";
	public static final String ACTION_DAY_NIGHT_ON = "TagNacht_an";
	public static final String ACTION_DAY_NIGHT_OFF = "TagNacht_aus";
	public static final String ACTION_SMS_AN = "sms_an";
	public static final String ACTION_SMS_AUS = "sms_aus";
	public static final String ACTION_SMS_STATUS = "sms_status";
	public static final String ON_AC = "on_ac";
	public static final String ON_AC_FAIL = "on_ac_fail";
	public static final String STAT_BAT = "stat_bat";
	public static final String STAT_STOERUNG = "stat_stoerung";
	public static final String STAT_PT_TEMP = "stat_pt_temp";
	public static final String STAT_RUNTIME = "stat_runtime";
	public static final String STAT_VERBRAUCH = "stat_verbrauch";
	public static final String STAT_STARTS = "stat_starts";
	public static final String STAT_DATE = "stat_date";
	public static final String STAT_SHUTDOWN = "shutdown";

	private final String STATUS = "status\n";
	private final String AN = "an\n";
	private final String AUS = "aus\n";

	private final static String TAG_STEUERUNG = "GPSSteuerungService";
	public static final String INFORM_APP = "com.steuerung.heizung";

	protected OutputStream mOutputStream;

	private BufferedReader mreader;

	private String mPhoneNumber = "";

	private TimerService myTimerService = new TimerService();
	private HeizungStatemachine myStateMaschine = new HeizungStatemachine();
	// private HeizungListener myHeizungListener = new HeizungListener();

	private AlarmManager mAlarmManager;

	private SmsReceiver mSMSreceiver;

	private IntentFilter mIntentFilter;

	private SerialPort mSerialPort;

	private String mDayNightOn;

	private String mDayNightOff;

	private boolean mUndervoltage;

	private Calendar mcal;

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		SteuerungService getService() {
			return SteuerungService.this;
		}
	}

	@Override
	public void onCreate() {

		if (SimualtionMode == false) {

			try {
				mSerialPort = new SerialPort(new File("/dev/ttyMT1"), 9600, 0);

				mreader = new BufferedReader(new InputStreamReader(mSerialPort.getInputStream(), "US-ASCII"));

				mOutputStream = mSerialPort.getOutputStream();

			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		
		// SMS event receiver
		mSMSreceiver = new SmsReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		mIntentFilter.setPriority(2147483646);
		registerReceiver(mSMSreceiver, mIntentFilter);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mwl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Heizung");
		mwl.acquire();

		Log.i(TAG_STEUERUNG, "SteuerungService-onCreate");

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		myStateMaschine.setTimerService(myTimerService);

		List<SCIHeizungListener> myList = myStateMaschine.getSCIHeizung().getListeners();

		myList.add(this);

		myStateMaschine.init();
		myStateMaschine.enter();

		timer.scheduleAtFixedRate(new mainTask(), 0, 1000);

		FreitagAnNext();

		logSDCard("Start: ");

		// Restore preferences
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// StoerungSMSsenden = sharedPrefs.getBoolean("pref_PowerLost", false);
		PowerFailsenden = sharedPrefs.getBoolean("pref_PowerLost", false);

		mRunTime = sharedPrefs.getLong("pref_RunHour", 0);
		mBrennerStarts = sharedPrefs.getLong("pref_BrennerStarts", 0);
		mRunTimeDate = sharedPrefs.getString("pref_RunTimeDate", sdf2.format(new Date()));

		mPhoneNumber = sharedPrefs.getString("phoneNumber", "");

		mDayNightOn = sharedPrefs.getString("timeOn_Key", "");
		mDayNightOff = sharedPrefs.getString("timeOff_Key", "");
		mUndervoltage = sharedPrefs.getBoolean("pref_Undervoltage", false);

		// Instance field for listener
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				// Your Implementation
				if (key.equals("pref_PowerLost")) {
					PowerFailsenden = prefs.getBoolean("pref_PowerLost", false);
				} else if (key.equals("pref_autoFreitag")) {
					if (prefs.getBoolean("pref_autoFreitag", false)) {
						myStateMaschine.setAuto(true);
					} else {
						myStateMaschine.setAuto(false);
					}
				} else if (key.equals("mPhoneNumber")) {
					mPhoneNumber = prefs.getString("phoneNumber", "");

				} else if (key.equals("timeOn_Key")) {
					mDayNightOn = prefs.getString("timeOn_Key", "");

				} else if (key.equals("timeOff_Key")) {
					mDayNightOff = prefs.getString("timeOff_Key", "");

				} else if (key.equals("reset")) {

					mRunTime = 0;
					mRunTimeDate = sdf2.format(new Date());
					mBrennerStarts = 0;
				}

			}
		};

		sharedPrefs.registerOnSharedPreferenceChangeListener(listener);
	}

	private class mainTask extends TimerTask {
		public void run() {

			myStateMaschine.runCycle();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Log.i(TAG_STEUERUNG, "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		if (intent != null) {
			doAction(intent);
		}
		// stopped, so return sticky.
		return START_STICKY;
	}

	private void HeizungAn() {
		String ret;
		logSDCard("Ein: ");

		if (SimualtionMode == false) {
			try {
				mOutputStream.write(AN.getBytes());
				ret = mreader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void HeizungAus() {
		String ret;
		logSDCard("Aus: ");
		if (SimualtionMode == false) {
			try {
				mOutputStream.write(AUS.getBytes());
				ret = mreader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void logSDRunTime(String log) {

		OutputStream fos;
		File myFile = new File(getExternalFilesDir(null), "Runtime.txt");
		try {

			fos = new FileOutputStream(myFile, true);
			fos.write(log.getBytes());
			fos.write('\n');
			fos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.w("ExternalStorage", "Error writing " + myFile, e);
			e.printStackTrace();
		}
	}

	private void logSDCard(String log) {

		Date myDate = new Date();
		OutputStream fos;
		File myFile = new File(getExternalFilesDir(null), "Log.txt");
		try {

			fos = new FileOutputStream(myFile, true);
			fos.write(sdf.format(myDate).getBytes()); // Timestamp
			fos.write(log.getBytes());
			fos.write('\n');
			fos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.w("ExternalStorage", "Error writing " + myFile, e);
			e.printStackTrace();
		}
	}

	private void doAction(Intent intent) {
		String action = intent.getAction();
		if (action.equals(ACTION_LAUNCH_ALARM)) {

			myStateMaschine.raiseTime();
			FreitagAnNext();

		} else if (action.equals(ACTION_HEIZUNG_AN)) {
			myStateMaschine.raiseOn();

		} else if (action.equals(ACTION_HEIZUNG_AUS)) {
			myStateMaschine.raiseOff();
		} else if (action.equals(ACTION_DAY_NIGHT_ON)) {
			dayNightActiv = true;

		} else if (action.equals(ACTION_DAY_NIGHT_OFF)) {
			dayNightActiv = false;

		} else if (action.equals(ACTION_FREITAG_AN)) {
			myStateMaschine.setAuto(true);

		} else if (action.equals(ACTION_FREITAG_AUS)) {

			myStateMaschine.setAuto(false);
		} else if (action.equals(ACTION_SMS_AN)) {
			logSDCard("SMS an");
			myStateMaschine.raiseOn();

		} else if (action.equals(ACTION_SMS_AUS)) {
			logSDCard("SMS aus ");
			myStateMaschine.raiseOff();

		} else if (action.equals(ACTION_SMS_STATUS)) {
			SMSStatus();

		} else if (action.equals(ON_AC)) {

			logSDCard("OnPower: ");
			Toast.makeText(this, R.string.onPower, Toast.LENGTH_LONG).show();
			if (PowerFailsenden == true) {
				SendSMS("Heizung: Strom wieder da");
			}
		} else if (action.equals(ON_AC_FAIL)) {
			logSDCard("PowerFail: ");
			if (PowerFailsenden == true) {
				SendSMS("Stromausfall Heizung");
			}
			Toast.makeText(this, R.string.failPower, Toast.LENGTH_LONG).show();
		} else if (action.equals(STAT_SHUTDOWN)) {
			savePref();
		}

	}

	private void SMSStatus() {

		double myRun = (double) mRunTime / 3600.0;
		String smsstr = mRunTimeDate + " Verbrauch: " + String.format("%.2f", myRun * 2.26) + " l   Dauer: "
				+ String.format("%.2f", myRun) + " h   Starts: " + mBrennerStarts;
		SendSMS(smsstr);

	}

	private void FreitagAnNext() {

		Calendar todayCal = Calendar.getInstance();
		todayCal.setTimeInMillis(System.currentTimeMillis());

		mFreitagan = Calendar.getInstance();
		mFreitagan.setTimeInMillis(todayCal.getTimeInMillis());
		mFreitagan.set(Calendar.HOUR_OF_DAY, 8);
		mFreitagan.set(Calendar.MINUTE, 0);
		mFreitagan.set(Calendar.SECOND, 0);
		mFreitagan.set(Calendar.MILLISECOND, 0);
		mFreitagan.add(Calendar.DAY_OF_YEAR, 1);

		while (mFreitagan.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
			mFreitagan.add(Calendar.DAY_OF_YEAR, 1);
		}

		Log.i(TAG_STEUERUNG, "Event: " + mFreitagan.getTime().toString());
		logSDCard("Event: " + mFreitagan.getTime().toString() + "  ");

		PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent(this, AlarmReciever.class), 0);

		mAlarmManager.cancel(sender);
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, mFreitagan.getTimeInMillis(), sender);

	}

	@Override
	public void onDestroy() {

		Log.i(TAG_STEUERUNG, "SteuerungService-onDestroy");

		savePref();

		// Unregister the SMS receiver
		unregisterReceiver(mSMSreceiver);

		mSerialPort.close();

		mwl.release();
		timer.cancel();

		// Tell the user we stopped.
		Toast.makeText(this, R.string.servicestop, Toast.LENGTH_SHORT).show();
	}

	private void savePref() {
		// Restore preferences

		Log.i("MANDL", "savePref");

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		SharedPreferences.Editor editor = sharedPrefs.edit();

		editor.putLong("pref_RunHour", mRunTime);
		editor.putLong("pref_BrennerStarts", mBrennerStarts);
		editor.putString("pref_RunTimeDate", mRunTimeDate);

		// Commit the edits!
		editor.commit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

	private Calendar mFreitagan;

	private boolean m_dayNightOn = false;

	@Override
	public void onOnRaised() {

		Log.i("MANDL", "OnRaised");
		HeizungAn();

	}

	@Override
	public void onOffRaised() {

		Log.i("MANDL", "OffRaised");
		HeizungAus();

	}

	@Override
	public void onGetstatusRaised() {

		String status = "";
		int myStoerung;
		int myHeizunglaeuft;
		int batt;
		int pt_temp;
		Log.i("MANDL", "onGetstatusRaised");

		if (SimualtionMode == false) {
			try {
				mOutputStream.write(STATUS.getBytes());

				status = mreader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			status = "status,0,1,0,512,512";
		}
		String delims = "[,]";

		//          Reset, Stoerung, Brenner an, Voltage, Temp
		// status,   0   ,      1  ,          0,     512,  512

		String[] tokens = status.split(delims);
		if (tokens.length == 6) {
			if (tokens[0].equals("status")) {
				try {
					myStoerung = Integer.parseInt(tokens[2]);
					if ((myStoerung == 1) && (Stoerung == 0)) {
						// Brenner ausgefallen
						Stoerung = 1;
						logSDCard("Brenner ausgefallen");
						SendSMS("Heizung: Brenner ausgefallen");

					} else if ((myStoerung == 0) && (Stoerung == 1)) {
						// Brenner laeuft wieder
						Stoerung = 0;
						logSDCard("Brenner laeuft wieder");
					}
					myHeizunglaeuft = Integer.parseInt(tokens[3]);
					if ((myHeizunglaeuft == 1) && (Heizunglaeuft == 0)) {
						// Heizung laeuft
						Heizunglaeuft = 1;
						Log.i("MANDL", "Heizung an:");
						logSDCard("Heizung an");
						mBrennerStarts++;
						startTime = new Date();
					} else if ((myHeizunglaeuft == 0) && (Heizunglaeuft == 1)) {
						// Heizung stop
						Heizunglaeuft = 0;
						stopTime = new Date();

						long timediff = (stopTime.getTime() - startTime.getTime()) / 1000;
						Log.i("MANDL", "Heizung dauer: " + timediff);
						logSDCard("Heizung aus " + timediff);

						logSDRunTime(sdf.format(stopTime) + timediff);

						mRunTime += timediff;

					}

					batt = Integer.parseInt(tokens[4]);
					pt_temp = Integer.parseInt(tokens[5]);
					Spannung_Akku = (batt * (5.0 / 1023.0) * 4.93) + 0.6;
					publishUpdate();
					CheckAkkuVoltage();
					NachtTagCheck();
					Log.i("MANDL", "Batt:" + Spannung_Akku);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}

	private void NachtTagCheck() {
		if (dayNightActiv == true) {
			
			mcal = Calendar.getInstance();
			int min = mcal.get(Calendar.MINUTE);
			int hour = mcal.get(Calendar.HOUR_OF_DAY);
			String[] parts = mDayNightOn.split(":");
			String[] parts2 = mDayNightOff.split(":");

			if ((hour == Integer.parseInt(parts[0])) && (min == Integer.parseInt(parts[1])) && (m_dayNightOn == false)){
				 m_dayNightOn = true;
				 HeizungAus();
				 
			}
			if ((hour == Integer.parseInt(parts2[0])) && (min == Integer.parseInt(parts2[1])) &&  (m_dayNightOn == true)) {
				 m_dayNightOn = false;
				 HeizungAn();
			}

		}
	}

	private void CheckAkkuVoltage() {
		if (mUndervoltage == true) {
			if (Spannung_Akku < 11.0 && mUndervoltageSend == false) {
				// storage battery fail
				SendSMS("Akkuspannung ist  " + Spannung_Akku);
				mUndervoltageSend = true;
			}
			if (Spannung_Akku > 12.8) {
				// storage battery is ok
				mUndervoltageSend = true;
			}

		}
	}

	private void SendSMS(String text) {
		SmsManager manager = SmsManager.getDefault();
		String Number;
		if (SimualtionMode == true) {
			Number = "5556";
		} else {
			Number = mPhoneNumber;

		}
		manager.sendTextMessage(Number, null, text, null, null);
	}

	private void publishUpdate() {

		double myRun;
		Intent intent = new Intent(INFORM_APP);
		intent.putExtra(STAT_BAT, String.format("%.2f", Spannung_Akku));
		intent.putExtra(STAT_STOERUNG, Stoerung);
		// intent.putExtra(STAT_PT_TEMP,pt_temp);
		myRun = (double) mRunTime / 3600.0;
		intent.putExtra(STAT_RUNTIME, String.format("%.2f", myRun));
		// 2.26 l/h
		intent.putExtra(STAT_VERBRAUCH, String.format("%.2f", myRun * 2.26));
		intent.putExtra(STAT_STARTS, String.format("%d", mBrennerStarts));
		intent.putExtra(STAT_DATE, mRunTimeDate);

		sendBroadcast(intent);
	}

}
