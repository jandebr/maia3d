package org.maia.graphics3d.render.gui;

import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.SceneRenderer;

public interface RenderKit {

	SceneRenderer createRenderer(Scene scene, RenderOptions options);

}