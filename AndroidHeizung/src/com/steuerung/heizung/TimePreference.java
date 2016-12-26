/*


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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;



public class TimePreference extends DialogPreference {
    private int mHour = 0;
    private int mMinute = 0;
    private TimePicker picker = null;
  
	private final String DEFAULT_VALUE = "00:00";
	


    public static int getHour(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[0]);
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        return Integer.parseInt(pieces[1]);
    }

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }
    
    public void setTime(int hour, int minute) {
    	mHour = hour;
    	mMinute = minute;
    	String time = toTime(mHour, mMinute);
    	persistString(time);
        notifyDependencyChange(shouldDisableDependents());
        notifyChanged();
    }
    
    public String toTime(int hour, int minute) {
    	return String.valueOf(hour) + ":" + String.valueOf(minute);
    }
    
    public void updateSummary() {
    	String time = String.valueOf(mHour) + ":" + String.valueOf(mMinute);
    	setSummary(time24to12(time));
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true); 
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(mHour);
        picker.setCurrentMinute(mMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int currHour = picker.getCurrentHour();
            int currMinute = picker.getCurrentMinute();
        	
        	if (!callChangeListener(toTime(currHour, currMinute))) {
        		return;
        	}

        	// persist
        	setTime(currHour, currMinute);
        	updateSummary();
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        String time = null;

        if (restorePersistedValue) {
            if (defaultValue == null) {
                time = getPersistedString(DEFAULT_VALUE);
            }
            else {
                time = getPersistedString(DEFAULT_VALUE);
            }
        }
        else {
            time = defaultValue.toString();
        }

        int currHour = getHour(time);
        int currMinute = getMinute(time);
        // need to persist here for default value to work
        setTime(currHour, currMinute);
        updateSummary();
    }
    
    public static Date toDate(String inTime) {
    	try {
	    	DateFormat inTimeFormat = new SimpleDateFormat("HH:mm", Locale.US);
	    	return inTimeFormat.parse(inTime);
    	} catch(ParseException e) {
    		return null;
    	}
    }
    
    public static String time24to12(String inTime) {
    	Date inDate = toDate(inTime);
    	if(inDate != null) {
			DateFormat outTimeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
			return outTimeFormat.format(inDate);
    	} else {
    		return inTime;
    	}
    }
}