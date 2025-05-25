package org.maia.graphics3d.model.object;

import java.util.Collection;

import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.ReusableObjectPack;

public interface RaytraceableObject3D extends Object3D {

	void intersectWithEyeRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			RenderOptions options, ReusableObjectPack reusableObjects);

	void intersectWithLightRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			ReusableObjectPack reusableObjects);

}