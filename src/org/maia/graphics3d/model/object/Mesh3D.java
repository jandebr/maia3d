package org.maia.graphics3d.model.object;

import java.util.List;

import org.maia.graphics3d.geometry.Point3D;

public interface Mesh3D {

	List<Point3D> getVertices();

	List<Edge> getEdges();

	public static interface Edge {

		int getFirstVertexIndex();

		int getSecondVertexIndex();

	}

}