package org.maia.graphics3d.model.object;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.ReusableObjectPack;
import org.maia.graphics3d.render.shading.FlatShadingModel;

/**
 * A <em>simple face</em> being a finite area in a plane enclosed by a convex polygon
 * 
 * <p>
 * A <em>simple face</em> is specified by a set of vertices, representing the vertices of a convex polygon which
 * encloses the face. For that matter it extends the <code>ConvexPolygonalObject3D</code> class. The traversal order of
 * the vertices is important. When perceived in clockwise direction the face is front-facing, in counter-clockwise
 * direction the face is back-facing.
 * </p>
 * <p>
 * A solid color applies to a <em>simple face</em>, although there can be a separate color for the front and back side
 * of the face. In addition, the color is subject to a <code>FlatShadingModel</code>.
 * </p>
 * 
 * @see FlatShadingModel
 */
public class SimpleFace3D extends ConvexPolygonalObject3D {

	private Color frontColor;

	private Color backColor;

	private FlatShadingModel shadingModel;

	public SimpleFace3D(Color color, FlatShadingModel shadingModel, Point3D... vertices) {
		this(color, color, shadingModel, vertices);
	}

	public SimpleFace3D(Color color, FlatShadingModel shadingModel, List<Point3D> vertices) {
		this(color, color, shadingModel, vertices);
	}

	public SimpleFace3D(Color frontColor, Color backColor, FlatShadingModel shadingModel, Point3D... vertices) {
		this(frontColor, backColor, shadingModel, Arrays.asList(vertices));
	}

	public SimpleFace3D(Color frontColor, Color backColor, FlatShadingModel shadingModel, List<Point3D> vertices) {
		super(vertices);
		this.frontColor = frontColor;
		this.backColor = backColor;
		this.shadingModel = shadingModel;
	}

	@Override
	protected Color sampleBaseColor(Point3D positionInCamera, Scene scene) {
		return isFrontFacingInCamera(scene) ? getFrontColor() : getBackColor();
	}

	protected boolean isFrontFacingInCamera(Scene scene) {
		return getPlaneInCameraCoordinates(scene.getCamera()).getNormalUnitVector().getZ() <= 0;
	}

	@Override
	protected void applySurfacePointShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects) {
		getShadingModel().applyShading(surfacePoint, scene, options, reusableObjects);
	}

	public Color getFrontColor() {
		return frontColor;
	}

	public Color getBackColor() {
		return backColor;
	}

	public FlatShadingModel getShadingModel() {
		return shadingModel;
	}

}