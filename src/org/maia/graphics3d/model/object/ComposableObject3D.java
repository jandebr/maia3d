package org.maia.graphics3d.model.object;

public interface ComposableObject3D extends Object3D {

	<T extends ComposableObject3D> CompositeObject3D<T> getCompositeObject();

}
