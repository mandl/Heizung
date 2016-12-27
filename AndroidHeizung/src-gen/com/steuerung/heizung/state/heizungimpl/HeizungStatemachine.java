package com.steuerung.heizung.state.heizungimpl;
import java.util.LinkedList;
import java.util.List;
import com.steuerung.heizung.state.ITimer;

public class HeizungStatemachine implements IHeizungStatemachine {

	protected class SCIHeizungImpl implements SCIHeizung {
	
		private List<SCIHeizungListener> listeners = new LinkedList<SCIHeizungListener>();
		
		public List<SCIHeizungListener> getListeners() {
			return listeners;
		}
		private boolean on;
		
		public boolean isRaisedOn() {
			return on;
		}
		
		protected void raiseOn() {
			on = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onOnRaised();
			}
		}
		
		private boolean off;
		
		public boolean isRaisedOff() {
			return off;
		}
		
		protected void raiseOff() {
			off = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onOffRaised();
			}
		}
		
		private boolean getstatus;
		
		public boolean isRaisedGetstatus() {
			return getstatus;
		}
		
		protected void raiseGetstatus() {
			getstatus = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onGetstatusRaised();
			}
		}
		
		protected void clearEvents() {
		}
		protected void clearOutEvents() {
		
		on = false;
		off = false;
		getstatus = false;
		}
		
	}
	
	protected SCIHeizungImpl sCIHeizung;
	
	protected class SCInterfaceImpl implements SCInterface {
	
		private boolean on;
		
		public void raiseOn() {
			on = true;
		}
		
		private boolean off;
		
		public void raiseOff() {
			off = true;
		}
		
		private boolean time;
		
		public void raiseTime() {
			time = true;
		}
		
		private boolean auto;
		
		public boolean getAuto() {
			return auto;
		}
		
		public void setAuto(boolean value) {
			this.auto = value;
		}
		
		protected void clearEvents() {
			on = false;
			off = false;
			time = false;
		}
	}
	
	protected SCInterfaceImpl sCInterface;
	
	private boolean initialized = false;
	
	public enum State {
		main_region_Go,
		main_region_Go_r1_AutoOff,
		main_region_Go_r1_AutoOn,
		main_region_Go_r2_off,
		main_region_Go_r2_on,
		main_region_Go_rc_status,
		main_region_Go_rc_status2,
		$NullState$
	};
	
	private final State[] stateVector = new State[3];
	
	private int nextStateIndex;
	
	private ITimer timer;
	
	private final boolean[] timeEvents = new boolean[3];
	public HeizungStatemachine() {
		sCIHeizung = new SCIHeizungImpl();
		sCInterface = new SCInterfaceImpl();
	}
	
	public void init() {
		this.initialized = true;
		if (timer == null) {
			throw new IllegalStateException("timer not set.");
		}
		for (int i = 0; i < 3; i++) {
			stateVector[i] = State.$NullState$;
		}
		clearEvents();
		clearOutEvents();
		sCInterface.setAuto(false);
	}
	
	public void enter() {
		if (!initialized) {
			throw new IllegalStateException(
					"The state machine needs to be initialized first by calling the init() function.");
		}
		if (timer == null) {
			throw new IllegalStateException("timer not set.");
		}
		enterSequence_main_region_default();
	}
	
	public void exit() {
		exitSequence_main_region();
	}
	
	/**
	 * @see IStatemachine#isActive()
	 */
	public boolean isActive() {
		return stateVector[0] != State.$NullState$||stateVector[1] != State.$NullState$||stateVector[2] != State.$NullState$;
	}
	
	/** 
	* Always returns 'false' since this state machine can never become final.
	*
	* @see IStatemachine#isFinal()
	*/
	public boolean isFinal() {
		return false;
	}
	/**
	* This method resets the incoming events (time events included).
	*/
	protected void clearEvents() {
		sCIHeizung.clearEvents();
		sCInterface.clearEvents();
		for (int i=0; i<timeEvents.length; i++) {
			timeEvents[i] = false;
		}
	}
	
	/**
	* This method resets the outgoing events.
	*/
	protected void clearOutEvents() {
		sCIHeizung.clearOutEvents();
	}
	
	/**
	* Returns true if the given state is currently active otherwise false.
	*/
	public boolean isStateActive(State state) {
	
		switch (state) {
		case main_region_Go:
			return stateVector[0].ordinal() >= State.
					main_region_Go.ordinal()&& stateVector[0].ordinal() <= State.main_region_Go_rc_status2.ordinal();
		case main_region_Go_r1_AutoOff:
			return stateVector[0] == State.main_region_Go_r1_AutoOff;
		case main_region_Go_r1_AutoOn:
			return stateVector[0] == State.main_region_Go_r1_AutoOn;
		case main_region_Go_r2_off:
			return stateVector[1] == State.main_region_Go_r2_off;
		case main_region_Go_r2_on:
			return stateVector[1] == State.main_region_Go_r2_on;
		case main_region_Go_rc_status:
			return stateVector[2] == State.main_region_Go_rc_status;
		case main_region_Go_rc_status2:
			return stateVector[2] == State.main_region_Go_rc_status2;
		default:
			return false;
		}
	}
	
	/**
	* Set the {@link ITimer} for the state machine. It must be set
	* externally on a timed state machine before a run cycle can be correct
	* executed.
	* 
	* @param timer
	*/
	public void setTimer(ITimer timer) {
		this.timer = timer;
	}
	
	/**
	* Returns the currently used timer.
	* 
	* @return {@link ITimer}
	*/
	public ITimer getTimer() {
		return timer;
	}
	
	public void timeElapsed(int eventID) {
		timeEvents[eventID] = true;
	}
	
	public SCIHeizung getSCIHeizung() {
		return sCIHeizung;
	}
	
	public SCInterface getSCInterface() {
		return sCInterface;
	}
	
	public void raiseOn() {
		sCInterface.raiseOn();
	}
	
	public void raiseOff() {
		sCInterface.raiseOff();
	}
	
	public void raiseTime() {
		sCInterface.raiseTime();
	}
	
	public boolean getAuto() {
		return sCInterface.getAuto();
	}
	
	public void setAuto(boolean value) {
		sCInterface.setAuto(value);
	}
	
	private boolean check_main_region_Go_r1_AutoOff_tr0_tr0() {
		return (sCInterface.time) && (sCInterface.getAuto()==true);
	}
	
	private boolean check_main_region_Go_r1_AutoOn_tr0_tr0() {
		return timeEvents[0];
	}
	
	private boolean check_main_region_Go_r1_AutoOn_tr1_tr1() {
		return sCInterface.off;
	}
	
	private boolean check_main_region_Go_r2_off_tr0_tr0() {
		return sCInterface.on;
	}
	
	private boolean check_main_region_Go_r2_on_tr0_tr0() {
		return sCInterface.off;
	}
	
	private boolean check_main_region_Go_rc_status_tr0_tr0() {
		return timeEvents[1];
	}
	
	private boolean check_main_region_Go_rc_status2_tr0_tr0() {
		return timeEvents[2];
	}
	
	private void effect_main_region_Go_r1_AutoOff_tr0() {
		exitSequence_main_region_Go_r1_AutoOff();
		enterSequence_main_region_Go_r1_AutoOn_default();
	}
	
	private void effect_main_region_Go_r1_AutoOn_tr0() {
		exitSequence_main_region_Go_r1_AutoOn();
		enterSequence_main_region_Go_r1_AutoOff_default();
	}
	
	private void effect_main_region_Go_r1_AutoOn_tr1() {
		exitSequence_main_region_Go_r1_AutoOn();
		enterSequence_main_region_Go_r1_AutoOff_default();
	}
	
	private void effect_main_region_Go_r2_off_tr0() {
		exitSequence_main_region_Go_r2_off();
		enterSequence_main_region_Go_r2_on_default();
	}
	
	private void effect_main_region_Go_r2_on_tr0() {
		exitSequence_main_region_Go_r2_on();
		enterSequence_main_region_Go_r2_off_default();
	}
	
	private void effect_main_region_Go_rc_status_tr0() {
		exitSequence_main_region_Go_rc_status();
		enterSequence_main_region_Go_rc_status2_default();
	}
	
	private void effect_main_region_Go_rc_status2_tr0() {
		exitSequence_main_region_Go_rc_status2();
		enterSequence_main_region_Go_rc_status_default();
	}
	
	/* Entry action for state 'AutoOff'. */
	private void entryAction_main_region_Go_r1_AutoOff() {
		sCIHeizung.raiseOff();
	}
	
	/* Entry action for state 'AutoOn'. */
	private void entryAction_main_region_Go_r1_AutoOn() {
		timer.setTimer(this, 0, 50400 * 1000, false);
		
		sCIHeizung.raiseOn();
	}
	
	/* Entry action for state 'off'. */
	private void entryAction_main_region_Go_r2_off() {
		sCIHeizung.raiseOff();
	}
	
	/* Entry action for state 'on'. */
	private void entryAction_main_region_Go_r2_on() {
		sCIHeizung.raiseOn();
	}
	
	/* Entry action for state 'status'. */
	private void entryAction_main_region_Go_rc_status() {
		timer.setTimer(this, 1, 3 * 1000, false);
		
		sCIHeizung.raiseGetstatus();
	}
	
	/* Entry action for state 'status2'. */
	private void entryAction_main_region_Go_rc_status2() {
		timer.setTimer(this, 2, 3 * 1000, false);
		
		sCIHeizung.raiseGetstatus();
	}
	
	/* Exit action for state 'AutoOn'. */
	private void exitAction_main_region_Go_r1_AutoOn() {
		timer.unsetTimer(this, 0);
	}
	
	/* Exit action for state 'status'. */
	private void exitAction_main_region_Go_rc_status() {
		timer.unsetTimer(this, 1);
	}
	
	/* Exit action for state 'status2'. */
	private void exitAction_main_region_Go_rc_status2() {
		timer.unsetTimer(this, 2);
	}
	
	/* 'default' enter sequence for state Go */
	private void enterSequence_main_region_Go_default() {
		enterSequence_main_region_Go_r1_default();
		enterSequence_main_region_Go_r2_default();
		enterSequence_main_region_Go_rc_default();
	}
	
	/* 'default' enter sequence for state AutoOff */
	private void enterSequence_main_region_Go_r1_AutoOff_default() {
		entryAction_main_region_Go_r1_AutoOff();
		nextStateIndex = 0;
		stateVector[0] = State.main_region_Go_r1_AutoOff;
	}
	
	/* 'default' enter sequence for state AutoOn */
	private void enterSequence_main_region_Go_r1_AutoOn_default() {
		entryAction_main_region_Go_r1_AutoOn();
		nextStateIndex = 0;
		stateVector[0] = State.main_region_Go_r1_AutoOn;
	}
	
	/* 'default' enter sequence for state off */
	private void enterSequence_main_region_Go_r2_off_default() {
		entryAction_main_region_Go_r2_off();
		nextStateIndex = 1;
		stateVector[1] = State.main_region_Go_r2_off;
	}
	
	/* 'default' enter sequence for state on */
	private void enterSequence_main_region_Go_r2_on_default() {
		entryAction_main_region_Go_r2_on();
		nextStateIndex = 1;
		stateVector[1] = State.main_region_Go_r2_on;
	}
	
	/* 'default' enter sequence for state status */
	private void enterSequence_main_region_Go_rc_status_default() {
		entryAction_main_region_Go_rc_status();
		nextStateIndex = 2;
		stateVector[2] = State.main_region_Go_rc_status;
	}
	
	/* 'default' enter sequence for state status2 */
	private void enterSequence_main_region_Go_rc_status2_default() {
		entryAction_main_region_Go_rc_status2();
		nextStateIndex = 2;
		stateVector[2] = State.main_region_Go_rc_status2;
	}
	
	/* 'default' enter sequence for region main region */
	private void enterSequence_main_region_default() {
		react_main_region__entry_Default();
	}
	
	/* 'default' enter sequence for region r1 */
	private void enterSequence_main_region_Go_r1_default() {
		react_main_region_Go_r1__entry_Default();
	}
	
	/* 'default' enter sequence for region r2 */
	private void enterSequence_main_region_Go_r2_default() {
		react_main_region_Go_r2__entry_Default();
	}
	
	/* 'default' enter sequence for region rc */
	private void enterSequence_main_region_Go_rc_default() {
		react_main_region_Go_rc__entry_Default();
	}
	
	/* Default exit sequence for state AutoOff */
	private void exitSequence_main_region_Go_r1_AutoOff() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
	}
	
	/* Default exit sequence for state AutoOn */
	private void exitSequence_main_region_Go_r1_AutoOn() {
		nextStateIndex = 0;
		stateVector[0] = State.$NullState$;
		
		exitAction_main_region_Go_r1_AutoOn();
	}
	
	/* Default exit sequence for state off */
	private void exitSequence_main_region_Go_r2_off() {
		nextStateIndex = 1;
		stateVector[1] = State.$NullState$;
	}
	
	/* Default exit sequence for state on */
	private void exitSequence_main_region_Go_r2_on() {
		nextStateIndex = 1;
		stateVector[1] = State.$NullState$;
	}
	
	/* Default exit sequence for state status */
	private void exitSequence_main_region_Go_rc_status() {
		nextStateIndex = 2;
		stateVector[2] = State.$NullState$;
		
		exitAction_main_region_Go_rc_status();
	}
	
	/* Default exit sequence for state status2 */
	private void exitSequence_main_region_Go_rc_status2() {
		nextStateIndex = 2;
		stateVector[2] = State.$NullState$;
		
		exitAction_main_region_Go_rc_status2();
	}
	
	/* Default exit sequence for region main region */
	private void exitSequence_main_region() {
		switch (stateVector[0]) {
		case main_region_Go_r1_AutoOff:
			exitSequence_main_region_Go_r1_AutoOff();
			break;
		case main_region_Go_r1_AutoOn:
			exitSequence_main_region_Go_r1_AutoOn();
			break;
		default:
			break;
		}
		
		switch (stateVector[1]) {
		case main_region_Go_r2_off:
			exitSequence_main_region_Go_r2_off();
			break;
		case main_region_Go_r2_on:
			exitSequence_main_region_Go_r2_on();
			break;
		default:
			break;
		}
		
		switch (stateVector[2]) {
		case main_region_Go_rc_status:
			exitSequence_main_region_Go_rc_status();
			break;
		case main_region_Go_rc_status2:
			exitSequence_main_region_Go_rc_status2();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region r1 */
	private void exitSequence_main_region_Go_r1() {
		switch (stateVector[0]) {
		case main_region_Go_r1_AutoOff:
			exitSequence_main_region_Go_r1_AutoOff();
			break;
		case main_region_Go_r1_AutoOn:
			exitSequence_main_region_Go_r1_AutoOn();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region r2 */
	private void exitSequence_main_region_Go_r2() {
		switch (stateVector[1]) {
		case main_region_Go_r2_off:
			exitSequence_main_region_Go_r2_off();
			break;
		case main_region_Go_r2_on:
			exitSequence_main_region_Go_r2_on();
			break;
		default:
			break;
		}
	}
	
	/* Default exit sequence for region rc */
	private void exitSequence_main_region_Go_rc() {
		switch (stateVector[2]) {
		case main_region_Go_rc_status:
			exitSequence_main_region_Go_rc_status();
			break;
		case main_region_Go_rc_status2:
			exitSequence_main_region_Go_rc_status2();
			break;
		default:
			break;
		}
	}
	
	/* The reactions of state AutoOff. */
	private void react_main_region_Go_r1_AutoOff() {
		if (check_main_region_Go_r1_AutoOff_tr0_tr0()) {
			effect_main_region_Go_r1_AutoOff_tr0();
		}
	}
	
	/* The reactions of state AutoOn. */
	private void react_main_region_Go_r1_AutoOn() {
		if (check_main_region_Go_r1_AutoOn_tr0_tr0()) {
			effect_main_region_Go_r1_AutoOn_tr0();
		} else {
			if (check_main_region_Go_r1_AutoOn_tr1_tr1()) {
				effect_main_region_Go_r1_AutoOn_tr1();
			}
		}
	}
	
	/* The reactions of state off. */
	private void react_main_region_Go_r2_off() {
		if (check_main_region_Go_r2_off_tr0_tr0()) {
			effect_main_region_Go_r2_off_tr0();
		}
	}
	
	/* The reactions of state on. */
	private void react_main_region_Go_r2_on() {
		if (check_main_region_Go_r2_on_tr0_tr0()) {
			effect_main_region_Go_r2_on_tr0();
		}
	}
	
	/* The reactions of state status. */
	private void react_main_region_Go_rc_status() {
		if (check_main_region_Go_rc_status_tr0_tr0()) {
			effect_main_region_Go_rc_status_tr0();
		}
	}
	
	/* The reactions of state status2. */
	private void react_main_region_Go_rc_status2() {
		if (check_main_region_Go_rc_status2_tr0_tr0()) {
			effect_main_region_Go_rc_status2_tr0();
		}
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_region__entry_Default() {
		enterSequence_main_region_Go_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_region_Go_r1__entry_Default() {
		enterSequence_main_region_Go_r1_AutoOff_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_region_Go_r2__entry_Default() {
		enterSequence_main_region_Go_r2_off_default();
	}
	
	/* Default react sequence for initial entry  */
	private void react_main_region_Go_rc__entry_Default() {
		enterSequence_main_region_Go_rc_status_default();
	}
	
	public void runCycle() {
		if (!initialized)
			throw new IllegalStateException(
					"The state machine needs to be initialized first by calling the init() function.");
		clearOutEvents();
		for (nextStateIndex = 0; nextStateIndex < stateVector.length; nextStateIndex++) {
			switch (stateVector[nextStateIndex]) {
			case main_region_Go_r1_AutoOff:
				react_main_region_Go_r1_AutoOff();
				break;
			case main_region_Go_r1_AutoOn:
				react_main_region_Go_r1_AutoOn();
				break;
			case main_region_Go_r2_off:
				react_main_region_Go_r2_off();
				break;
			case main_region_Go_r2_on:
				react_main_region_Go_r2_on();
				break;
			case main_region_Go_rc_status:
				react_main_region_Go_rc_status();
				break;
			case main_region_Go_rc_status2:
				react_main_region_Go_rc_status2();
				break;
			default:
				// $NullState$
			}
		}
		clearEvents();
	}
}
