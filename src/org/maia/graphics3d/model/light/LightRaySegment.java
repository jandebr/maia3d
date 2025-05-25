package org.maia.graphics3d.model.light;

import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.geometry.Point3D;

public class LightRaySegment extends LineSegment3D {

	private LightSource lightSource;

	public LightRaySegment() {
		super(Point3D.origin(), Point3D.origin());
	}

	public LightSource getLightSource() {
		return lightSource;
	}

	public void setLightSource(LightSource lightSource) {
		this.lightSource = lightSource;
	}

}