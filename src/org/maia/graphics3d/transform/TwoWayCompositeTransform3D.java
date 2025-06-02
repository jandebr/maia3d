package org.maia.graphics3d.transform;

import java.util.List;

import org.maia.graphics3d.geometry.Point3D;

/**
 * Represents a sequence of transformations, supporting both "forward" and "reverse" application
 * 
 * <p>
 * Essentially brings together one <code>CompositeTransform3D</code> (for forward application) and one
 * <code>ReverseCompositeTransform3D</code> (for reverse application), referencing a sequence of individual
 * transformations and their inverses, respectively.
 * </p>
 * 
 * @see CompositeTransform3D
 * @see ReverseCompositeTransform3D
 */
public class TwoWayCompositeTransform3D {

	private CompositeTransform3D forwardCompositeTransform;

	private CompositeTransform3D reverseCompositeTransform;

	public TwoWayCompositeTransform3D() {
		this.forwardCompositeTransform = new CompositeTransform3D();
		this.reverseCompositeTransform = new ReverseCompositeTransform3D();
	}

	public TwoWayCompositeTransform3D then(TransformMatrix3D matrix) {
		getForwardCompositeTransform().then(matrix);
		getReverseCompositeTransform().then(Transformation3D.getInverseMatrix(matrix));
		// Set the inverses of the both current composite matrices
		getForwardCompositeMatrix().setInverseMatrix(getReverseCompositeMatrix());
		getReverseCompositeMatrix().setInverseMatrix(getForwardCompositeMatrix());
		return this;
	}

	public TwoWayCompositeTransform3D undo() {
		getForwardCompositeTransform().undo();
		getReverseCompositeTransform().undo();
		return this;
	}

	public TwoWayCompositeTransform3D undoFrom(int stepIndex) {
		getForwardCompositeTransform().undoFrom(stepIndex);
		getReverseCompositeTransform().undoFrom(stepIndex);
		return this;
	}

	public TwoWayCompositeTransform3D replace(int stepIndex, TransformMatrix3D matrix) {
		getForwardCompositeTransform().replace(stepIndex, matrix);
		getReverseCompositeTransform().replace(stepIndex, Transformation3D.getInverseMatrix(matrix));
		// Set the inverses of the both current composite matrices
		getForwardCompositeMatrix().setInverseMatrix(getReverseCompositeMatrix());
		getReverseCompositeMatrix().setInverseMatrix(getForwardCompositeMatrix());
		return this;
	}

	public TwoWayCompositeTransform3D reset() {
		getForwardCompositeTransform().reset();
		getReverseCompositeTransform().reset();
		return this;
	}

	public int getIndexOfCurrentStep() {
		return getForwardCompositeTransform().getIndexOfCurrentStep();
	}

	public Point3D forwardTransform(Point3D point) {
		return getForwardCompositeMatrix().transform(point);
	}

	public List<Point3D> forwardTransform(List<Point3D> points) {
		return getForwardCompositeMatrix().transform(points);
	}

	public Point3D reverseTransform(Point3D point) {
		return getReverseCompositeMatrix().transform(point);
	}

	public List<Point3D> reverseTransform(List<Point3D> points) {
		return getReverseCompositeMatrix().transform(points);
	}

	public TransformMatrix3D getForwardCompositeMatrix() {
		return getForwardCompositeTransform().getCompositeMatrix();
	}

	public TransformMatrix3D getReverseCompositeMatrix() {
		return getReverseCompositeTransform().getCompositeMatrix();
	}

	private CompositeTransform3D getForwardCompositeTransform() {
		return forwardCompositeTransform;
	}

	private CompositeTransform3D getReverseCompositeTransform() {
		return reverseCompositeTransform;
	}

}
