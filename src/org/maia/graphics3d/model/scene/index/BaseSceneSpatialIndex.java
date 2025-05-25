package org.maia.graphics3d.model.scene.index;

import java.util.Collection;
import java.util.Vector;

import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.object.Object3D;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.model.scene.SceneUtils;

public abstract class BaseSceneSpatialIndex implements SceneSpatialIndex {

	private Scene scene;

	protected BaseSceneSpatialIndex(Scene scene) {
		this.scene = scene;
	}

	@Override
	public Scene getScene() {
		return scene;
	}

	protected Camera getCamera() {
		return getScene().getCamera();
	}

	protected Collection<Object3D> getIndexedObjects() {
		Collection<Object3D> sceneObjects = SceneUtils.getAllIndividualObjectsInScene(getScene());
		Collection<Object3D> indexedObjects = new Vector<Object3D>(sceneObjects.size());
		Box3D sceneBox = getSceneBox();
		for (Object3D object : sceneObjects) {
			boolean overlaps = true;
			if (object.isBounded()) {
				Box3D objectBox = getObjectBox(object);
				overlaps = objectBox != null && objectBox.overlaps(sceneBox);
			}
			if (overlaps) {
				indexedObjects.add(object);
			}
		}
		return indexedObjects;
	}

	protected Box3D getSceneBox() {
		return getScene().getBoundingBoxInCameraCoordinates();
	}

	protected Box3D getObjectBox(Object3D object) {
		Box3D box = null;
		if (object.isBounded()) {
			box = object.asBoundedObject().getBoundingBoxInCameraCoordinates(getCamera());
		}
		return box;
	}

}