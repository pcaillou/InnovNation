/**
 * 
 */
package util.graph;

/**
 * Utility interface to do some work on a collection
 * @author Pierre Marques
 *
 * @param <T>
 */
public interface Worker<T>{
	void work(T content);
	void start(T content);
	void end(T content);
}