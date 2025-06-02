package org.maia.graphics3d.model.object;

import java.util.List;

import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.object.Mesh3D.Edge;
import org.maia.graphics3d.transform.TransformMatrix3D;
import org.maia.graphics3d.transform.Transformation3D;

public abstract class VertexObject3D extends BaseObject3D implements MeshObject3D {

	private Mesh3D meshInObjectCoordinates; // base mesh

	private Mesh3D meshInWorldCoordinates; // derived, cached mesh

	private Mesh3D meshInCameraCoordinates; // derived, cached mesh

	private Mesh3D meshInViewVolumeCoordinates; // derived, cached mesh

	protected VertexObject3D(List<Point3D> vertices, List<Edge> edges) {
		this.meshInObjectCoordinates = new Mesh3DImpl(vertices, edges);
	}

	@Override
	public final boolean isComposite() {
		return false;
	}

	@Override
	public final <T extends ComposableObject3D> CompositeObject3D<T> asCompositeObject() {
		throw new ClassCastException();
	}

	@Override
	protected Box3D deriveBoundingBoxInObjectCoordinates() {
		return deriveBoundingBox(getVerticesInObjectCoordinates());
	}

	@Override
	protected Box3D deriveBoundingBoxInWorldCoordinates() {
		return deriveBoundingBox(getVerticesInWorldCoordinates());
	}

	@Override
	protected Box3D deriveBoundingBoxInCameraCoordinates(Camera camera) {
		return deriveBoundingBox(getVerticesInCameraCoordinates(camera));
	}

	@Override
	protected Box3D deriveBoundingBoxInViewVolumeCoordinates(Camera camera) {
		return deriveBoundingBox(getVerticesInViewVolumeCoordinates(camera));
	}

	private Box3D deriveBoundingBox(List<Point3D> vertices) {
		Box3D bbox = null;
		if (!vertices.isEmpty()) {
			Point3D vertex = vertices.get(0);
			double x1 = vertex.getX();
			double x2 = x1;
			double y1 = vertex.getY();
			double y2 = y1;
			double z1 = vertex.getZ();
			double z2 = z1;
			for (int i = 1; i < vertices.size(); i++) {
				vertex = vertices.get(i);
				double x = vertex.getX();
				double y = vertex.getY();
				double z = vertex.getZ();
				x1 = Math.min(x1, x);
				x2 = Math.max(x2, x);
				y1 = Math.min(y1, y);
				y2 = Math.max(y2, y);
				z1 = Math.min(z1, z);
				z2 = Math.max(z2, z);
			}
			bbox = new Box3D(x1, x2, y1, y2, z1, z2);
		}
		return bbox;
	}

	public int getVertexCount() {
		return getVerticesInObjectCoordinates().size();
	}

	public List<Point3D> getVerticesInObjectCoordinates() {
		return getMeshInObjectCoordinates().getVertices();
	}

	public List<Point3D> getVerticesInWorldCoordinates() {
		return getMeshInWorldCoordinates().getVertices();
	}

	public List<Point3D> getVerticesInCameraCoordinates(Camera camera) {
		return getMeshInCameraCoordinates(camera).getVertices();
	}

	public List<Point3D> getVerticesInViewVolumeCoordinates(Camera camera) {
		return getMeshInViewVolumeCoordinates(camera).getVertices();
	}

	@Override
	public Mesh3D getMeshInObjectCoordinates() {
		return meshInObjectCoordinates;
	}

	@Override
	public Mesh3D getMeshInWorldCoordinates() {
		if (meshInWorldCoordinates == null) {
			meshInWorldCoordinates = deriveMeshInWorldCoordinates();
		}
		return meshInWorldCoordinates;
	}

	@Override
	public Mesh3D getMeshInCameraCoordinates(Camera camera) {
		if (meshInCameraCoordinates == null) {
			meshInCameraCoordinates = deriveMeshInCameraCoordinates(camera);
		}
		return meshInCameraCoordinates;
	}

	@Override
	public Mesh3D getMeshInViewVolumeCoordinates(Camera camera) {
		if (meshInViewVolumeCoordinates == null) {
			meshInViewVolumeCoordinates = deriveMeshInViewVolumeCoordinates(camera);
		}
		return meshInViewVolumeCoordinates;
	}

	private Mesh3D deriveMeshInWorldCoordinates() {
		return new Mesh3DImpl(deriveVerticesInWorldCoordinates(), getEdges());
	}

	private Mesh3D deriveMeshInCameraCoordinates(Camera camera) {
		return new Mesh3DImpl(deriveVerticesInCameraCoordinates(camera), getEdges());
	}

	private Mesh3D deriveMeshInViewVolumeCoordinates(Camera camera) {
		return new Mesh3DImpl(deriveVerticesInViewVolumeCoordinates(camera), getEdges());
	}

	private List<Point3D> deriveVerticesInWorldCoordinates() {
		return getSelfToRootCompositeTransform().getForwardCompositeMatrix()
				.transform(getVerticesInObjectCoordinates());
	}

	private List<Point3D> deriveVerticesInCameraCoordinates(Camera camera) {
		return camera.getViewingMatrix().transform(getVerticesInWorldCoordinates());
	}

	private List<Point3D> deriveVerticesInViewVolumeCoordinates(Camera camera) {
		TransformMatrix3D projectionMatrix = camera.getViewVolume().getProjectionMatrix();
		List<Point3D> projectedVertices = projectionMatrix.transform(getVerticesInCameraCoordinates(camera));
		if (camera.getViewVolume().isPerspectiveProjection()) {
			applyPerspectiveDivision(projectedVertices);
		}
		return projectedVertices;
	}

	private void applyPerspectiveDivision(List<Point3D> vertices) {
		for (Point3D vertex : vertices) {
			vertex.normalizeToUnitW();
		}
	}

	private List<Edge> getEdges() {
		return meshInObjectCoordinates.getEdges();
	}

	protected Point3D fromCameraToObjectCoordinates(Point3D point, Camera camera) {
		return fromWorldToObjectCoordinates(fromCameraToWorldCoordinates(point, camera));
	}

	protected Point3D fromCameraToWorldCoordinates(Point3D point, Camera camera) {
		return Transformation3D.getInverseMatrix(camera.getViewingMatrix()).transform(point);
	}

	protected Point3D fromWorldToObjectCoordinates(Point3D point) {
		return getSelfToRootCompositeTransform().reverseTransform(point);
	}

	@Override
	public void notifySelfHasTransformed() {
		super.notifySelfHasTransformed();
		invalidateWorldAndCameraMesh();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		super.notifyAncestorHasTransformed();
		invalidateWorldAndCameraMesh();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		super.cameraHasChanged(camera);
		invalidateCameraMesh();
	}

	@Override
	public void releaseMemory() {
		invalidateWorldAndCameraMesh();
	}

	private void invalidateWorldAndCameraMesh() {
		invalidateWorldMesh();
		invalidateCameraMesh();
	}

	private void invalidateWorldMesh() {
		meshInWorldCoordinates = null;
	}

	private void invalidateCameraMesh() {
		meshInCameraCoordinates = null;
		meshInViewVolumeCoordinates = null;
	}

}
