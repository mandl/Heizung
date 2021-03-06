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

import android.util.Log;

import com.steuerung.heizung.state.heizungimpl.IHeizungStatemachine.SCIHeizungListener;

public class HeizungListener implements SCIHeizungListener {

	@Override
	public void onOnRaised() {
		// TODO Auto-generated method stub
		Log.i("MANDL","OnRaised");

	}

	@Override
	public void onOffRaised() {
		// TODO Auto-generated method stub
		Log.i("MANDL","OffRaised");

	}

	@Override
	public void onGetstatusRaised() {
		// TODO Auto-generated method stub
		Log.i("MANDL","GetstatusRaised");
	}
	

}
