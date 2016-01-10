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

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ResetHourDialogPreference extends DialogPreference {

	public ResetHourDialogPreference(Context oContext, AttributeSet attrs) {
		super(oContext, attrs);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
						
			 persistBoolean(!getPersistedBoolean(true));
			
		}
	}

}
