package util.graph;

import java.util.Iterator;

public interface Navigator<T> {
	Iterator<T> navigate(T node);
}
