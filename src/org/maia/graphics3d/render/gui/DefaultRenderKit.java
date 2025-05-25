package org.maia.graphics3d.render.gui;

import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RaytraceRenderer;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.RenderOptions.RenderMode;
import org.maia.graphics3d.render.SceneRenderer;
import org.maia.graphics3d.render.WireframeRenderer;

public class DefaultRenderKit implements RenderKit {

	public DefaultRenderKit() {
	}

	@Override
	public SceneRenderer createRenderer(Scene scene, RenderOptions options) {
		SceneRenderer renderer = null;
		if (RenderMode.PROTOTYPE.equals(options.getRenderMode())) {
			renderer = createPrototypeSceneRenderer(scene, options);
		} else if (RenderMode.REALISTIC.equals(options.getRenderMode())) {
			renderer = createRealisticSceneRenderer(scene, options);
		}
		return renderer;
	}

	protected SceneRenderer createPrototypeSceneRenderer(Scene scene, RenderOptions options) {
		return new WireframeRenderer();
	}

	protected SceneRenderer createRealisticSceneRenderer(Scene scene, RenderOptions options) {
		return new RaytraceRenderer();
	}

}