package org.maia.graphics3d.transform;

/**
 * Represents a sequence of (inverse) transformations, intended for "reverse" application
 * 
 * @see CompositeTransform3D
 */
public class ReverseCompositeTransform3D extends CompositeTransform3D {

	public ReverseCompositeTransform3D() {
	}

	@Override
	protected TransformMatrix3D extendCompositeMatrix(TransformMatrix3D composite, TransformMatrix3D matrix) {
		return matrix.postMultiply(composite);
	}

}
