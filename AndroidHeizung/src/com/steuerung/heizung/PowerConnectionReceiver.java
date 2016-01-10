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
import android.os.BatteryManager;

public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) { 
    	
    	
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        
        
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL;
    
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        
        if ((usbCharge == true)  || (acCharge == true)  )
        {
        	Intent i = new Intent(context, SteuerungService.class);
    		i.setAction(SteuerungService.ON_AC);
    		context.startService(i);
        }
        else
        {
        	Intent i = new Intent(context, SteuerungService.class);
    		i.setAction(SteuerungService.ON_AC_FAIL);
    		context.startService(i);
        }
        	
    }
}
