package com.steuerung.heizung.state.heizungimpl;
import java.util.LinkedList;
import java.util.List;
import com.steuerung.heizung.state.TimeEvent;
import com.steuerung.heizung.state.ITimerService;

public class HeizungStatemachine implements IHeizungStatemachine {

	private final TimeEvent heizung_main_region_Online_time_event_0 = new TimeEvent(
			true, 0);
	private final TimeEvent heizung_main_region_Online_r1_HeizungAutomatik_time_event_0 = new TimeEvent(
			false, 1);
	private final TimeEvent heizung_main_region_Offline_time_event_0 = new TimeEvent(
			true, 2);

	private final boolean[] timeEvents = new boolean[3];

	private final class SCIHeizungImpl implements SCIHeizung {

		private List<SCIHeizungListener> listeners = new LinkedList<SCIHeizungListener>();

		public List<SCIHeizungListener> getListeners() {
			return listeners;
		}

		private boolean on;

		public boolean isRaisedOn() {
			return on;
		}

		private void raiseOn() {
			on = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onOnRaised();
			}
		}

		private boolean off;

		public boolean isRaisedOff() {
			return off;
		}

		private void raiseOff() {
			off = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onOffRaised();
			}
		}

		private boolean getstatus;

		public boolean isRaisedGetstatus() {
			return getstatus;
		}

		private void raiseGetstatus() {
			getstatus = true;
			for (SCIHeizungListener listener : listeners) {
				listener.onGetstatusRaised();
			}
		}

		public void clearEvents() {
		}

		public void clearOutEvents() {
			on = false;
			off = false;
			getstatus = false;
		}
	}

	private SCIHeizungImpl sCIHeizung;
	private final class SCInterfaceImpl implements SCInterface {

		private boolean on;

		public void raiseOn() {
			on = true;
		}

		private boolean tagnachton;

		public void raiseTagnachton() {
			tagnachton = true;
		}

		private boolean off;

		public void raiseOff() {
			off = true;
		}

		private boolean timeout;

		public void raiseTimeout() {
			timeout = true;
		}

		private boolean connect;

		public void raiseConnect() {
			connect = true;
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

		private boolean tag;

		public boolean getTag() {
			return tag;
		}

		public void setTag(boolean value) {
			this.tag = value;
		}

		public void clearEvents() {
			on = false;
			tagnachton = false;
			off = false;
			timeout = false;
			connect = false;
			time = false;
		}

	}

	private SCInterfaceImpl sCInterface;

	public enum State {
		main_region_Online, main_region_Online_r1_HandSteuerung, main_region_Online_r1_HeizungAutomatik, main_region_Online_r1_HeizungAus, main_region_Offline, $NullState$
	};

	private State[] historyVector = new State[1];
	private final State[] stateVector = new State[1];

	private int nextStateIndex;

	private ITimerService timerService;

	private long cycleStartTime;

	public HeizungStatemachine() {

		sCIHeizung = new SCIHeizungImpl();
		sCInterface = new SCInterfaceImpl();

		heizung_main_region_Online_time_event_0.setStatemachine(this);
		heizung_main_region_Online_r1_HeizungAutomatik_time_event_0
				.setStatemachine(this);
		heizung_main_region_Offline_time_event_0.setStatemachine(this);
	}

	public void init() {
		if (timerService == null) {
			throw new IllegalStateException("TimerService not set.");
		}
		for (int i = 0; i < 1; i++) {
			stateVector[i] = State.$NullState$;
		}

		for (int i = 0; i < 1; i++) {
			historyVector[i] = State.$NullState$;
		}
		clearEvents();
		clearOutEvents();

		sCInterface.auto = false;

		sCInterface.tag = false;
	}

	public void enter() {
		if (timerService == null) {
			throw new IllegalStateException("TimerService not set.");
		}
		cycleStartTime = timerService.getSystemTimeMillis();
		entryAction();

		getTimerService().setTimer(heizung_main_region_Offline_time_event_0,
				1 * 1000, cycleStartTime);

		nextStateIndex = 0;
		stateVector[0] = State.main_region_Offline;
	}

	public void exit() {
		switch (stateVector[0]) {
			case main_region_Online_r1_HandSteuerung :
				historyVector[0] = stateVector[0];

				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService().resetTimer(
						heizung_main_region_Online_time_event_0);
				break;

			case main_region_Online_r1_HeizungAutomatik :
				historyVector[0] = stateVector[0];

				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService()
						.resetTimer(
								heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);

				getTimerService().resetTimer(
						heizung_main_region_Online_time_event_0);
				break;

			case main_region_Online_r1_HeizungAus :
				historyVector[0] = stateVector[0];

				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService().resetTimer(
						heizung_main_region_Online_time_event_0);
				break;

			case main_region_Offline :
				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService().resetTimer(
						heizung_main_region_Offline_time_event_0);
				break;

			default :
				break;
		}

		exitAction();
	}

	protected void clearEvents() {
		sCIHeizung.clearEvents();
		sCInterface.clearEvents();

		for (int i = 0; i < timeEvents.length; i++) {
			timeEvents[i] = false;
		}
	}

	protected void clearOutEvents() {
		sCIHeizung.clearOutEvents();
	}

	public boolean isStateActive(State state) {
		switch (state) {
			case main_region_Online :
				return stateVector[0].ordinal() >= State.main_region_Online
						.ordinal()
						&& stateVector[0].ordinal() <= State.main_region_Online_r1_HeizungAus
								.ordinal();
			case main_region_Online_r1_HandSteuerung :
				return stateVector[0] == State.main_region_Online_r1_HandSteuerung;
			case main_region_Online_r1_HeizungAutomatik :
				return stateVector[0] == State.main_region_Online_r1_HeizungAutomatik;
			case main_region_Online_r1_HeizungAus :
				return stateVector[0] == State.main_region_Online_r1_HeizungAus;
			case main_region_Offline :
				return stateVector[0] == State.main_region_Offline;
			default :
				return false;
		}
	}

	public void setTimerService(ITimerService timerService) {
		this.timerService = timerService;
	}

	public ITimerService getTimerService() {
		return timerService;
	}

	public void onTimeEventRaised(TimeEvent timeEvent) {
		timeEvents[timeEvent.getIndex()] = true;
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
	public void raiseTagnachton() {
		sCInterface.raiseTagnachton();
	}
	public void raiseOff() {
		sCInterface.raiseOff();
	}
	public void raiseTimeout() {
		sCInterface.raiseTimeout();
	}
	public void raiseConnect() {
		sCInterface.raiseConnect();
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
	public boolean getTag() {
		return sCInterface.getTag();
	}

	public void setTag(boolean value) {
		sCInterface.setTag(value);
	}

	/* Entry action for statechart 'Heizung'. */
	private void entryAction() {
	}

	/* Exit action for state 'Heizung'. */
	private void exitAction() {
	}

	/* shallow enterSequence with history in child r1 */
	private void shallowEnterSequenceMain_region_Online_r1() {
		switch (historyVector[0]) {
			case main_region_Online_r1_HandSteuerung :
				sCIHeizung.raiseOn();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HandSteuerung;
				break;

			case main_region_Online_r1_HeizungAutomatik :
				getTimerService()
						.setTimer(
								heizung_main_region_Online_r1_HeizungAutomatik_time_event_0,
								3 * 1000, cycleStartTime);

				sCIHeizung.raiseOn();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAutomatik;
				break;

			case main_region_Online_r1_HeizungAus :
				sCIHeizung.raiseOff();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAus;
				break;

			default :
				break;
		}
	}

	/* The reactions of state HandSteuerung. */
	private void reactMain_region_Online_r1_HandSteuerung() {
		if (sCInterface.timeout) {
			historyVector[0] = stateVector[0];

			switch (stateVector[0]) {
				case main_region_Online_r1_HandSteuerung :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				case main_region_Online_r1_HeizungAutomatik :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;

					getTimerService()
							.resetTimer(
									heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);
					break;

				case main_region_Online_r1_HeizungAus :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				default :
					break;
			}

			getTimerService().resetTimer(
					heizung_main_region_Online_time_event_0);

			getTimerService().setTimer(
					heizung_main_region_Offline_time_event_0, 1 * 1000,
					cycleStartTime);

			nextStateIndex = 0;
			stateVector[0] = State.main_region_Offline;
		} else {
			if (timeEvents[heizung_main_region_Online_time_event_0.getIndex()]) {
				sCIHeizung.raiseGetstatus();
			}

			if (sCInterface.off) {
				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				sCIHeizung.raiseOff();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAus;
			}
		}
	}

	/* The reactions of state HeizungAutomatik. */
	private void reactMain_region_Online_r1_HeizungAutomatik() {
		if (sCInterface.timeout) {
			historyVector[0] = stateVector[0];

			switch (stateVector[0]) {
				case main_region_Online_r1_HandSteuerung :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				case main_region_Online_r1_HeizungAutomatik :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;

					getTimerService()
							.resetTimer(
									heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);
					break;

				case main_region_Online_r1_HeizungAus :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				default :
					break;
			}

			getTimerService().resetTimer(
					heizung_main_region_Online_time_event_0);

			getTimerService().setTimer(
					heizung_main_region_Offline_time_event_0, 1 * 1000,
					cycleStartTime);

			nextStateIndex = 0;
			stateVector[0] = State.main_region_Offline;
		} else {
			if (timeEvents[heizung_main_region_Online_time_event_0.getIndex()]) {
				sCIHeizung.raiseGetstatus();
			}

			if (timeEvents[heizung_main_region_Online_r1_HeizungAutomatik_time_event_0
					.getIndex()]) {
				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService()
						.resetTimer(
								heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);

				sCIHeizung.raiseOff();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAus;
			} else {
				if (sCInterface.off) {
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;

					getTimerService()
							.resetTimer(
									heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);

					sCIHeizung.raiseOff();

					nextStateIndex = 0;
					stateVector[0] = State.main_region_Online_r1_HeizungAus;
				} else {
					if (sCInterface.on) {
						nextStateIndex = 0;
						stateVector[0] = State.$NullState$;

						getTimerService()
								.resetTimer(
										heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);

						sCIHeizung.raiseOn();

						nextStateIndex = 0;
						stateVector[0] = State.main_region_Online_r1_HandSteuerung;
					}
				}
			}
		}
	}

	/* The reactions of state HeizungAus. */
	private void reactMain_region_Online_r1_HeizungAus() {
		if (sCInterface.timeout) {
			historyVector[0] = stateVector[0];

			switch (stateVector[0]) {
				case main_region_Online_r1_HandSteuerung :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				case main_region_Online_r1_HeizungAutomatik :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;

					getTimerService()
							.resetTimer(
									heizung_main_region_Online_r1_HeizungAutomatik_time_event_0);
					break;

				case main_region_Online_r1_HeizungAus :
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;
					break;

				default :
					break;
			}

			getTimerService().resetTimer(
					heizung_main_region_Online_time_event_0);

			getTimerService().setTimer(
					heizung_main_region_Offline_time_event_0, 1 * 1000,
					cycleStartTime);

			nextStateIndex = 0;
			stateVector[0] = State.main_region_Offline;
		} else {
			if (timeEvents[heizung_main_region_Online_time_event_0.getIndex()]) {
				sCIHeizung.raiseGetstatus();
			}

			if (sCInterface.time && sCInterface.auto == true) {
				nextStateIndex = 0;
				stateVector[0] = State.$NullState$;

				getTimerService()
						.setTimer(
								heizung_main_region_Online_r1_HeizungAutomatik_time_event_0,
								3 * 1000, cycleStartTime);

				sCIHeizung.raiseOn();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAutomatik;
			} else {
				if (sCInterface.on) {
					nextStateIndex = 0;
					stateVector[0] = State.$NullState$;

					sCIHeizung.raiseOn();

					nextStateIndex = 0;
					stateVector[0] = State.main_region_Online_r1_HandSteuerung;
				}
			}
		}
	}

	/* The reactions of state Offline. */
	private void reactMain_region_Offline() {
		if (sCInterface.connect) {
			nextStateIndex = 0;
			stateVector[0] = State.$NullState$;

			getTimerService().resetTimer(
					heizung_main_region_Offline_time_event_0);

			getTimerService().setTimer(heizung_main_region_Online_time_event_0,
					5 * 1000, cycleStartTime);

			/* Enter the region with shallow history */
			if (historyVector[0] != State.$NullState$) {
				shallowEnterSequenceMain_region_Online_r1();
			} else {
				sCIHeizung.raiseOff();

				nextStateIndex = 0;
				stateVector[0] = State.main_region_Online_r1_HeizungAus;
			}
		} else {
			if (timeEvents[heizung_main_region_Offline_time_event_0.getIndex()]) {
				sCIHeizung.raiseGetstatus();
			}
		}
	}

	public void runCycle() {

		cycleStartTime = timerService.getSystemTimeMillis();

		clearOutEvents();

		for (nextStateIndex = 0; nextStateIndex < stateVector.length; nextStateIndex++) {

			switch (stateVector[nextStateIndex]) {
				case main_region_Online_r1_HandSteuerung :
					reactMain_region_Online_r1_HandSteuerung();
					break;
				case main_region_Online_r1_HeizungAutomatik :
					reactMain_region_Online_r1_HeizungAutomatik();
					break;
				case main_region_Online_r1_HeizungAus :
					reactMain_region_Online_r1_HeizungAus();
					break;
				case main_region_Offline :
					reactMain_region_Offline();
					break;
				default :
					// $NullState$
			}
		}

		clearEvents();
	}
}
