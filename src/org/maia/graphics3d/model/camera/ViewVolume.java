package org.maia.graphics3d.model.camera;

import org.maia.graphics3d.transform.TransformMatrix;
import org.maia.graphics2d.geometry.Rectangle2D;

public interface ViewVolume {

	boolean isPerspectiveProjection();

	TransformMatrix getProjectionMatrix();

	double getViewAngleInDegrees();

	double getAspectRatio();

	double getViewPlaneZ();

	double getFarPlaneZ();

	Rectangle2D getViewPlaneRectangle();

}