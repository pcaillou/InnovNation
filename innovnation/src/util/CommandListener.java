package util;

import java.util.Collection;
import java.util.EventListener;

/**
 * A listener that gets triggered by some command.<br/>
 * It has help informations. 
 * @author Pierre Marques
 */
public abstract class CommandListener implements EventListener {
	private final String trigger, usage, help, toStringValue;

	public CommandListener(String trigger, String help) {
		super();
		this.trigger = trigger;
		this.usage = null;
		this.help = help;
		this.toStringValue = new StringBuilder("usage: ")
			.append(this.trigger).append(":\n")
			.append(help).toString();
	}
	
	public CommandListener(String trigger, String usage, String help) {
		super();
		this.trigger = trigger;
		this.usage = usage;
		this.help = help;
		this.toStringValue = new StringBuilder("usage: ")
			.append(this.trigger).append(" ").append(usage).append(":\n")
			.append(help).toString();
	}

	/**
	 * @return the command trigger
	 */
	public final String getTrigger() {
		return trigger;
	}

	/**
	 * @return the description of the parameters
	 */
	public String getUsage() {
		return usage;
	}

	/**
	 * @return the help description of the command
	 */
	public String getHelp() {
		return help;
	}
	
	/**
	 * @return the help description of the command, with command invocation
	 */
	public String toString() {
		return toStringValue;
	}
	
	public final boolean triggersOn(String trigger) {
		return this.trigger.equals(trigger);
	}
	
	public abstract void command(Collection<String> arguments);

}
