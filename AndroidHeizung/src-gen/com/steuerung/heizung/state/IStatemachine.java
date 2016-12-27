package com.steuerung.heizung.state;

/**
 * Basic interface for state machines.
 */
public interface IStatemachine {

	/**
	 * Initializes the state machine. Used to initialize internal variables etc.
	 */
	public void init();

	/**
	 * Enters the state machine. Sets the state machine into a defined state.
	 */
	public void enter();

	/**
	 * Exits the state machine. Leaves the state machine with a defined state.
	 */
	public void exit();

	/**
	 * Checks whether the state machine is active. 
	 * A state machine is active if it has been entered. It is inactive if it has not been entered at all or if it has been exited.
	 */
	public boolean isActive();

	/**
	 * Checks whether all active states are final. 
	 * If there are no active states then the state machine is considered being incative. In this case this method returns <code>false</code>.
	 */
	public boolean isFinal();

	/**
	* Start a run-to-completion cycle.
	*/
	public void runCycle();
}
