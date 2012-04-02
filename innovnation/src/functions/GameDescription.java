/**
 * 
 */
package functions;

import java.rmi.RemoteException;

/**
 * @author Pierre Marques
 *
 */
public final class GameDescription implements IGameDescription {
	
	private static final long serialVersionUID = 1L;
	
	private final String bindName, name, theme;
	
	public GameDescription(String bindName, String name, String theme) throws RemoteException {
		super();
		this.bindName = bindName;
		this.name = name;
		this.theme = theme;
	}

	public GameDescription(IGameDescription d) throws RemoteException {
		this.bindName = d.getBindName();
		this.name = d.getName();
		this.theme = d.getTheme();
	}

	/* (non-Javadoc)
	 * @see functions.IGameDescription#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see functions.IGameDescription#getBindName()
	 */
	@Override
	public String getBindName() {
		return bindName;
	}
	
	/* (non-Javadoc)
	 * @see functions.IGameDescription#getRootIdeaName()
	 */
	@Override
	public String getTheme() {
		return theme;
	}

	@Override
	public String toString() {
		return new StringBuilder(name)
		.append(" about ")
		.append(theme)
		.append("\n\tat ")
		.append(bindName)
		.toString();
	}
}
