package model.input;

import java.util.ArrayList;
import java.util.List;

public class VectorTime {
	ArrayList<Integer> vector = new ArrayList<Integer>();

	public VectorTime(String time) {
		String[] times = time.split(",");
		for (String t : times)
			vector.add(Integer.parseInt(t));
	}

	public VectorTime(List<Integer> vector) {
		this.vector.addAll(vector);
	}

	public boolean lessThan(VectorTime t) {
		boolean foundStrictlyLess = false;
		for (int i = 0; i < vector.size(); ++i) {
			if (vector.get(i) < t.vector.get(i))
				foundStrictlyLess = true;
			else if (vector.get(i) > t.vector.get(i))
				return false;
		}
		return foundStrictlyLess;
	}

	public boolean isOneTime() {
		boolean sawOne = false;
		for (int i = 0; i < vector.size(); ++i) {
			if (sawOne && vector.get(i) == 1)
				return false;
			if (vector.get(i) == 1)
				sawOne = true;
			if (vector.get(i) > 0)
				return false;
		}
		return true;
	}

	public VectorTime step(int i) {
		List<Integer> vector = new ArrayList<Integer>();
		vector.addAll(this.vector);
		vector.set(i, vector.get(i) + 1);
		return new VectorTime(vector);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((vector == null) ? 0 : vector.hashCode());
		for (int i = 0; i < vector.size(); ++i) {
			result += prime*result + vector.get(i).hashCode();
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VectorTime other = (VectorTime) obj;
		if (vector == null) {
			if (other.vector != null)
				return false;
		} else {
			if (vector.size() != other.vector.size())
				return false;
			for (int i = 0; i < vector.size(); ++i) {
				if (vector.get(i) != other.vector.get(i))
					return false;
			}
		}
		return true;
	}
}
