package org.maia.graphics3d.model.camera;

import org.maia.graphics3d.transform.TransformMatrix3D;
import org.maia.graphics3d.transform.Transformation3D;
import org.maia.graphics2d.geometry.Radians;
import org.maia.graphics2d.geometry.Rectangle2D;
import org.maia.graphics3d.geometry.Box3D;

public class PerspectiveViewVolume implements ViewVolume {

	private double viewAngleInDegrees;

	private double aspectRatio;

	private double N;

	private double F;

	private PerspectiveViewVolume(double viewAngleInDegrees, double aspectRatio, double N, double F) {
		if (N >= F)
			throw new IllegalArgumentException("Near plane is not before the far plane: " + N + " >= " + F);
		setViewAngleInDegrees(viewAngleInDegrees);
		setAspectRatio(aspectRatio);
		setN(N);
		setF(F);
	}

	public static PerspectiveViewVolume createFromParameters(double viewAngleInDegrees, double aspectRatio, double N,
			double F) {
		return new PerspectiveViewVolume(viewAngleInDegrees, aspectRatio, N, F);
	}

	public static PerspectiveViewVolume createToEncloseInDepth(Box3D box, double viewAngleInDegrees, double aspectRatio) {
		if (box.getZ2() >= 0)
			throw new IllegalArgumentException("The box goes before the eye's plane: " + box);
		double N = -box.getZ2(); // is positive
		double F = -box.getZ1(); // is positive
		if (N == F)
			F += 0.001;
		return createFromParameters(viewAngleInDegrees, aspectRatio, N, F);
	}

	public static PerspectiveViewVolume createToEncloseEntirely(Box3D box, double aspectRatio) {
		PerspectiveViewVolume viewVolume = createToEncloseInDepth(box, 60.0, aspectRatio);
		double top = Math.max(Math.abs(box.getY1()), Math.abs(box.getY2()));
		double right = Math.max(Math.abs(box.getX1()), Math.abs(box.getX2()));
		if (right / top > aspectRatio) {
			top = right / aspectRatio;
		}
		double viewAngleInDegrees = Radians.radiansToDegrees(Math.atan(top / viewVolume.getN()) * 2);
		viewVolume.setViewAngleInDegrees(viewAngleInDegrees);
		return viewVolume;
	}

	@Override
	public final boolean isPerspectiveProjection() {
		return true;
	}

	@Override
	public TransformMatrix3D getProjectionMatrix() {
		return Transformation3D.getPerspectiveProjectionMatrix(Radians.degreesToRadians(getViewAngleInDegrees()),
				getAspectRatio(), getN(), getF());
	}

	@Override
	public Rectangle2D getViewPlaneRectangle() {
		double top = getN() * Math.tan(Radians.degreesToRadians(getViewAngleInDegrees() / 2));
		double bottom = -top;
		double right = top * getAspectRatio();
		double left = -right;
		return new Rectangle2D(left, right, bottom, top);
	}

	@Override
	public double getViewPlaneZ() {
		return -getN(); // is negative
	}

	@Override
	public double getFarPlaneZ() {
		return -getF(); // is negative
	}

	@Override
	public double getViewAngleInDegrees() {
		return viewAngleInDegrees;
	}

	public void setViewAngleInDegrees(double viewAngleInDegrees) {
		if (viewAngleInDegrees <= 0 || viewAngleInDegrees >= 90)
			throw new IllegalArgumentException("View angle is out of bounds: " + viewAngleInDegrees);
		this.viewAngleInDegrees = viewAngleInDegrees;
	}

	@Override
	public double getAspectRatio() {
		return aspectRatio;
	}

	public void setAspectRatio(double aspectRatio) {
		if (aspectRatio <= 0)
			throw new IllegalArgumentException("Aspect ratio is not strictly positive: " + aspectRatio);
		this.aspectRatio = aspectRatio;
	}

	public double getN() {
		return N;
	}

	public void changeN(double N) {
		if (N >= getF())
			throw new IllegalArgumentException("Near plane is not before the far plane: " + N + " >= " + getF());
		setN(N);
	}

	private void setN(double N) {
		if (N <= 0)
			throw new IllegalArgumentException("N is not strictly positive: " + N);
		this.N = N;
	}

	public double getF() {
		return F;
	}

	public void changeF(double F) {
		if (F <= getN())
			throw new IllegalArgumentException("Near plane is not before the far plane: " + getN() + " >= " + F);
		setF(F);
	}

	private void setF(double F) {
		if (F <= 0)
			throw new IllegalArgumentException("F is not strictly positive: " + F);
		this.F = F;
	}

}
