/**
 * 
 */
package util;

import java.io.Serializable;

/**
 * @author Pierre Marques
 *
 */
public final class Pair<A, B> implements Serializable{
	private static final long serialVersionUID = 1L;
	public A a;
	public B b;
	
	public Pair() {}
	
	public Pair(A a, B b) {
		super();
		this.a = a;
		this.b = b;
	}
	
}
