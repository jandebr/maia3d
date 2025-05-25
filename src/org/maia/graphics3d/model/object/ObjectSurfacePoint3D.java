package org.maia.graphics3d.model.object;

import java.awt.Color;

import org.maia.graphics3d.geometry.Point3D;

public interface ObjectSurfacePoint3D {

	Object3D getObject();

	Point3D getPositionInCamera();

	Color getColor();

	void setColor(Color color);

}
