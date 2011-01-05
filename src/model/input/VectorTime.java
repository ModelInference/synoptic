package model.input;

import java.util.ArrayList;
import java.util.List;

public class VectorTime {
	ArrayList<Integer> vector = new ArrayList<Integer>();

	/**
	 * Builds a VectorTime from a string that looks like "1,2,3"
	 * @param timeStr string input representing a vtime
	 */
	public VectorTime(String timeStr) {
		String[] times = timeStr.split(",");
		for (String t : times)
			vector.add(Integer.parseInt(t));
	}

	/**
	 * Builds a VectorTime from a vector
	 * @param vector input vector
	 */
	public VectorTime(List<Integer> vector) {
		this.vector.addAll(vector);
	}

	/**
	 * Returns true if (this < t), otherwise returns false
	 * @param t the other vtime
	 * @return
	 */
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

	/**
	 * @return Whether or not this is a unit vector
	 */
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

	/**
	 * @return Whether or not the vector is of length 1
	 */
	public boolean isSingular() {
		return vector.size() == 1;
	}
	
	/**
	 * Increments vtime at an index and returns the new vtime
	 * @param index
	 * @return the newly created, incremented vtime 
	 */
	public VectorTime step(int index) {
		List<Integer> vector = new ArrayList<Integer>();
		vector.addAll(this.vector);
		vector.set(index, vector.get(index) + 1);
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
	
	public String toString() {
		return vector.toString();
	}
}