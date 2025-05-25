package org.maia.graphics3d.render;

import java.util.Collection;

import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.view.ViewPort;

public interface SceneRenderer {

	void render(Scene scene, ViewPort output, RenderOptions options);

	void render(Scene scene, Collection<ViewPort> outputs, RenderOptions options);

	void addProgressTracker(SceneRendererProgressTracker tracker);

	void removeProgressTracker(SceneRendererProgressTracker tracker);

}
