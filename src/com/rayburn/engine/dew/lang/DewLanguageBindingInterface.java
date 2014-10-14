package com.rayburn.engine.dew.lang;

/**
 * @author Will Stuckey
 * @date 3/3/14
 * <p>This interface contains the system calls the DewInterpreter needs to run.</p>
 */
public interface DewLanguageBindingInterface
{
	/**
	 * Passes the interpreter assertions for setting and evaluating properties.
	 * @param assertions interpreter assertions, empty if no assertions
	 */
	public void setInterpreterAssertions(Object[] assertions);

	/**
	 * Searches the Object-Property tree for an entity in the given path.
	 * @param entityTreePath the path through the tree to the entity
	 * @return the entity, null if reference not found
	 */
	public Object getBoundEntity(String entityTreePath);

	/**
	 * Searches the Object-Property tree for a property belonging to an entity in the given path.
	 * @param propertyTreePath the path through the tree to the entity
	 * @return the property, null if reference not found
	 */
	public Object getBoundProperty(String propertyTreePath);

	/**
	 * Searches the Object-Property tree for a property belonging to an entity in the given path, then sets its value.
	 * @param propertyTreePath
	 * @param value
	 * @return if the value was successfully set
	 */
	public boolean setBoundProperty(String propertyTreePath, Object value);

	/**
	 * Tells the system to print an object
	 * @param o
	 */
 	public void print(Object o);

	/**
	 * Has the command processor to execute a command via implicit console
	 * @param commandLine the entire command line
	 * @see com.rayburn.engine.dew.Console
	 */
	public void execInternalCommand(String commandLine);

	/**
	 * Tells the system to sync before an interpreter call. This function is only called when the interpreter determines
	 * the next operation might require a sync to be thread safe.
	 */
	public void sync();

	/**
	 * Tells the system to sync around a sync object before an interpreter call. This function is only called when the
	 * interpreter determines the next operation might require a sync to be thread safe.
	 * @param syncObject
	 */
	public void sync(Object syncObject);

	/**
	 * Instructs the system to halt interpreter operations and sync.
	 */
	public void halt();

	/**
	 * Instructs the system to stop
	 * @param exitCode an exit code for debugging
	 */
	public void exit(int exitCode);

	/**
	 * Instructs the system to force quit
	 * @param exitCode an exit code for debugging
	 */
	public void forceExit(int exitCode);
}
