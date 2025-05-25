package org.maia.graphics3d.model.scene;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.maia.graphics2d.geometry.Radians;
import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.MovableCamera;
import org.maia.graphics3d.model.light.DirectionalLightSource;
import org.maia.graphics3d.model.light.LightSource;
import org.maia.graphics3d.model.light.PositionalLightSource;
import org.maia.graphics3d.model.object.Mesh3D;
import org.maia.graphics3d.model.object.Mesh3D.Edge;
import org.maia.graphics3d.model.object.MeshObject3D;
import org.maia.graphics3d.model.object.Object3D;
import org.maia.graphics3d.model.object.PolygonalObject3D;
import org.maia.graphics3d.model.object.RaytraceableObject3D;

public class SceneUtils {

	public static Collection<Object3D> getAllIndividualObjectsInScene(Scene scene) {
		Collection<Object3D> objects = new Vector<Object3D>(1000);
		for (Object3D object : scene.getTopLevelObjects()) {
			collectAllIndividualObjects(object, objects);
		}
		return objects;
	}

	private static void collectAllIndividualObjects(Object3D current, Collection<Object3D> collection) {
		if (current.isComposite()) {
			for (Object3D part : current.asCompositeObject().getParts()) {
				collectAllIndividualObjects(part, collection);
			}
		} else {
			collection.add(current);
		}
	}

	public static Collection<MeshObject3D> getAllMeshObjectsInScene(Scene scene) {
		Collection<MeshObject3D> objects = new Vector<MeshObject3D>(1000);
		for (Object3D object : scene.getTopLevelObjects()) {
			collectAllMeshObjects(object, objects);
		}
		return objects;
	}

	private static void collectAllMeshObjects(Object3D current, Collection<MeshObject3D> collection) {
		if (current.isMesh()) {
			collection.add(current.asMeshObject());
		}
		if (current.isComposite()) {
			for (Object3D part : current.asCompositeObject().getParts()) {
				collectAllMeshObjects(part, collection);
			}
		}
	}

	public static Collection<RaytraceableObject3D> getAllRaytraceableObjectsInScene(Scene scene) {
		Collection<RaytraceableObject3D> objects = new Vector<RaytraceableObject3D>(1000);
		for (Object3D object : scene.getTopLevelObjects()) {
			collectAllRaytraceableObjects(object, objects);
		}
		return objects;
	}

	private static void collectAllRaytraceableObjects(Object3D current, Collection<RaytraceableObject3D> collection) {
		if (current.isComposite()) {
			for (Object3D part : current.asCompositeObject().getParts()) {
				collectAllRaytraceableObjects(part, collection);
			}
		} else if (current.isRaytraceable()) {
			collection.add(current.asRaytraceableObject());
		}
	}

	public static Collection<PositionalLightSource> getAllPositionalLightSourcesInScene(Scene scene) {
		Collection<PositionalLightSource> lightSources = new Vector<PositionalLightSource>();
		for (LightSource lightSource : scene.getLightSources()) {
			if (lightSource.isPositional()) {
				lightSources.add((PositionalLightSource) lightSource);
			}
		}
		return lightSources;
	}

	public static Collection<DirectionalLightSource> getAllDirectionalLightSourcesInScene(Scene scene) {
		Collection<DirectionalLightSource> lightSources = new Vector<DirectionalLightSource>();
		for (LightSource lightSource : scene.getLightSources()) {
			if (lightSource.isDirectional()) {
				lightSources.add((DirectionalLightSource) lightSource);
			}
		}
		return lightSources;
	}

	public static void moveCameraToEncloseScene(Scene scene, double viewAngleInDegrees, double aspectRatio) {
		if (scene.getCamera() instanceof MovableCamera) {
			MovableCamera camera = (MovableCamera) scene.getCamera();
			Box3D box = scene.getBoundingBoxInCameraCoordinates();
			Point3D boxc = box.getCenter();
			double top = Math.max(Math.abs(box.getY1()), Math.abs(box.getY2()));
			double right = Math.max(Math.abs(box.getX1()), Math.abs(box.getX2()));
			if (right / top > aspectRatio) {
				top = right / aspectRatio;
			}
			double du = boxc.getX();
			double dv = boxc.getY();
			double dn = box.getZ2() + top / Math.tan(Radians.degreesToRadians(viewAngleInDegrees) / 2);
			camera.slide(du, dv, dn);
		} else {
			throw new UnsupportedOperationException("The scene's camera may not support this operation");
		}
	}

	public static ModelMetrics getModelMetrics(Scene scene) {
		ModelMetrics metrics = new ModelMetrics();
		Set<Point3D> uniqueVertices = new HashSet<Point3D>(1000);
		Set<LineSegment3D> uniqueEdges = new HashSet<LineSegment3D>(1000);
		for (Object3D object : scene.getTopLevelObjects()) {
			countModelMetrics(object, metrics, uniqueVertices, uniqueEdges);
		}
		metrics.setUniqueVertices(uniqueVertices.size());
		metrics.setUniqueEdges(uniqueEdges.size());
		return metrics;
	}

	private static void countModelMetrics(Object3D current, ModelMetrics metrics, Set<Point3D> uniqueVertices,
			Set<LineSegment3D> uniqueEdges) {
		if (current.isComposite()) {
			for (Object3D part : current.asCompositeObject().getParts()) {
				countModelMetrics(part, metrics, uniqueVertices, uniqueEdges);
			}
		} else {
			if (current.isMesh()) {
				Mesh3D mesh = current.asMeshObject().getMeshInWorldCoordinates();
				metrics.setVertices(metrics.getVertices() + mesh.getVertices().size());
				metrics.setEdges(metrics.getEdges() + mesh.getEdges().size());
				uniqueVertices.addAll(mesh.getVertices());
				for (Edge edge : mesh.getEdges()) {
					Point3D p1 = mesh.getVertices().get(edge.getFirstVertexIndex());
					Point3D p2 = mesh.getVertices().get(edge.getSecondVertexIndex());
					uniqueEdges.add(new LineSegment3D(p1, p2));
				}
			}
			if (current instanceof PolygonalObject3D) {
				metrics.setFaces(metrics.getFaces() + 1);
			}
		}
	}

	public static class ModelMetrics {

		private long vertices;

		private long edges;

		private long faces;

		private long uniqueVertices;

		private long uniqueEdges;

		public ModelMetrics() {
		}

		public long getVertices() {
			return vertices;
		}

		public void setVertices(long vertices) {
			this.vertices = vertices;
		}

		public long getEdges() {
			return edges;
		}

		public void setEdges(long edges) {
			this.edges = edges;
		}

		public long getFaces() {
			return faces;
		}

		public void setFaces(long faces) {
			this.faces = faces;
		}

		public long getUniqueVertices() {
			return uniqueVertices;
		}

		public void setUniqueVertices(long uniqueVertices) {
			this.uniqueVertices = uniqueVertices;
		}

		public long getUniqueEdges() {
			return uniqueEdges;
		}

		public void setUniqueEdges(long uniqueEdges) {
			this.uniqueEdges = uniqueEdges;
		}

	}

}