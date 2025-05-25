package org.maia.graphics3d.render;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.light.LightRaySegment;
import org.maia.graphics3d.model.object.Object3D;
import org.maia.graphics3d.model.object.ObjectSurfacePoint3D;
import org.maia.graphics3d.model.scene.index.NonUniformlyBinnedSceneSpatialIndex.ReusableBinNeighborsIterator;
import org.maia.graphics3d.model.scene.index.NonUniformlyBinnedSceneSpatialIndex.ReusableBinSideList;
import org.maia.graphics3d.model.scene.index.NonUniformlyBinnedSceneViewPlaneIndex.ReusableLastVisitedLeafBin;
import org.maia.graphics3d.render.shading.ObscuredObjectsCache;

/**
 * Collection of objects that can be reused exclusively in the context of the same thread
 * 
 * <p>
 * The purpose of this class is to improve processing speed by avoiding the creation of many short-lived objects, which
 * may in turn lead to costly garbage collections by the JVM. The code idiom is to create a new instance of
 * <code>ReusableObjectPack</code> within every <code>Thread</code> and to pass it along the method invocation chain to
 * where it is used.
 * </p>
 * <p>
 * Instances of this class as well as any of the contained objects are <em>not</em> thread-safe
 * </p>
 */
public class ReusableObjectPack {

	private List<ObjectSurfacePoint3D> intersectionsList;

	private Set<Object3D> objectsSet;

	private ReusableBinSideList binSidesList;

	private ReusableBinNeighborsIterator binNeighborsIterator;

	private ReusableLastVisitedLeafBin lastVisitedLeafBin;

	private LightRaySegment lightRay;

	private ObscuredObjectsCache obscuredObjectsCache;

	private Point3D pointInViewVolume;

	public ReusableObjectPack() {
		this.intersectionsList = new Vector<ObjectSurfacePoint3D>();
		this.objectsSet = new HashSet<Object3D>(300);
		this.binSidesList = new ReusableBinSideList();
		this.binNeighborsIterator = new ReusableBinNeighborsIterator();
		this.lastVisitedLeafBin = new ReusableLastVisitedLeafBin();
		this.lightRay = new LightRaySegment();
		this.obscuredObjectsCache = new ObscuredObjectsCache();
		this.pointInViewVolume = new Point3D();
	}

	public List<ObjectSurfacePoint3D> getEmptiedIntersectionsList() {
		List<ObjectSurfacePoint3D> list = getIntersectionsList();
		list.clear();
		return list;
	}

	public List<ObjectSurfacePoint3D> getIntersectionsList() {
		return intersectionsList;
	}

	public Set<Object3D> getEmptiedObjectsSet() {
		Set<Object3D> set = getObjectsSet();
		set.clear();
		return set;
	}

	public Set<Object3D> getObjectsSet() {
		return objectsSet;
	}

	public ReusableBinSideList getBinSidesList() {
		return binSidesList;
	}

	public ReusableBinNeighborsIterator getBinNeighborsIterator() {
		return binNeighborsIterator;
	}

	public ReusableLastVisitedLeafBin getLastVisitedLeafBin() {
		return lastVisitedLeafBin;
	}

	public LightRaySegment getLightRay() {
		return lightRay;
	}

	public ObscuredObjectsCache getObscuredObjectsCache() {
		return obscuredObjectsCache;
	}

	public Point3D getPointInViewVolume() {
		return pointInViewVolume;
	}

}