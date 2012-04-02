package util.graph;

public interface Collector<T> extends Navigator<T> {
	boolean isCollectible(T node);
}
