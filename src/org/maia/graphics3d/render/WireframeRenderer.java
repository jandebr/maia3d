package org.maia.graphics3d.render;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import org.maia.graphics2d.geometry.Rectangle2D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.object.Mesh3D;
import org.maia.graphics3d.model.object.Mesh3D.Edge;
import org.maia.graphics3d.model.object.MeshObject3D;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.model.scene.SceneUtils;
import org.maia.graphics3d.render.view.ViewPort;
import org.maia.util.ColorUtils;

public class WireframeRenderer extends BaseSceneRenderer {

	private static final String STEP_LABEL_RENDER = "Rendering wireframe";

	public WireframeRenderer() {
	}

	@Override
	protected void renderImpl(Scene scene, Collection<ViewPort> outputs, RenderOptions options) {
		Camera camera = scene.getCamera();
		Rectangle2D viewPlaneBounds = camera.getViewVolume().getViewPlaneRectangle();
		Collection<MeshObject3D> objects = SceneUtils.getAllMeshObjectsInScene(scene);
		int n = objects.size();
		int i = 0;
		for (MeshObject3D object : objects) {
			Mesh3D mesh = object.getMeshInViewVolumeCoordinates(camera);
			Mesh3D clippedMesh = clipMeshAgainstViewPlaneBounds(mesh, viewPlaneBounds);
			renderMesh(clippedMesh, outputs, options);
			double progress = ++i / (double) n;
			fireRenderingProgressUpdate(scene, 1, 0, progress, STEP_LABEL_RENDER);
		}
	}

	private Mesh3D clipMeshAgainstViewPlaneBounds(Mesh3D mesh, Rectangle2D viewPlaneBounds) {
		// TODO Clip against the canonical view volume
		return mesh;
	}

	private void renderMesh(Mesh3D mesh, Collection<ViewPort> outputs, RenderOptions options) {
		for (ViewPort output : outputs) {
			renderMesh(mesh, output, options);
		}
	}

	private void renderMesh(Mesh3D mesh, ViewPort output, RenderOptions options) {
		List<Point3D> vertices = mesh.getVertices();
		for (Edge edge : mesh.getEdges()) {
			Point3D p1 = vertices.get(edge.getFirstVertexIndex());
			Point3D p2 = vertices.get(edge.getSecondVertexIndex());
			renderEdge(p1, p2, output, options);
		}
	}

	protected void renderEdge(Point3D p1, Point3D p2, ViewPort output, RenderOptions options) {
		output.drawLineInViewCoordinates(p1.getX(), p1.getY(), p1.getZ(), getColorForVertex(p1, options), p2.getX(),
				p2.getY(), p2.getZ(), getColorForVertex(p2, options));
	}

	protected Color getColorForVertex(Point3D vertex, RenderOptions options) {
		double r = (vertex.getZ() + 1) / 2; // between 0 (near plane) and 1 (far plane)
		if (r < 0 || r > 1.0) {
			// Edge vertex lies outside view volume
			return Color.RED;
		} else {
			return ColorUtils.interpolate(options.getWireframeColorNear(), options.getWireframeColorFar(), (float) r);
		}
	}

}
