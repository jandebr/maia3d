package org.maia.graphics3d.geometry;

import org.maia.graphics3d.transform.TransformMatrix3D;

public interface Curve3D {

	/**
	 * Samples this curve along its path
	 * 
	 * @param t
	 *            The relative distance along the path, with <code>t in [0,1]</code>
	 * @return A point along the curve's path
	 */
	Point3D sample(double t);

	/**
	 * Transforms this curve into another curve
	 * 
	 * @param matrix
	 *            The transformation matrix
	 * @return The transformed curve
	 * @throws UnsupportedOperationException
	 *             If this curve does not support transformations
	 */
	Curve3D transform(TransformMatrix3D matrix);

}