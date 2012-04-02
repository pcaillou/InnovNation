package util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A container mapping keys to a collection of values.</br>
 * Essentially, this is a Map variant allowing multiple values on each key.
 * @author Pierre Marques
 * 
 * @param <Key> type of keys
 * @param <Value> type of mapped values
 */
public interface MultiMap<Key, Value> {

	/**
	 * @return the number of values mapped in this collection
	 */
	int size();

	/**
	 * tells if this MultiMap is empty.
	 * @return true if no value is mapped, else false
	 */
	boolean isEmpty();

	/**
	 * tells if the given key is present in the container.
	 * @param key the key to look for
	 * @return true if the key is associated to some value, else false
	 */
	boolean containsKey(Key key);

	/**
	 * tells if the given value is present in the container.
	 * @param value the value to look for
	 * @return true if the value is associated to some key, else false
	 */
	boolean containsValue(Value value);

	/**
	 * tells if the given value is mapped to the given key.
	 * @param key the key to which the value is supposedly associated
	 * @param value the value to find
	 * @return true if the key exists and one of its associated values is the value to find
	 */
	boolean contains(Key key, Value value);
	
	/**
	 * Returns the list of values to which the given key is mapped, or null if not present.
	 * @param key the key whose list is to be returned
	 * @return the list of values, or null if the key is not mapped
	 */
	List<Value> get(Key key);

	/**
	 * Maps the given value to the given key.
	 * @param key the key to map the value to
	 * @param value the value to map
	 */
	void put(Key key, Value value);

	/**
	 * Returns the list of the keys mapped in this multimap.
	 * @return a set view of the mapped keys
	 */
	Set<Key> keySet();

	/**
	 * Returns the collection of every values mapped in this multimap.<br/> 
	 * There can be duplicates, as the multimap has no restriction on them.
	 * @return a collection ov every values.
	 */
	Collection<Value> values();

}