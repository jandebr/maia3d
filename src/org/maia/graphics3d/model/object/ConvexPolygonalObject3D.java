package org.maia.graphics3d.model.object;

import java.util.List;

import org.maia.graphics2d.geometry.ConvexPolygon2D;
import org.maia.graphics2d.geometry.Point2D;
import org.maia.graphics2d.geometry.Polygon2D;
import org.maia.graphics3d.geometry.Point3D;

/**
 * An object in 3D space that has the geometrical shape of a convex polygon
 *
 * <p>
 * A polygon is made up of <em>n</em> vertices and <em>n</em> edges, connecting the vertices by line segments.
 * </p>
 * <p>
 * The following assumptions hold for a <code>ConvexPolygonalObject3D</code>
 * <ul>
 * <li>The vertices all lie in the same plane</li>
 * <li>The polygon is <em>convex</em>, meaning the angle between adjacent edges must be &lt;= 180 degrees. This implies
 * it is a <em>simple</em> polygon, which does not intersect itself and has no holes</li>
 * </ul>
 * </p>
 */
public class ConvexPolygonalObject3D extends PolygonalObject3D {

	public ConvexPolygonalObject3D(Point3D... vertices) {
		super(vertices);
	}

	public ConvexPolygonalObject3D(List<Point3D> vertices) {
		super(vertices);
	}

	@Override
	protected ProjectionState createProjectionState() {
		return new ConvexProjectionState();
	}

	private class ConvexProjectionState extends ProjectionState {

		public ConvexProjectionState() {
		}

		@Override
		protected Polygon2D derivePolygon(List<Point2D> vertices) {
			return new ConvexPolygon2D(vertices);
		}

	}

}