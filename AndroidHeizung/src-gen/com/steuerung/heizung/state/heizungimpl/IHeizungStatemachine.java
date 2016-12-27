package com.steuerung.heizung.state.heizungimpl;

import java.util.List;
import com.steuerung.heizung.state.IStatemachine;
import com.steuerung.heizung.state.ITimerCallback;

public interface IHeizungStatemachine extends ITimerCallback,IStatemachine {

	public interface SCIHeizung {
	
		public boolean isRaisedOn();
		
		public boolean isRaisedOff();
		
		public boolean isRaisedGetstatus();
		
	public List<SCIHeizungListener> getListeners();
	}
	
	public interface SCIHeizungListener {
	
		public void onOnRaised();
		public void onOffRaised();
		public void onGetstatusRaised();
		}
	
	public SCIHeizung getSCIHeizung();
	
	public interface SCInterface {
	
		public void raiseOn();
		
		public void raiseOff();
		
		public void raiseTime();
		
		public boolean getAuto();
		
		public void setAuto(boolean value);
		
	}
	
	public SCInterface getSCInterface();
	
}
