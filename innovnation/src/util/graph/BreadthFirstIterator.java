package util.graph;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Reproduction of BreadthFirstEnumeration from TreeModel from Sun JDK 1.6.0.24
 * 
 * @author Pierre Marques
 * 
 * @param <K> key type
 */
public class BreadthFirstIterator<K> implements Iterator<K> {
	private Queue queue;
	private Navigator<K> navigator;
	
	public BreadthFirstIterator(final K rootNode, Navigator<K> navigator) {
		super();
		queue = new Queue();
		this.navigator=navigator;
		queue.enqueue(new Iterator<K>() {
			boolean hasNext=true;
			K n = rootNode;
			public void remove() {}
			public K next() { return n; }
			public boolean hasNext() { return hasNext ? !(hasNext=false) : false; }
		});
	}

	public boolean hasNext() {
		return (!queue.isEmpty() && queue.firstObject().hasNext());
	}

	public K next() {
		Iterator<K> i = queue.firstObject();
		K    node = i.next();
		Iterator<K> nextIterator = navigator.navigate(node);

		if (!i.hasNext()) queue.dequeue();
		if (nextIterator.hasNext()) queue.enqueue(nextIterator);
		return node;
	}

	public void remove() { throw new UnsupportedOperationException(); }

	// A simple queue with a linked list data structure.
	final class Queue {
		QNode head=null; // null if empty
		QNode tail=null;

		final class QNode {
			public Iterator<K>   object;
			public QNode next;//null if end
			public QNode(Iterator<K> o) {
				this.object = o;
				this.next = null;
			}
		}

		public void enqueue(Iterator<K> o) {
			if (head == null) head = tail = new QNode(o);
			else {
				tail.next = new QNode(o);
				tail = tail.next;
			}
		}

		public Iterator<K> dequeue() {
			if (head == null) throw new NoSuchElementException("No more elements");
			Iterator<K> retval = head.object;
			QNode oldHead = head;
			head = head.next;
			if (head == null) tail = null;
			else oldHead.next = null;
			return retval;
		}

		public Iterator<K> firstObject() {
			if(head == null) throw new NoSuchElementException("No more elements");
			return head.object;
		}

		public boolean isEmpty() { return head == null; }

	} // End of class Queue

}
