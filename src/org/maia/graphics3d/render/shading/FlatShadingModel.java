package org.maia.graphics3d.render.shading;

import org.maia.graphics3d.model.object.ObjectSurfacePoint3D;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.ReusableObjectPack;

public interface FlatShadingModel extends ShadingModel {

	void applyShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects);

}