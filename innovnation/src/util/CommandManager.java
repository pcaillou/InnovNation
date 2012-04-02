package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.event.EventListenerList;


/**
 * A command manager used to pipe commands into listeners.<br/>
 * It has built-in support for aliases (alias, unalias and listalias)
 * This is a Runnable, to start a console
 * @author Pierre Marques
 *
 */
public class CommandManager implements Runnable{
	
	private final class AliasCommand extends CommandListener{
		
		private final String aliased;
		public AliasCommand(String alias, String command) {
			super(alias, "déclenche "+command);
			aliased=command;
		}
		
		public String getCommand() {
			return aliased;
		}
		
		@Override
		public void command(Collection<String> arguments) {
			execute(aliased, arguments);
		}
	}
	
	private final EventListenerList listeners;
	
	public CommandManager() {
		this.listeners = new EventListenerList();
		
		addCommandListener( new CommandListener( "help",
				"<commandes>",
				"affiche la liste des commandes, ou l'aide de chaque commande indiquée"
		) {
			@Override
			public void command(Collection<String> arguments) {
				if(arguments.isEmpty()){
					//list commandes
					for(CommandListener c : listeners.getListeners(CommandListener.class) ){
						System.out.println(c.getTrigger());
					}
					for(AliasCommand c : listeners.getListeners(AliasCommand.class) ){
						System.out.println("alias "+c.getTrigger()+" "+c.getCommand());
					}
				} else {
					for(String s : arguments){
						for(CommandListener c : listeners.getListeners(CommandListener.class)){
							if(c.triggersOn(s)) System.out.println(c);
						}
						for(AliasCommand c : listeners.getListeners(AliasCommand.class)){
							if(c.triggersOn(s)) System.out.println(c);
						}
					}
				}
			}
		});

		addCommandListener( new CommandListener( "alias",
				"<commandes>",
				"crée un alias vers la chaine de commande indiquée. chaque commande recevra tous les arguments de l'alias"
		) {
			@Override
			public void command(Collection<String> arguments) {
				if(arguments.size()<2) return;
				Iterator<String> argument = arguments.iterator();
				String alias = argument.next();
				removeAlias(alias);
				while(argument.hasNext()){
					addAlias(alias, argument.next());					
				}
			}
		});

		addCommandListener( new CommandListener( "unalias",
				"<alias>",
				"supprime les alias indiqués"
		) {
			@Override
			public void command(Collection<String> arguments) {
				for(String argument : arguments) removeAlias(argument);
			}
		});

		addCommandListener( new CommandListener( "listalias",
				"affiche la liste des alias"
		) {
			@Override
			public void command(Collection<String> arguments) {
				AliasCommand[] list = listeners.getListeners(AliasCommand.class);
				for(AliasCommand ac : list){
					System.out.println("alias "+ac.getTrigger()+" "+ac.getCommand());
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			System.out.print("> ");
			line = br.readLine();
			while (line != null){
				line = line.trim();
				try{
					parse(line);
				} catch (IllegalArgumentException e){
					System.out.println(e.getMessage());
				}
				System.out.print("> ");
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public final void addCommandListener(CommandListener l){
		listeners.add(CommandListener.class, l);
	}
	
	public final void removeCommandListener(CommandListener l){
		listeners.remove(CommandListener.class, l);
	}
	
	public final void addAlias(String alias, String command){
		listeners.add(AliasCommand.class, new AliasCommand(alias, command));
	}
	
	public final void removeAlias(String alias){
		AliasCommand[] list = listeners.getListeners(AliasCommand.class);
		for(AliasCommand ac : list){
			if(ac.triggersOn(alias)) listeners.remove(AliasCommand.class, ac);
		}
	}
	
	protected final void execute(String trigger, Collection<String> arguments){
		for( Object o : listeners.getListenerList()){
			if(o instanceof CommandListener){
				if( ((CommandListener) o).triggersOn(trigger) ){
					((CommandListener) o).command(arguments);
				}
			}
		}
	}

	public void parse(String line) throws IllegalArgumentException {
		if(line.isEmpty()) return;
		/*
		 * tokenize on "
		 * check for \"
		 * every even tokens is to tokenize normaly, every odd ones are strings
		 */
		
		StringTokenizer unquoter = new StringTokenizer(line, "\"");
		LinkedList<String> arguments = new LinkedList<String>();
		
		boolean splitting = true;
		//mettre dans unquoted les tokens réels
		while( unquoter.hasMoreTokens() ){
			StringBuilder sb = new StringBuilder();
			
			//skip escaped quotes
			do {
				if(sb.length()>0) sb.setCharAt(sb.length()-1, '"');
				sb.append(unquoter.nextToken());
			} while(sb.charAt(sb.length()-1)=='\\' && unquoter.hasMoreTokens());
			
			if(splitting){
				StringTokenizer st = new StringTokenizer(sb.toString());
				while(st.hasMoreTokens()) arguments.add(st.nextToken());
			} else {
				arguments.add(sb.toString());
			}
			splitting = !splitting;
		}
		
		try{
			String trigger = arguments.pop();
			execute(trigger, arguments);
		} catch (NoSuchElementException e){
			return;
		}
	}

}
