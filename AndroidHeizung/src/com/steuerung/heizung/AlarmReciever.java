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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class AlarmReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent arg1) {
		//PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
		//PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AppAlarmReceiver");
		//wl.acquire(120000);
		Intent i = new Intent(ctx, SteuerungService.class);
		i.setAction(SteuerungService.ACTION_LAUNCH_ALARM);
		ctx.startService(i);
	}

}
