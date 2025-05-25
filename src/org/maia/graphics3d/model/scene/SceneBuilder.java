package org.maia.graphics3d.model.scene;

import java.util.Collection;

import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.camera.PerspectiveViewVolume;
import org.maia.graphics3d.model.camera.RevolvingCamera;
import org.maia.graphics3d.model.camera.RevolvingCameraImpl;
import org.maia.graphics3d.model.camera.ViewVolume;
import org.maia.graphics3d.model.light.LightSource;
import org.maia.graphics3d.model.object.Object3D;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.depth.DepthBlurParameters;
import org.maia.graphics3d.render.depth.DepthFunction;
import org.maia.graphics3d.render.depth.SigmoidDepthFunction;
import org.maia.graphics3d.render.view.ColorDepthBuffer;

public abstract class SceneBuilder {

	protected SceneBuilder() {
	}

	public RenderOptions getDefaultRenderOptions() {
		return RenderOptions.createDefaultOptions();
	}

	public Scene build(RenderOptions options) {
		Scene scene = createEmptyScene(options);
		for (Object3D object : createTopLevelObjects(options)) {
			scene.addTopLevelObject(object);
		}
		for (LightSource light : createLightSources(scene, options)) {
			scene.addLightSource(light);
		}
		scene.setBackdrop(createBackdrop(scene, options));
		scene.setDarknessDepthFunction(createDarknessDepthFunction(scene, options));
		scene.setDepthBlurParameters(createDepthBlurParameters(scene, options));
		return scene;
	}

	protected Scene createEmptyScene(RenderOptions options) {
		return new Scene(getSceneName(), createCamera(options));
	}

	protected abstract String getSceneName();

	protected abstract Camera createCamera(RenderOptions options);

	protected abstract Collection<Object3D> createTopLevelObjects(RenderOptions options);

	protected abstract Collection<LightSource> createLightSources(Scene scene, RenderOptions options);

	protected ColorDepthBuffer createBackdrop(Scene scene, RenderOptions options) {
		return null;
	}

	protected DepthFunction createDarknessDepthFunction(Scene scene, RenderOptions options) {
		Box3D bbox = scene.getBoundingBoxInCameraCoordinates();
		double nearDepth = -bbox.getZ2();
		double farDepth = -bbox.getZ1();
		return SigmoidDepthFunction.createFilter(nearDepth, farDepth, 0.6, 0.4);
	}

	protected DepthBlurParameters createDepthBlurParameters(Scene scene, RenderOptions options) {
		return new DepthBlurParameters(0.6, 1.0);
	}

	protected RevolvingCamera createRevolvingCamera(Point3D pivotPoint, Point3D position, double viewAngleInDegrees,
			double aspectRatio, double viewPlaneNegativeZ, double farPlaneNegativeZ) {
		ViewVolume viewVolume = PerspectiveViewVolume.createFromParameters(viewAngleInDegrees, aspectRatio,
				viewPlaneNegativeZ, farPlaneNegativeZ);
		return new RevolvingCameraImpl(pivotPoint, position, viewVolume);
	}

}