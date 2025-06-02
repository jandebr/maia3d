package org.maia.graphics3d.transform;

import java.util.List;
import java.util.Vector;

import org.maia.graphics3d.geometry.Point3D;

/**
 * Represents a sequence of transformations, intended for "forward" application
 * 
 * @see ReverseCompositeTransform3D
 */
public class CompositeTransform3D {

	/**
	 * The individual transformation matrices in the sequence. The order corresponds to the order in which the
	 * transformations are to be applied.
	 */
	private List<TransformMatrix3D> matrices = new Vector<TransformMatrix3D>();

	/**
	 * The combined result of applying all the transformation matrices in the subsequence.
	 * 
	 * In symbols,
	 * <code>C[i] = M[i] * C[i-1] = M[i] * M[i-1] * C[i-2] = ... = M[i] * M[i-1] * M[i-2] * ... * M[0]</code> , where
	 * <code>C</code> denotes <code>compositeMatrices</code> and <code>M</code> denotes <code>matrices</code>
	 */
	private List<TransformMatrix3D> compositeMatrices = new Vector<TransformMatrix3D>();

	public CompositeTransform3D() {
		reset(); // initialize with the Identity transform
	}

	public CompositeTransform3D then(TransformMatrix3D matrix) {
		getMatrices().add(matrix);
		getCompositeMatrices().add(extendCompositeMatrix(getCompositeMatrix(), matrix));
		return this;
	}

	public CompositeTransform3D undo() {
		int n = getMatrices().size();
		if (n > 1) {
			getMatrices().remove(n - 1);
			getCompositeMatrices().remove(n - 1);
		} else {
			throw new IllegalStateException(
					"Attempting to invoke CompositeTransform.previous() when in the initial state");
		}
		return this;
	}

	public CompositeTransform3D undoFrom(int stepIndex) {
		validateStepIndex(stepIndex);
		int n = getMatrices().size() - stepIndex;
		for (int i = 0; i < n; i++) {
			undo();
		}
		return this;
	}

	public CompositeTransform3D replace(int stepIndex, TransformMatrix3D matrix) {
		validateStepIndex(stepIndex);
		if (stepIndex == getIndexOfCurrentStep()) {
			return undo().then(matrix);
		} else {
			List<TransformMatrix3D> redo = new Vector<TransformMatrix3D>(getMatrices().subList(stepIndex + 1,
					getMatrices().size()));
			undoFrom(stepIndex).then(matrix);
			for (TransformMatrix3D R : redo) {
				then(R);
			}
			return this;
		}
	}

	public CompositeTransform3D reset() {
		getMatrices().clear();
		getCompositeMatrices().clear();
		return then(Transformation3D.getIdentityMatrix());
	}

	public int getIndexOfCurrentStep() {
		return getMatrices().size() - 1;
	}

	private void validateStepIndex(int stepIndex) {
		if (stepIndex <= 0 || stepIndex > getIndexOfCurrentStep())
			throw new IndexOutOfBoundsException("Step index out of bounds [1," + getIndexOfCurrentStep() + "]: "
					+ stepIndex);
	}

	public Point3D transform(Point3D point) {
		return getCompositeMatrix().transform(point);
	}

	public TransformMatrix3D getCompositeMatrix() {
		int n = getCompositeMatrices().size();
		if (n == 0) {
			return Transformation3D.getIdentityMatrix();
		} else {
			return getCompositeMatrices().get(n - 1);
		}
	}

	protected TransformMatrix3D extendCompositeMatrix(TransformMatrix3D composite, TransformMatrix3D matrix) {
		return matrix.preMultiply(composite);
	}

	public List<TransformMatrix3D> getMatrices() {
		return matrices;
	}

	private List<TransformMatrix3D> getCompositeMatrices() {
		return compositeMatrices;
	}

}
