package org.maia.graphics3d.model.object;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.geometry.Polygon2D;
import org.maia.graphics3d.Metrics3D;
import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.geometry.Plane3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.geometry.Vector3D;
import org.maia.graphics3d.model.OrthographicProjection;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.object.Mesh3D.Edge;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.ReusableObjectPack;
import org.maia.util.ColorUtils;

/**
 * An object in 3D space that has the geometrical shape of a simple polygon
 *
 * <p>
 * A polygon is made up of <em>n</em> vertices and <em>n</em> edges, connecting the vertices by line segments.
 * </p>
 * <p>
 * The following assumptions hold for a <code>PolygonalObject3D</code>
 * <ul>
 * <li>The vertices all lie in the same plane</li>
 * <li>The polygon is <em>simple</em>, meaning it does not intersect itself and has no holes</li>
 * </ul>
 * </p>
 */
public class PolygonalObject3D extends VertexObject3D {

	private Plane3D planeCamera; // in camera coordinates

	private ProjectionState projectionState;

	private static Map<Integer, List<Edge>> reusableEdgesMap = new HashMap<Integer, List<Edge>>();

	private static final double APPROXIMATE_ZERO = 0.000001;

	public PolygonalObject3D(Point3D... vertices) {
		this(Arrays.asList(vertices));
	}

	public PolygonalObject3D(List<Point3D> vertices) {
		super(vertices, getPolygonEdges(vertices));
		this.projectionState = createProjectionState();
	}

	private static List<Edge> getPolygonEdges(List<Point3D> vertices) {
		Integer cacheKey = vertices.size();
		List<Edge> edges = reusableEdgesMap.get(cacheKey);
		if (edges == null) {
			edges = createPolygonEdges(vertices);
			reusableEdgesMap.put(cacheKey, edges);
		}
		return edges;
	}

	private static List<Edge> createPolygonEdges(List<Point3D> vertices) {
		int n = vertices.size();
		List<Edge> edges = new Vector<Edge>(n);
		for (int i = 0; i < n; i++) {
			int j = (i + 1) % n;
			edges.add(new Mesh3DImpl.EdgeImpl(i, j));
		}
		return edges;
	}

