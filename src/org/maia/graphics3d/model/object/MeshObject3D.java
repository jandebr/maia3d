package org.maia.graphics3d.model.object;

import org.maia.graphics3d.model.camera.Camera;

public interface MeshObject3D extends Object3D {

	Mesh3D getMeshInObjectCoordinates();

	Mesh3D getMeshInWorldCoordinates();

	Mesh3D getMeshInCameraCoordinates(Camera camera);

	Mesh3D getMeshInViewVolumeCoordinates(Camera camera);

}
