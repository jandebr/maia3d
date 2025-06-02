package org.maia.graphics3d.transform;

import org.maia.graphics3d.Metrics3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.geometry.Vector3D;
import org.maia.graphics3d.model.OrthographicProjection;

public class Transformation3D {

	private static TransformMatrix3D IDENTITY_MATRIX;

	private Transformation3D() {
	}

	public static TransformMatrix3D getIdentityMatrix() {
		if (IDENTITY_MATRIX == null) {
			TransformMatrix3D I = createIdentityMatrix();
			I.setInverseMatrix(I);
			IDENTITY_MATRIX = I;
		}
		return IDENTITY_MATRIX;
	}

	private static TransformMatrix3D createIdentityMatrix() {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getTranslationMatrix(double dx, double dy, double dz) {
		TransformMatrix3D M = createTranslationMatrix(dx, dy, dz);
		TransformMatrix3D I = createTranslationMatrix(-dx, -dy, -dz);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix3D createTranslationMatrix(double dx, double dy, double dz) {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(0, 3, dx);
		builder.setValue(1, 1, 1.0);
		builder.setValue(1, 3, dy);
		builder.setValue(2, 2, 1.0);
		builder.setValue(2, 3, dz);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getScalingMatrix(double sx, double sy, double sz) {
		TransformMatrix3D M = createScalingMatrix(sx, sy, sz);
		TransformMatrix3D I = createScalingMatrix(1.0 / sx, 1.0 / sy, 1.0 / sz);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix3D createScalingMatrix(double sx, double sy, double sz) {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, sx);
		builder.setValue(1, 1, sy);
		builder.setValue(2, 2, sz);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getRotationXrollMatrix(double angleInRadians) {
		TransformMatrix3D M = createRotationXrollMatrix(angleInRadians);
		TransformMatrix3D I = createRotationXrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix3D createRotationXrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, c);
		builder.setValue(1, 2, -s);
		builder.setValue(2, 1, s);
		builder.setValue(2, 2, c);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getRotationYrollMatrix(double angleInRadians) {
		TransformMatrix3D M = createRotationYrollMatrix(angleInRadians);
		TransformMatrix3D I = createRotationYrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix3D createRotationYrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, c);
		builder.setValue(0, 2, s);
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 0, -s);
		builder.setValue(2, 2, c);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getRotationZrollMatrix(double angleInRadians) {
		TransformMatrix3D M = createRotationZrollMatrix(angleInRadians);
		TransformMatrix3D I = createRotationZrollMatrix(-angleInRadians);
		M.setInverseMatrix(I);
		I.setInverseMatrix(M);
		return M;
	}

	private static TransformMatrix3D createRotationZrollMatrix(double angleInRadians) {
		double c = Math.cos(angleInRadians);
		double s = Math.sin(angleInRadians);
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, c);
		builder.setValue(0, 1, -s);
		builder.setValue(1, 0, s);
		builder.setValue(1, 1, c);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getOrthographicProjectionMatrix(OrthographicProjection projection) {
		if (OrthographicProjection.ONTO_XY_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoXYplaneMatrix();
		} else if (OrthographicProjection.ONTO_XZ_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoXZplaneMatrix();
		} else if (OrthographicProjection.ONTO_YZ_PLANE.equals(projection)) {
			return getOrthographicProjectionOntoYZplaneMatrix();
		}
		return null;
	}

	public static TransformMatrix3D getOrthographicProjectionOntoXYplaneMatrix() {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(1, 1, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getOrthographicProjectionOntoXZplaneMatrix() {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getOrthographicProjectionOntoYZplaneMatrix() {
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(1, 1, 1.0);
		builder.setValue(2, 2, 1.0);
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getCameraViewingMatrix(Point3D eye, Vector3D uUnit, Vector3D vUnit, Vector3D nUnit) {
		Vector3D e = eye.minus(Point3D.origin());
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, uUnit.getX());
		builder.setValue(0, 1, uUnit.getY());
		builder.setValue(0, 2, uUnit.getZ());
		builder.setValue(0, 3, -e.dotProduct(uUnit));
		builder.setValue(1, 0, vUnit.getX());
		builder.setValue(1, 1, vUnit.getY());
		builder.setValue(1, 2, vUnit.getZ());
		builder.setValue(1, 3, -e.dotProduct(vUnit));
		builder.setValue(2, 0, nUnit.getX());
		builder.setValue(2, 1, nUnit.getY());
		builder.setValue(2, 2, nUnit.getZ());
		builder.setValue(2, 3, -e.dotProduct(nUnit));
		builder.setValue(3, 3, 1.0);
		return builder.build();
	}

	public static TransformMatrix3D getPerspectiveProjectionMatrix(double viewAngleInRadians, double aspectRatio,
			double N, double F) {
		double top = N * Math.tan(viewAngleInRadians / 2);
		double bottom = -top;
		double right = top * aspectRatio;
		double left = -right;
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		builder.setValue(0, 0, 2 * N / (right - left));
		builder.setValue(0, 2, (right + left) / (right - left));
		builder.setValue(1, 1, 2 * N / (top - bottom));
		builder.setValue(1, 2, (top + bottom) / (top - bottom));
		builder.setValue(2, 2, -(F + N) / (F - N));
		builder.setValue(2, 3, -2 * F * N / (F - N));
		builder.setValue(3, 2, -1.0);
		return builder.build();
	}

	public static TransformMatrix3D getInverseMatrix(TransformMatrix3D matrix) throws MatrixInversionException {
		TransformMatrix3D inverse = matrix.getInverseMatrix();
		if (inverse == null) {
			inverse = createInverseMatrix(matrix);
			inverse.setInverseMatrix(matrix); // wiring for future reuse
			matrix.setInverseMatrix(inverse); // wiring for future reuse
		}
		return inverse;
	}

	private static TransformMatrix3D createInverseMatrix(TransformMatrix3D matrix) throws MatrixInversionException {
		Metrics3D.getInstance().incrementMatrixInversions();
		double det = computeDeterminant(matrix);
		if (det == 0)
			throw new MatrixInversionException();
		double[] T = matrix.getValues();
		TransformMatrixBuilder3D builder = new TransformMatrixBuilder3D();
		if (matrix.isAffine()) {
			builder.setValue(0, 0, (T[5] * T[10] - T[6] * T[9]) / det);
			builder.setValue(0, 1, (T[2] * T[9] - T[1] * T[10]) / det);
			builder.setValue(0, 2, (T[1] * T[6] - T[2] * T[5]) / det);
			builder.setValue(0, 3, (T[3] * T[6] * T[9] - T[2] * T[7] * T[9] - T[3] * T[5] * T[10] + T[1] * T[7] * T[10]
					+ T[2] * T[5] * T[11] - T[1] * T[6] * T[11]) / det);
			builder.setValue(1, 0, (T[6] * T[8] - T[4] * T[10]) / det);
			builder.setValue(1, 1, (T[0] * T[10] - T[2] * T[8]) / det);
			builder.setValue(1, 2, (T[2] * T[4] - T[0] * T[6]) / det);
			builder.setValue(1, 3, (T[2] * T[7] * T[8] - T[3] * T[6] * T[8] + T[3] * T[4] * T[10] - T[0] * T[7] * T[10]
					- T[2] * T[4] * T[11] + T[0] * T[6] * T[11]) / det);
			builder.setValue(2, 0, (T[4] * T[9] - T[5] * T[8]) / det);
			builder.setValue(2, 1, (T[1] * T[8] - T[0] * T[9]) / det);
			builder.setValue(2, 2, (T[0] * T[5] - T[1] * T[4]) / det);
			builder.setValue(2, 3, (T[3] * T[5] * T[8] - T[1] * T[7] * T[8] - T[3] * T[4] * T[9] + T[0] * T[7] * T[9]
					+ T[1] * T[4] * T[11] - T[0] * T[5] * T[11]) / det);
			builder.setValue(3, 3, (T[1] * T[6] * T[8] - T[2] * T[5] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9]
					- T[1] * T[4] * T[10] + T[0] * T[5] * T[10]) / det);
		} else {
			builder.setValue(0, 0, (T[6] * T[11] * T[13] - T[7] * T[10] * T[13] + T[7] * T[9] * T[14]
					- T[5] * T[11] * T[14] - T[6] * T[9] * T[15] + T[5] * T[10] * T[15]) / det);
			builder.setValue(0, 1, (T[3] * T[10] * T[13] - T[2] * T[11] * T[13] - T[3] * T[9] * T[14]
					+ T[1] * T[11] * T[14] + T[2] * T[9] * T[15] - T[1] * T[10] * T[15]) / det);
			builder.setValue(0, 2, (T[2] * T[7] * T[13] - T[3] * T[6] * T[13] + T[3] * T[5] * T[14]
					- T[1] * T[7] * T[14] - T[2] * T[5] * T[15] + T[1] * T[6] * T[15]) / det);
			builder.setValue(0, 3, (T[3] * T[6] * T[9] - T[2] * T[7] * T[9] - T[3] * T[5] * T[10] + T[1] * T[7] * T[10]
					+ T[2] * T[5] * T[11] - T[1] * T[6] * T[11]) / det);
			builder.setValue(1, 0, (T[7] * T[10] * T[12] - T[6] * T[11] * T[12] - T[7] * T[8] * T[14]
					+ T[4] * T[11] * T[14] + T[6] * T[8] * T[15] - T[4] * T[10] * T[15]) / det);
			builder.setValue(1, 1, (T[2] * T[11] * T[12] - T[3] * T[10] * T[12] + T[3] * T[8] * T[14]
					- T[0] * T[11] * T[14] - T[2] * T[8] * T[15] + T[0] * T[10] * T[15]) / det);
			builder.setValue(1, 2, (T[3] * T[6] * T[12] - T[2] * T[7] * T[12] - T[3] * T[4] * T[14]
					+ T[0] * T[7] * T[14] + T[2] * T[4] * T[15] - T[0] * T[6] * T[15]) / det);
			builder.setValue(1, 3, (T[2] * T[7] * T[8] - T[3] * T[6] * T[8] + T[3] * T[4] * T[10] - T[0] * T[7] * T[10]
					- T[2] * T[4] * T[11] + T[0] * T[6] * T[11]) / det);
			builder.setValue(2, 0, (T[5] * T[11] * T[12] - T[7] * T[9] * T[12] + T[7] * T[8] * T[13]
					- T[4] * T[11] * T[13] - T[5] * T[8] * T[15] + T[4] * T[9] * T[15]) / det);
			builder.setValue(2, 1, (T[3] * T[9] * T[12] - T[1] * T[11] * T[12] - T[3] * T[8] * T[13]
					+ T[0] * T[11] * T[13] + T[1] * T[8] * T[15] - T[0] * T[9] * T[15]) / det);
			builder.setValue(2, 2, (T[1] * T[7] * T[12] - T[3] * T[5] * T[12] + T[3] * T[4] * T[13]
					- T[0] * T[7] * T[13] - T[1] * T[4] * T[15] + T[0] * T[5] * T[15]) / det);
			builder.setValue(2, 3, (T[3] * T[5] * T[8] - T[1] * T[7] * T[8] - T[3] * T[4] * T[9] + T[0] * T[7] * T[9]
					+ T[1] * T[4] * T[11] - T[0] * T[5] * T[11]) / det);
			builder.setValue(3, 0, (T[6] * T[9] * T[12] - T[5] * T[10] * T[12] - T[6] * T[8] * T[13]
					+ T[4] * T[10] * T[13] + T[5] * T[8] * T[14] - T[4] * T[9] * T[14]) / det);
			builder.setValue(3, 1, (T[1] * T[10] * T[12] - T[2] * T[9] * T[12] + T[2] * T[8] * T[13]
					- T[0] * T[10] * T[13] - T[1] * T[8] * T[14] + T[0] * T[9] * T[14]) / det);
			builder.setValue(3, 2, (T[2] * T[5] * T[12] - T[1] * T[6] * T[12] - T[2] * T[4] * T[13]
					+ T[0] * T[6] * T[13] + T[1] * T[4] * T[14] - T[0] * T[5] * T[14]) / det);
			builder.setValue(3, 3, (T[1] * T[6] * T[8] - T[2] * T[5] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9]
					- T[1] * T[4] * T[10] + T[0] * T[5] * T[10]) / det);
		}
		return builder.build();
	}

	private static double computeDeterminant(TransformMatrix3D matrix) {
		double[] T = matrix.getValues();
		if (matrix.isAffine()) {
			return -T[2] * T[5] * T[8] + T[1] * T[6] * T[8] + T[2] * T[4] * T[9] - T[0] * T[6] * T[9]
					- T[1] * T[4] * T[10] + T[0] * T[5] * T[10];
		} else {
			return T[3] * T[6] * T[9] * T[12] - T[2] * T[7] * T[9] * T[12] - T[3] * T[5] * T[10] * T[12]
					+ T[1] * T[7] * T[10] * T[12] + T[2] * T[5] * T[11] * T[12] - T[1] * T[6] * T[11] * T[12]
					- T[3] * T[6] * T[8] * T[13] + T[2] * T[7] * T[8] * T[13] + T[3] * T[4] * T[10] * T[13]
					- T[0] * T[7] * T[10] * T[13] - T[2] * T[4] * T[11] * T[13] + T[0] * T[6] * T[11] * T[13]
					+ T[3] * T[5] * T[8] * T[14] - T[1] * T[7] * T[8] * T[14] - T[3] * T[4] * T[9] * T[14]
					+ T[0] * T[7] * T[9] * T[14] + T[1] * T[4] * T[11] * T[14] - T[0] * T[5] * T[11] * T[14]
					- T[2] * T[5] * T[8] * T[15] + T[1] * T[6] * T[8] * T[15] + T[2] * T[4] * T[9] * T[15]
					- T[0] * T[6] * T[9] * T[15] - T[1] * T[4] * T[10] * T[15] + T[0] * T[5] * T[10] * T[15];
		}
	}

	@SuppressWarnings("serial")
	public static class MatrixInversionException extends RuntimeException {

		public MatrixInversionException() {
			this("Cannot invert a singular matrix");
		}

		public MatrixInversionException(String message) {
			super(message);
		}

	}

}