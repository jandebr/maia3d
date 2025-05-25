package org.maia.graphics3d.model.light;

import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.CoordinateFrame;
import org.maia.graphics3d.model.scene.Scene;

public interface PositionalLightSource extends LightSource {

	/**
	 * The position of this light source
	 * 
	 * @return The position, represented in the <em>world</em> coordinate frame
	 * @see CoordinateFrame#WORLD
	 */
	Point3D getPositionInWorld();

	/**
	 * Tells whether this light source is stationary in the camera view
	 * 
	 * @return <code>true</code> if the position or direction of this light source is fixed, i.e., does not move with
	 *         the camera, <code>false</code> otherwise
	 */
	boolean isStationary();

	/**
	 * Returns the position of this light source in camera coordinates
	 * 
	 * @param scene
	 *            The scene being viewed by its associated camera
	 * @return The position of this light source, in camera coordinates
	 * @see Scene#getCamera()
	 * @see #isStationary()
	 */
	Point3D getPositionInCamera(Scene scene);

}