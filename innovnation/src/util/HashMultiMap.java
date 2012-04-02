package util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HashMultiMap<Key, Value> implements MultiMap<Key, Value> {
	private Map<Key, List<Value> > innerMap;
	
	public HashMultiMap() {
		this.innerMap = new HashMap<Key, List<Value>>();
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#size()
	 */
	@Override
	public int size() {
		int n=0;
		for(List<Value> l : innerMap.values()) n+=l.size();
		return n;
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return innerMap.isEmpty();
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Key key) {
		return innerMap.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Value value) {
		for(List<Value> l : innerMap.values()){
			if(l.contains(value)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see util.MultiMap#contains(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean contains(Key key, Value value) {
		if(!innerMap.containsKey(key)) return false;
		return innerMap.get(key).contains(value);
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#get(java.lang.Object)
	 */
	@Override
	public List<Value> get(Key key) {
		return innerMap.get(key);
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#put(Key, Value)
	 */
	@Override
	public void put(Key key, Value value) {
		if(innerMap.containsKey(key)){
			innerMap.get(key).add(value);
		} else {
			List<Value> l = new LinkedList<Value>();
			l.add(value);
			innerMap.put(key, l);
		}
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#keySet()
	 */
	@Override
	public Set<Key> keySet() {
		return innerMap.keySet();
	}

	/* (non-Javadoc)
	 * @see data.MultiMap#values()
	 */
	@Override
	public Collection<Value> values() {
		List<Value> all = new LinkedList<Value>();
		for(List<Value> l : innerMap.values()) all.addAll(l);
		return all;
	}

}
