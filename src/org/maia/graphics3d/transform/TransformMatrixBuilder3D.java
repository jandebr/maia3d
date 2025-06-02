package org.maia.graphics3d.transform;

import java.util.Arrays;

public class TransformMatrixBuilder3D {

	private double[] values;

	public TransformMatrixBuilder3D() {
		this.values = new double[16];
	}

	public TransformMatrixBuilder3D(double[] values) {
		this.values = Arrays.copyOf(values, values.length);
	}

	public TransformMatrixBuilder3D(TransformMatrix3D matrix) {
		this(matrix.getValues());
	}

	public TransformMatrix3D build() {
		return new TransformMatrix3D(getValues());
	}

	public double getValue(int row, int col) {
		return getValues()[row * 4 + col];
	}

	public TransformMatrixBuilder3D setValue(int row, int col, double value) {
		getValues()[row * 4 + col] = value;
		return this;
	}

	private double[] getValues() {
		return values;
	}

}
