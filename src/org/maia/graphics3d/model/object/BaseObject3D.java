package org.maia.graphics3d.model.object;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.maia.graphics3d.Metrics3D;
import org.maia.graphics3d.geometry.Box3D;
import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.geometry.Vector3D;
import org.maia.graphics3d.model.CoordinateFrame;
import org.maia.graphics3d.model.camera.Camera;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.render.RenderOptions;
import org.maia.graphics3d.render.ReusableObjectPack;
import org.maia.graphics3d.transform.TransformMatrix;
import org.maia.graphics3d.transform.Transformation;
import org.maia.graphics3d.transform.TwoWayCompositeTransform;

public abstract class BaseObject3D
		implements BoundedObject3D, ComposableObject3D, TransformableObject3D, RaytraceableObject3D {

	private TwoWayCompositeTransform ownCompositeTransform; // own transform

	private TwoWayCompositeTransform selfToRootCompositeTransform; // cached transform from self to top-level composite

	private CompositeObject3D<BaseObject3D> compositeObject; // parent object, if any

	private Box3D boundingBoxInObjectCoordinates; // cached bounding box

	private Box3D boundingBoxInWorldCoordinates; // cached bounding box

	private Box3D boundingBoxInCameraCoordinates; // cached bounding box

	private Box3D boundingBoxInViewVolumeCoordinates; // cached bounding box

	protected BaseObject3D() {
		this.ownCompositeTransform = new TwoWayCompositeTransform();
	}

	@Override
	public final boolean isBounded() {
		return true;
	}

	@Override
	public final BoundedObject3D asBoundedObject() {
		return this;
	}

	@Override
	public final boolean isRaytraceable() {
		return true;
	}

	@Override
	public final RaytraceableObject3D asRaytraceableObject() {
		return this;
	}

	@Override
	public final boolean isTransformable() {
		return true;
	}

	@Override
	public final TransformableObject3D asTransformableObject() {
		return this;
	}

	@Override
	public final boolean isComposable() {
		return true;
	}

	@Override
	public final ComposableObject3D asComposableObject() {
		return this;
	}

	@Override
	public final boolean isMesh() {
		return this instanceof MeshObject3D;
	}

	@Override
	public final MeshObject3D asMeshObject() {
		if (isMesh()) {
			return (MeshObject3D) this;
		} else {
			throw new ClassCastException();
		}
	}

	@Override
	public BaseObject3D translateX(double distance) {
		return translate(distance, 0, 0);
	}

	@Override
	public BaseObject3D translateY(double distance) {
		return translate(0, distance, 0);
	}

	@Override
	public BaseObject3D translateZ(double distance) {
		return translate(0, 0, distance);
	}

	@Override
	public BaseObject3D translate(double dx, double dy, double dz) {
		return transform(Transformation.getTranslationMatrix(dx, dy, dz));
	}

	@Override
	public TransformableObject3D translate(Vector3D vector) {
		return translate(vector.getX(), vector.getY(), vector.getZ());
	}

	@Override
	public BaseObject3D scaleX(double scale) {
		return scale(scale, 1.0, 1.0);
	}

	@Override
	public BaseObject3D scaleY(double scale) {
		return scale(1.0, scale, 1.0);
	}

	@Override
	public BaseObject3D scaleZ(double scale) {
		return scale(1.0, 1.0, scale);
	}

	@Override
	public TransformableObject3D scale(double scale) {
		return scale(scale, scale, scale);
	}

	@Override
	public BaseObject3D scale(double sx, double sy, double sz) {
		return transform(Transformation.getScalingMatrix(sx, sy, sz));
	}

	@Override
	public BaseObject3D rotateX(double angleInRadians) {
		return transform(Transformation.getRotationXrollMatrix(angleInRadians));
	}

	@Override
	public BaseObject3D rotateY(double angleInRadians) {
		return transform(Transformation.getRotationYrollMatrix(angleInRadians));
	}

	@Override
	public BaseObject3D rotateZ(double angleInRadians) {
		return transform(Transformation.getRotationZrollMatrix(angleInRadians));
	}

	@Override
	public BaseObject3D transform(TransformMatrix matrix) {
		getOwnCompositeTransform().then(matrix);
		notifySelfHasTransformed();
		return this;
	}

	@Override
	public BaseObject3D undoLastTransform() {
		getOwnCompositeTransform().undo();
		notifySelfHasTransformed();
		return this;
	}

	@Override
	public TransformableObject3D undoTransformsFrom(int stepIndex) {
		getOwnCompositeTransform().undoFrom(stepIndex);
		notifySelfHasTransformed();
		return this;
	}

	@Override
	public TransformableObject3D replaceTransformAt(int stepIndex, TransformMatrix matrix) {
		getOwnCompositeTransform().replace(stepIndex, matrix);
		notifySelfHasTransformed();
		return this;
	}

	@Override
	public BaseObject3D resetTransforms() {
		getOwnCompositeTransform().reset();
		notifySelfHasTransformed();
		return this;
	}

	@Override
	public int getIndexOfCurrentTransformStep() {
		return getOwnCompositeTransform().getIndexOfCurrentStep();
	}

	protected TwoWayCompositeTransform getOwnCompositeTransform() {
		return ownCompositeTransform;
	}

	protected TwoWayCompositeTransform getSelfToRootCompositeTransform() {
		if (selfToRootCompositeTransform == null) {
			selfToRootCompositeTransform = deriveSelfToRootCompositeTransform();
		}
		return selfToRootCompositeTransform;
	}

	private TwoWayCompositeTransform deriveSelfToRootCompositeTransform() {
		List<BaseObject3D> ancestors = getAncestors();
		if (ancestors.isEmpty()) {
			return getOwnCompositeTransform();
		} else {
			TwoWayCompositeTransform ct = new TwoWayCompositeTransform();
			ct.then(getOwnCompositeTransform().getForwardCompositeMatrix());
			for (BaseObject3D ancestor : ancestors) {
				ct.then(ancestor.getOwnCompositeTransform().getForwardCompositeMatrix());
			}
			return ct;
		}
	}

	protected List<BaseObject3D> getAncestors() {
		List<BaseObject3D> ancestors = new Vector<BaseObject3D>();
		BaseObject3D current = this;
		while (current.getCompositeObject() != null) {
			BaseObject3D parent = (BaseObject3D) current.getCompositeObject();
			ancestors.add(parent);
			current = parent;
		}
		return ancestors;
	}

	@Override
	public void notifySelfHasTransformed() {
		invalidateSelfToRootCompositeTransform();
		invalidateWorldAndCameraBoundingBox();
	}

	@Override
	public void notifyAncestorHasTransformed() {
		invalidateSelfToRootCompositeTransform();
		invalidateWorldAndCameraBoundingBox();
	}

	@Override
	public void cameraHasChanged(Camera camera) {
		invalidateCameraBoundingBox();
	}

	private void invalidateSelfToRootCompositeTransform() {
		selfToRootCompositeTransform = null;
	}

	private void invalidateWorldAndCameraBoundingBox() {
		invalidateWorldBoundingBox();
		invalidateCameraBoundingBox();
	}

	private void invalidateWorldBoundingBox() {
		boundingBoxInWorldCoordinates = null;
	}

	private void invalidateCameraBoundingBox() {
		boundingBoxInCameraCoordinates = null;
		boundingBoxInViewVolumeCoordinates = null;
	}

	@Override
	public final Box3D getBoundingBox(CoordinateFrame cframe, Camera camera) {
		if (cframe.equals(CoordinateFrame.OBJECT)) {
			return getBoundingBoxInObjectCoordinates();
		} else if (cframe.equals(CoordinateFrame.WORLD)) {
			return getBoundingBoxInWorldCoordinates();
		} else if (cframe.equals(CoordinateFrame.CAMERA)) {
			return getBoundingBoxInCameraCoordinates(camera);
		} else if (cframe.equals(CoordinateFrame.VIEWVOLUME)) {
			return getBoundingBoxInViewVolumeCoordinates(camera);
		} else {
			return null;
		}
	}

	@Override
	public Box3D getBoundingBoxInObjectCoordinates() {
		if (boundingBoxInObjectCoordinates == null) {
			Metrics3D.getInstance().incrementBoundingBoxComputations();
			boundingBoxInObjectCoordinates = deriveBoundingBoxInObjectCoordinates();
		}
		return boundingBoxInObjectCoordinates;
	}

	@Override
	public Box3D getBoundingBoxInWorldCoordinates() {
		if (boundingBoxInWorldCoordinates == null) {
			Metrics3D.getInstance().incrementBoundingBoxComputations();
			boundingBoxInWorldCoordinates = deriveBoundingBoxInWorldCoordinates();
		}
		return boundingBoxInWorldCoordinates;
	}

	@Override
	public Box3D getBoundingBoxInCameraCoordinates(Camera camera) {
		if (boundingBoxInCameraCoordinates == null) {
			Metrics3D.getInstance().incrementBoundingBoxComputations();
			boundingBoxInCameraCoordinates = deriveBoundingBoxInCameraCoordinates(camera);
		}
		return boundingBoxInCameraCoordinates;
	}

	@Override
	public Box3D getBoundingBoxInViewVolumeCoordinates(Camera camera) {
		if (boundingBoxInViewVolumeCoordinates == null) {
			Metrics3D.getInstance().incrementBoundingBoxComputations();
			boundingBoxInViewVolumeCoordinates = deriveBoundingBoxInViewVolumeCoordinates(camera);
		}
		return boundingBoxInViewVolumeCoordinates;
	}

	protected abstract Box3D deriveBoundingBoxInObjectCoordinates();

	protected abstract Box3D deriveBoundingBoxInWorldCoordinates();

	protected abstract Box3D deriveBoundingBoxInCameraCoordinates(Camera camera);

	protected abstract Box3D deriveBoundingBoxInViewVolumeCoordinates(Camera camera);

	@SuppressWarnings("unchecked")
	@Override
	public CompositeObject3D<BaseObject3D> getCompositeObject() {
		return compositeObject;
	}

	protected void setCompositeObject(CompositeObject3D<BaseObject3D> compositeObject) {
		this.compositeObject = compositeObject;
	}

	@Override
	public void intersectWithEyeRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			RenderOptions options, ReusableObjectPack reusableObjects) {
		int n = intersections.size();
		intersectSelfWithRay(ray, scene, intersections, options, reusableObjects, true, true);
		Metrics3D.getInstance().incrementEyeRayWithObjectIntersectionChecks();
		if (intersections.size() > n) {
			Metrics3D.getInstance().incrementEyeRayWithObjectIntersections();
		}
	}

	@Override
	public void intersectWithLightRay(LineSegment3D ray, Scene scene, Collection<ObjectSurfacePoint3D> intersections,
			ReusableObjectPack reusableObjects) {
		int n = intersections.size();
		intersectSelfWithRay(ray, scene, intersections, null, reusableObjects, false, false);
		Metrics3D.getInstance().incrementLightRayWithObjectIntersectionChecks();
		if (intersections.size() > n) {
			Metrics3D.getInstance().incrementLightRayWithObjectIntersections();
		}
	}

	protected abstract void intersectSelfWithRay(LineSegment3D ray, Scene scene,
			Collection<ObjectSurfacePoint3D> intersections, RenderOptions options, ReusableObjectPack reusableObjects,
			boolean applyShading, boolean rayFromEye);

}