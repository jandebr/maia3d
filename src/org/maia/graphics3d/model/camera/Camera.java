package org.maia.graphics3d.model.camera;

import org.maia.graphics3d.transform.TransformMatrix3D;
import org.maia.graphics3d.geometry.Point3D;

public interface Camera {

	TransformMatrix3D getViewingMatrix();

	ViewVolume getViewVolume();

	Point3D getPosition();

	void addObserver(CameraObserver observer);

	void removeObserver(CameraObserver observer);

}