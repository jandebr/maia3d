package org.maia.graphics3d.model.object;

import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.model.CoordinateFrame;
import org.maia.graphics3d.model.camera.Camera;

public interface BoundedObject3D extends Object3D {

	Box3D getBoundingBox(CoordinateFrame cframe, Camera camera);

	Box3D getBoundingBoxInObjectCoordinates();

	Box3D getBoundingBoxInWorldCoordinates();

	Box3D getBoundingBoxInCameraCoordinates(Camera camera);

	Box3D getBoundingBoxInViewVolumeCoordinates(Camera camera);

}