	protected ProjectionState createProjectionState() {
		return new ProjectionState();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("PolygonalObject3D {\n");
		for (Point3D vertex : getVerticesInObjectCoordinates()) {
			sb.append('\t').append(vertex).append('\n');
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	protected void intersectSelfWithRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			RenderOptions options, ReusableObjectPack reusableObjects, boolean applyShading, boolean rayFromEye) {
		ObjectSurfacePoint3D surfacePoint = findSurfacePointHitByRay(ray, scene, intersections, rayFromEye);
		if (surfacePoint != null) {
			colorSurfacePointHitByRay(surfacePoint, scene, options, reusableObjects, applyShading);
			if (surfacePoint.getColor() != null) {
				intersections.add(surfacePoint);
			}
		}
	}

	protected ObjectSurfacePoint3D findSurfacePointHitByRay(LineSegment3D ray, Scene scene,
			Collection<ObjectSurfacePoint3D> intersections, boolean rayFromEye) {
		ObjectSurfacePoint3D surfacePoint = null;
		Point3D positionInCamera = ray.intersect(getPlaneInCameraCoordinates(scene.getCamera()));
		if (positionInCamera != null) {
			// Early out (performance optimalization)
			boolean earlyOut = false;
			if (rayFromEye) {
				ObjectSurfacePoint3D nearestOpaque = getOpaqueIntersectionNearestToEye(intersections);
				earlyOut = nearestOpaque != null
						&& nearestOpaque.getPositionInCamera().getZ() > positionInCamera.getZ();
			}
			if (!earlyOut) {
				// Check insideness
				if (containsPointOnPlane(positionInCamera, scene)) {
					surfacePoint = new ObjectSurfacePoint3DImpl(this, positionInCamera, null);
				}
			}
		}
		return surfacePoint;
	}

	private ObjectSurfacePoint3D getOpaqueIntersectionNearestToEye(Collection<ObjectSurfacePoint3D> intersections) {
		ObjectSurfacePoint3D nearestOpaque = null;
		if (!intersections.isEmpty()) {
			double nearestDepth = 0;
			for (ObjectSurfacePoint3D intersection : intersections) {
				double depth = -intersection.getPositionInCamera().getZ();
				if ((nearestOpaque == null || depth < nearestDepth)
						&& ColorUtils.isFullyOpaque(intersection.getColor())) {
					nearestOpaque = intersection;
					nearestDepth = depth;
				}
			}
		}
		return nearestOpaque;
	}

	protected boolean containsPointOnPlane(Point3D positionInCamera, Scene scene) {
		boolean contains = false;
		if (insideBoundingBox(positionInCamera, scene)) {
			ProjectionState ps = getProjectionState();
			ps.setScene(scene);
			contains = ps.getPolygon().contains(ps.project(positionInCamera)); // inside-test with 2D-projected polygon
			Metrics3D.getInstance().incrementPointInsidePolygonChecks();
		}
		return contains;
	}

	private boolean insideBoundingBox(Point3D positionInCamera, Scene scene) {
		Box3D bbox = getBoundingBoxInCameraCoordinates(scene.getCamera());
		if (bbox.getWidth() <= APPROXIMATE_ZERO || bbox.getHeight() <= APPROXIMATE_ZERO
				|| bbox.getDepth() <= APPROXIMATE_ZERO) {
			// For planes perpendicular to a side of the view volume, finite precision computation requires a more
			// conservative bounding box insideness check
			double x1 = positionInCamera.getX() - APPROXIMATE_ZERO;
			double x2 = positionInCamera.getX() + APPROXIMATE_ZERO;
			double y1 = positionInCamera.getY() - APPROXIMATE_ZERO;
			double y2 = positionInCamera.getY() + APPROXIMATE_ZERO;
			double z1 = positionInCamera.getZ() - APPROXIMATE_ZERO;
			double z2 = positionInCamera.getZ() + APPROXIMATE_ZERO;
			return bbox.overlaps(new Box3D(x1, x2, y1, y2, z1, z2));
		}
		return bbox.contains(positionInCamera);
	}

	protected void colorSurfacePointHitByRay(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects, boolean applyShading) {
		Color color = sampleBaseColor(surfacePoint.getPositionInCamera(), scene);
		if (color != null) {
			surfacePoint.setColor(color);
			if (applyShading) {
				applySurfacePointShading(surfacePoint, scene, options, reusableObjects);
			}
		}
	}

	protected Color sampleBaseColor(Point3D positionInCamera, Scene scene) {
		return Color.BLACK; // Subclasses should override this
	}

	protected void applySurfacePointShading(ObjectSurfacePoint3D surfacePoint, Scene scene, RenderOptions options,
			ReusableObjectPack reusableObjects) {
		// Subclasses should override this
	}

	public Plane3D getPlaneInCameraCoordinates(Camera camera) {
		if (planeCamera == null) {
			planeCamera = derivePlaneInCameraCoordinates(camera);
		}
		return planeCamera;
	}

	private Plane3D derivePlaneInCameraCoordinates(Camera camera) {
		List<Point3D> vertices = getVerticesInCameraCoordinates(camera);
		return new Plane3D(vertices.get(0), vertices.get(1), vertices.get(2));
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidatePlane();
		invalidateProjectionState();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidatePlane();
		invalidateProjectionState();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidatePlane();
		invalidateProjectionState();
	}

	@Override
	public void releaseMemory() {
		super.releaseMemory();
		invalidatePlane();
		invalidateProjectionState();
	}

	private void invalidatePlane() {
		planeCamera = null;
	}

	private void invalidateProjectionState() {
		getProjectionState().invalidate();
	}

	private ProjectionState getProjectionState() {
		return projectionState;
	}

	protected class ProjectionState {

		private OrthographicProjection projection;

		private Polygon2D polygon;

		private Scene scene;

		public ProjectionState() {
			invalidate();
		}

		public void invalidate() {
			projection = null;
			polygon = null;
		}

		public Point2D project(Point3D point) {
			OrthographicProjection projection = getProjection();
			if (OrthographicProjection.ONTO_XY_PLANE.equals(projection)) {
				return new Point2D(point.getX(), point.getY());
			} else if (OrthographicProjection.ONTO_XZ_PLANE.equals(projection)) {
				return new Point2D(point.getX(), point.getZ());
			} else if (OrthographicProjection.ONTO_YZ_PLANE.equals(projection)) {
				return new Point2D(-point.getZ(), point.getY());
			}
			return null;
		}

		public List<Point2D> project(List<Point3D> points) {
			List<Point2D> projected = new Vector<Point2D>(points.size());
			for (Point3D point : points) {
				projected.add(project(point));
			}
			return projected;
		}

		public OrthographicProjection getProjection() {
			if (projection == null) {
				projection = deriveProjection();
			}
			return projection;
		}

		private OrthographicProjection deriveProjection() {
			Vector3D n = getPlaneInCameraCoordinates(getScene().getCamera()).getNormalUnitVector();
			if (Math.abs(n.getLatitudeInRadians()) >= Math.PI / 4)
				return OrthographicProjection.ONTO_XZ_PLANE;
			double lon = n.getLongitudeInRadians();
			if (Math.abs(lon - Math.PI / 2) <= Math.PI / 4)
				return OrthographicProjection.ONTO_XY_PLANE;
			if (Math.abs(lon - 1.5 * Math.PI) <= Math.PI / 4)
				return OrthographicProjection.ONTO_XY_PLANE;
			return OrthographicProjection.ONTO_YZ_PLANE;
		}

		public Polygon2D getPolygon() {
			if (polygon == null) {
				polygon = derivePolygon();
			}
			return polygon;
		}

		private Polygon2D derivePolygon() {
			List<Point2D> vertices = project(getVerticesInCameraCoordinates(getScene().getCamera()));
			return derivePolygon(vertices);
		}

		protected Polygon2D derivePolygon(List<Point2D> vertices) {
			return new Polygon2D(Polygon2D.deriveCentroid(vertices), vertices);
		}

		public Scene getScene() {
			return scene;
		}

		public void setScene(Scene scene) {
			if (this.scene != null && !this.scene.equals(scene)) {
				invalidate();
			}
			this.scene = scene;
		}

	}

}
