package org.maia.graphics3d.model.light;

import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.scene.Scene;

public class SpotLight extends BaseLight implements PositionalLightSource {

	private Point3D positionInWorld; // in world coordinates

	private boolean stationary;

	private Point3D positionInCamera;

	public SpotLight(Point3D positionInWorld) {
		this(positionInWorld, 1.0);
	}

	public SpotLight(Point3D positionInWorld, double brightness) {
		this(positionInWorld, brightness, false);
	}

	public SpotLight(Point3D positionInWorld, double brightness, boolean stationary) {
		super(brightness);
		this.positionInWorld = positionInWorld;
		this.stationary = stationary;
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		invalidatePositionInCamera();
	}

	@Override
	public Point3D getPositionInCamera(Scene scene) {
		if (positionInCamera == null) {
			positionInCamera = derivePositionInCamera(scene);
		}
		return positionInCamera;
	}

	private void invalidatePositionInCamera() {
		positionInCamera = null;
	}

	private Point3D derivePositionInCamera(Scene scene) {
		Point3D position = getPositionInWorld();
		if (!isStationary()) {
			position = scene.getCamera().getViewingMatrix().transform(position);
		}
		return position;
	}

	@Override
	public Point3D getPositionInWorld() {
		return positionInWorld;
	}

	@Override
	public boolean isStationary() {
		return stationary;
	}

	@Override
	public boolean isPositional() {
		return true;
	}

}