/**
 * 
 */
package util.graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.HashMultiMap;
import util.MultiMap;



/**
 * Directed Acylic Graph
 * @author Pierre Marques
 */
public final class Dag<Key, T> implements Serializable, Iterable<T>{
	private static final long serialVersionUID = 1L;
	
	private Map<Key, T> contents;
	private MultiMap<Key, Key> links;
	transient private MultiMap<Key, Key> reverseLinks;
	
	private class ValueIterator implements Iterator<T>{
		private Iterator<Key> iterator;

		public ValueIterator() {
			this.iterator = contents.keySet().iterator();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			return get(iterator.next());
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private class ShowBfsWorker<V> implements Worker<V> {
		@Override
		public void work(V content) { System.out.print(" "+content); }
		@Override
		public void start(V content){
			System.out.print("BFS iteration from "+content+':');
		}
		@Override
		public void end(V content){ System.out.println(); }
	};

	private Navigator<Key> fwd = new Navigator<Key>() {
		@Override
		public Iterator<Key> navigate(Key node) {
			return links.get(node).iterator();
		}
	};
	//private Navigator<T> bwd = new DagNode.Backward<T>();


	/**
	 * Empty constructor.
	 */
	public Dag() {
		super();
		contents = new LinkedHashMap<Key, T>();
		links = new HashMultiMap<Key, Key>();
		reverseLinks= new HashMultiMap<Key, Key>();
	}

	/**
	 * tells if the given key is associated to a value
	 * @param id the key to find
	 * @return true if the key is associated
	 */
	public boolean exists(Key id){
		return contents.containsKey(id);
	}

	/**
	 * tells what is the value associated to the given key
	 * @param id the key to find
	 * @return the associated value or null if absent
	 */
	public T get(Key id) {
		try{
			return contents.get(id);
		} catch (NullPointerException e) {
			return null;
		}
	}


	/**
	 * returns a list of the values mapped to some keys.
	 * @param keys list of keys to look for
	 * @return a list of the values mapped to the given keys.
	 */
	public LinkedList<T> mapped(Collection<Key> keys) {
		LinkedList<T> res = new LinkedList<T>();
		for(Key key : keys) {
			T v = contents.get(key);
			if(v!=null)	res.add(v);
		}
		return res;
	}
	
	
	/**
	 * Creates a node linking value to the key id
	 * @param id key to use
	 * @param value value to link to id
	 */
	public void createNode(Key id, T value){
		contents.put(id, value);
	}


	/**
	 * make a relation in such a way that <code>enfant</code> is a child of <code>parent</code>
	 * @param parent parent node id
	 * @param child child node id
	 */
	public void makeRelation(Key parent, Key child) throws IllegalArgumentException{
		if(!exists(child) || !exists(parent)) throw new IllegalArgumentException();
		//cancel duplicate entries
		if(links.contains(parent, child)) return;
		
		links.put(parent, child);
		reverseLinks.put(child, parent);
	}


	/**
	 * make a dependance in such a way that <code>dependent</code> is a child of <code>value</code>
	 * @param child dependant node id
	 * @param parent value node id
	 */
	public void makeDepend(Key child, Key parent){
		makeRelation(parent, child);
	}

	/**
	 * Execute worker sur tout les noeuds dépendants de source (ses descendants),
	 * sachant que source est évaluée.
	 * @param source identifiant du noeud d'origine
	 * @param worker tache a effectuer sur le noeud évalué
	 */
	public void checkDependencies(Key source, Worker<T> worker){
		if(!exists(source)) throw new NullPointerException();
		
		worker.start(contents.get(source));
		for(Iterator<Key> iterator = new BreadthFirstIterator<Key>(source,fwd); iterator.hasNext();) {
			Key current = iterator.next();
			worker.work(contents.get(current));
		}
		worker.end(contents.get(source));
	}


	/**
	 * print the sequence of nodes traveled during a checkDependencies from root;
	 * @param root the id of the node to check from
	 */
	public void showBfs(Key root){
		checkDependencies(root, new ShowBfsWorker<T>());
	}

	
	public Collection<T> getParents(Key nodeId){
		
		List<Key> parentsIds = reverseLinks.get(nodeId);
		
		if ( (parentsIds == null) || (parentsIds.isEmpty()) ) // quick & efficient exit when possible
			return Collections.emptyList();
		
		final LinkedList<T> parents = new LinkedList<T>();
		for (Key currentKey : parentsIds) {
			parents.add(contents.get(currentKey)); // nota: this assumes that contents never returns null !
		}
		
		return parents;
		
	}


	public Collection<T> getChildren(Key nodeId){
		
		List<Key> childrenIds = reverseLinks.get(nodeId);
		
		if ( (childrenIds == null) || (childrenIds.isEmpty()) ) // quick & efficient exit when possible
			return Collections.emptyList();
		
		final LinkedList<T> children = new LinkedList<T>();
		for (Key currentKey : childrenIds) {
			children.add(contents.get(currentKey)); // nota: this assumes that contents never returns null !
		}
		
		return children;
		
	}

	public Collection<Key> getParentsIds(Key nodeId){
		Collection<Key> res = reverseLinks.get(nodeId);
		
		if (res == null)
			return Collections.<Key>emptyList();
		
		return res;
	}
	
	public Collection<Key> getChildrenIds(Key nodeId){

		Collection<Key> res = links.get(nodeId);
		
		if (res == null)
			return Collections.<Key>emptyList();
		
		return res;
	}
	
	public boolean hasParents(Key nodeId) {
		List<Key> l = reverseLinks.get(nodeId);
		
		return ( (l != null) && (!l.isEmpty()) );
	}
	
	public boolean hasChildren(Key nodeId) {
		List<Key> l = links.get(nodeId);
		
		return ( (l != null) && (!l.isEmpty()) );
	}
	
	/**
	 * @return
	 */
	public int getCount() {
		return contents.size();
	}

	/**
	 * @return
	 */
	public Iterator<Key> keyIterator() {
		return contents.keySet().iterator();
	}


	/**
	 * @return an iterator over values
	 */
	public Iterator<T> iterator() {
		return new ValueIterator();
	}

	/*
	 * Serializing
	 */

	private void writeObject(ObjectOutputStream out)
	throws IOException{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		reverseLinks = new HashMultiMap<Key, Key>();
		for(Key parent : links.keySet()){
			for(Key child : links.get(parent))
				reverseLinks.put(child, parent);
		}
	}

}