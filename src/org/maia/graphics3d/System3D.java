package org.maia.graphics3d;

import org.maia.graphics3d.model.scene.Scene;
import org.maia.util.SystemUtils;

public class System3D {

	private System3D() {
	}

	public static void releaseMemoryAfterRendering(Scene scene) {
		SystemUtils.releaseMemory();
	}

	public static void releaseMemory(Scene scene) {
		scene.releaseMemory();
		SystemUtils.releaseMemory();
	}

}