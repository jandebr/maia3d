package org.maia.graphics3d.model.object;

import java.util.Collection;

public interface CompositeObject3D<T extends ComposableObject3D> extends Object3D {

	Collection<T> getParts();

}
