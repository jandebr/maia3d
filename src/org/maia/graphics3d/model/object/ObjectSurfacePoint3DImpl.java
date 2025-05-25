package org.maia.graphics3d.model.object;

import java.awt.Color;

import org.maia.graphics3d.geometry.Point3D;

public class ObjectSurfacePoint3DImpl implements ObjectSurfacePoint3D {

	private Object3D object;

	private Point3D positionInCamera;

	private Color color;

	public ObjectSurfacePoint3DImpl(Object3D object, Point3D positionInCamera, Color color) {
		this.object = object;
		this.positionInCamera = positionInCamera;
		this.color = color;
	}

	@Override
	public Object3D getObject() {
		return object;
	}

	@Override
	public Point3D getPositionInCamera() {
		return positionInCamera;
	}

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}

}
