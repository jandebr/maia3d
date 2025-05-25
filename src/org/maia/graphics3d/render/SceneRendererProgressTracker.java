package org.maia.graphics3d.render;

import org.maia.graphics3d.model.scene.Scene;

public interface SceneRendererProgressTracker {

	void renderingStarted(SceneRenderer renderer, Scene scene);

	void renderingProgressUpdate(SceneRenderer renderer, Scene scene, int totalSteps, int stepIndex,
			double stepProgress, String stepLabel);

	void renderingCompleted(SceneRenderer renderer, Scene scene);

}
