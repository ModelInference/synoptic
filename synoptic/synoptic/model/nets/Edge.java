package model.nets;

public class Edge<S, T> {
	protected S source;
	protected T target;
	private int weight = 1;

	public Edge(S source, T target, int weight) {
		this.source = source;
		this.target = target;
		this.weight = weight;
	}

	public T getTarget() {
		return target;
	}

	public S getSource() {
		return source;
	}

	public void setSource(S source) {
		this.source = source;
	}

	public void setTarget(T target) {
		this.target = target;
	}

	public void setWeight(int weight) {
		this.weight += weight;
	}

	public int getWeight() {
		return weight;
	}

	@Override
	public String toString() {
		return Integer.toString(weight);
	}
}
