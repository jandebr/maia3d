package org.maia.graphics3d.render;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.maia.graphics2d.Metrics2D;
import org.maia.graphics2d.geometry.Rectangle2D;
import org.maia.graphics2d.image.ops.convolute.Convolution;
import org.maia.graphics2d.image.ops.convolute.ConvolutionMatrix;
import org.maia.graphics3d.Metrics3D;
import org.maia.graphics3d.geometry.LineSegment3D;
import org.maia.graphics3d.geometry.Point3D;
import org.maia.graphics3d.model.camera.ViewVolume;
import org.maia.graphics3d.model.object.Object3D;
import org.maia.graphics3d.model.object.ObjectSurfacePoint3D;
import org.maia.graphics3d.model.object.ObjectSurfacePoint3DImpl;
import org.maia.graphics3d.model.scene.Scene;
import org.maia.graphics3d.model.scene.SceneUtils;
import org.maia.graphics3d.model.scene.index.SceneViewPlaneIndex;
import org.maia.graphics3d.render.depth.DepthBlurOperation;
import org.maia.graphics3d.render.depth.DepthBlurOperation.DepthBlurOperationProgressTracker;
import org.maia.graphics3d.render.depth.DepthBlurParameters;
import org.maia.graphics3d.render.depth.DepthFunction;
import org.maia.graphics3d.render.view.ColorDepthBuffer;
import org.maia.graphics3d.render.view.ViewPort;
import org.maia.util.ColorUtils;

public class RaytraceRenderer extends BaseSceneRenderer {

	private static final String STEP_LABEL_INITIALIZE = "Initializing";

	private static final String STEP_LABEL_RAYTRACE = "Raytracing";

	private static final String STEP_LABEL_DEPTHBLUR_COMPUTE = "Computing depth blur";

	private static final String STEP_LABEL_DEPTHBLUR_RENDER = "Rendering depth blur";

	public RaytraceRenderer() {
	}

	@Override
	protected void renderImpl(Scene scene, Collection<ViewPort> outputs, RenderOptions options) {
		RenderState state = new RenderState(scene, options);
		renderInit(state);
		renderRaster(state, outputs);
		if (state.shouldApplyDepthBlur()) {
			applyDepthBlur(state, outputs);
		}
		System.out.println(Metrics2D.getInstance());
		System.out.println(Metrics3D.getInstance());
	}

	private void renderInit(RenderState state) {
		Scene scene = state.getScene();
		int steps = state.getTotalSteps();
		int step = state.getCurrentStep();
		fireRenderingProgressUpdate(scene, steps, step, 0.0, STEP_LABEL_INITIALIZE);
		scene.getSpatialIndex(); // create spatial index upfront (in single thread!)
		fireRenderingProgressUpdate(scene, steps, step, 0.5, STEP_LABEL_INITIALIZE);
		scene.getViewPlaneIndex(); // create view plane index upfront (in single thread!)
		fireRenderingProgressUpdate(scene, steps, step, 1.0, STEP_LABEL_INITIALIZE);
		System.out.println(state);
	}

	private void renderRaster(RenderState state, Collection<ViewPort> outputs) {
		state.incrementStep();
		int n = state.getOptions().getSafeNumberOfRenderThreads();
		if (n == 1) {
			renderRasterInCurrentThread(state, outputs);
		} else {
			renderRasterInSeparateThreads(state, outputs, n);
		}
	}

	private void renderRasterInCurrentThread(RenderState state, Collection<ViewPort> outputs) {
		state.setActiveRenderRasterWorkers(1);
		new RenderRasterWorker(state, outputs).run();
	}

	private synchronized void renderRasterInSeparateThreads(RenderState state, Collection<ViewPort> outputs,
			int numberOfThreads) {
		ThreadGroup workers = new ThreadGroup("Raytrace workers");
		System.out.println("Spawning " + numberOfThreads + " raytrace worker threads");
		for (int i = 0; i < numberOfThreads; i++) {
			Thread t = new Thread(workers, new RenderRasterWorker(state, outputs), "Raytrace worker #" + i);
			t.setDaemon(true);
			t.start();
			state.setActiveRenderRasterWorkers(state.getActiveRenderRasterWorkers() + 1);
		}
		while (state.getActiveRenderRasterWorkers() > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private synchronized void notifyRenderRasterWorkerCompletion(RenderRasterWorker worker) {
		RenderState state = worker.getState();
		state.setActiveRenderRasterWorkers(state.getActiveRenderRasterWorkers() - 1);
		notifyAll();
	}

	private void applyDepthBlur(RenderState state, Collection<ViewPort> outputs) {
		// Blur by depth
		state.incrementStep();
		ColorDepthBuffer raster = state.getRaster();
		int sppx = state.getSamplesPerPixelX();
		int sppy = state.getSamplesPerPixelY();
		DepthBlurParameters params = state.getScene().getDepthBlurParameters().clone();
		params.setMaxBlurPixelRadius(params.getMaxBlurPixelRadius() * Math.max(sppx, sppy)); // radius in samples
		raster.replaceImage(DepthBlurOperation.blurImageByDepth(raster, params, new DepthBlurTracker(state)));
		// Update outputs
		state.incrementStep();
		for (ViewPort output : outputs) {
			output.clear();
		}
		ConvolutionMatrix avgMatrix = state.getPixelAveragingConvolutionMatrix();
		int pw = state.getPixelWidth();
		int ph = state.getPixelHeight();
		for (int iy = 0; iy < ph; iy++) {
			for (int ix = 0; ix < pw; ix++) {
				if (state.getSamplesPerPixel() == 1) {
					renderPixelAtViewPorts(ix, iy, raster.getColor(ix, iy), outputs);
				} else {
					renderPixelAtViewPorts(ix, iy, raster.convoluteColor(ix * sppx, iy * sppy, avgMatrix), outputs);
				}
			}
			fireRenderingProgressUpdate(state.getScene(), state.getTotalSteps(), state.getCurrentStep(),
					(iy + 1.0) / ph, STEP_LABEL_DEPTHBLUR_RENDER);
		}
	}

	private void renderPixelAtViewPorts(int ix, int iy, Color color, Collection<ViewPort> outputs) {
		for (ViewPort output : outputs) {
			output.paintPixelInWindowCoordinates(ix, iy, color);
		}
	}

	private class RenderState {

		private Scene scene;

		private RenderOptions options;

		private Rectangle2D viewPlaneBounds;

		private double viewPlaneZ;

		private ColorDepthBuffer raster;

		private ConvolutionMatrix pixelAveragingConvolutionMatrix;

		private int currentStep;

		private int totalSteps;

		private int nextRenderLineIndex;

		private int activeRenderRasterWorkers;

		public RenderState(Scene scene, RenderOptions options) {
			ViewVolume vv = scene.getCamera().getViewVolume();
			this.scene = scene;
			this.options = options;
			this.viewPlaneBounds = vv.getViewPlaneRectangle();
			this.viewPlaneZ = vv.getViewPlaneZ();
			this.raster = new ColorDepthBuffer(getPixelWidth() * getSamplesPerPixelX(),
					getPixelHeight() * getSamplesPerPixelY(), options.getSceneBackgroundColor());
			this.pixelAveragingConvolutionMatrix = Convolution.getScaledGaussianBlurMatrix(getSamplesPerPixelY(),
					getSamplesPerPixelX(), 2.0);
			this.currentStep = 0;
			this.totalSteps = shouldApplyDepthBlur() ? 4 : 2;
			this.nextRenderLineIndex = 0;
			this.activeRenderRasterWorkers = 0;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("RenderState {\n");
			sb.append("\tObjects {\n");
			sb.append("\t\tTop level objects: ").append(getScene().getTopLevelObjects().size()).append("\n");
			sb.append("\t\tRaytraceable objects: ")
					.append(SceneUtils.getAllRaytraceableObjectsInScene(getScene()).size()).append("\n");
			sb.append("\t\t").append(getScene().getSpatialIndex().toString().replace("\n", "\n\t\t")).append("\n");
			sb.append("\t}\n");
			sb.append("\tCamera {\n");
			sb.append("\t\tPosition: ").append(getScene().getCamera().getPosition()).append("\n");
			sb.append("\t}\n");
			sb.append("\tView plane {\n");
			sb.append("\t\tXY bounds: ").append(getViewPlaneBounds()).append("\n");
			sb.append("\t\tZ: ").append(getViewPlaneZ()).append("\n");
			sb.append("\t\t").append(getViewPlaneIndex().toString().replace("\n", "\n\t\t")).append("\n");
			sb.append("\t}\n");
			sb.append("}");
			return sb.toString();
		}

		public void incrementStep() {
			currentStep++;
		}

		public synchronized boolean hasNextRenderLine() {
			return getNextRenderLineIndex() < getPixelHeight();
		}

		public synchronized int nextRenderLine() {
			if (!hasNextRenderLine())
				throw new NoSuchElementException("All lines were rendered");
			int index = getNextRenderLineIndex();
			setNextRenderLineIndex(index + 1);
			return index;
		}

		public int getPixelWidth() {
			return getOptions().getRenderWidth();
		}

		public int getPixelHeight() {
			return getOptions().getRenderHeight();
		}

		public int getSamplesPerPixelX() {
			return getOptions().getSamplingMode().getSamplesPerPixelX();
		}

		public int getSamplesPerPixelY() {
			return getOptions().getSamplingMode().getSamplesPerPixelY();
		}

		public int getSamplesPerPixel() {
			return getSamplesPerPixelX() * getSamplesPerPixelY();
		}

		public boolean shouldApplyDepthBlur() {
			return getOptions().isDepthBlurEnabled() && getScene().getDepthBlurParameters() != null;
		}

		public Scene getScene() {
			return scene;
		}

		public RenderOptions getOptions() {
			return options;
		}

		public Rectangle2D getViewPlaneBounds() {
			return viewPlaneBounds;
		}

		public double getViewPlaneZ() {
			return viewPlaneZ;
		}

		public ColorDepthBuffer getRaster() {
			return raster;
		}

		public ConvolutionMatrix getPixelAveragingConvolutionMatrix() {
			return pixelAveragingConvolutionMatrix;
		}

		private SceneViewPlaneIndex getViewPlaneIndex() {
			return getScene().getViewPlaneIndex();
		}

		public int getCurrentStep() {
			return currentStep;
		}

		public int getTotalSteps() {
			return totalSteps;
		}

		public double getRasterRenderProgress() {
			return getNextRenderLineIndex() / (double) getPixelHeight();
		}

		private int getNextRenderLineIndex() {
			return nextRenderLineIndex;
		}

		private void setNextRenderLineIndex(int nextRenderLineIndex) {
			this.nextRenderLineIndex = nextRenderLineIndex;
		}

		public int getActiveRenderRasterWorkers() {
			return activeRenderRasterWorkers;
		}

		public void setActiveRenderRasterWorkers(int activeRenderRasterWorkers) {
			this.activeRenderRasterWorkers = activeRenderRasterWorkers;
		}

	}

	private class RenderRasterWorker implements Runnable {

		private RenderState state;

		private ReusableObjectPack reusableObjects; // for use by this worker thread only (not thread-safe!)

		private Collection<ViewPort> outputs;

		private List<ObjectSurfacePoint3D> intersections; // reusable

		private List<Color> colorList; // reusable

		private Point3D pointOnViewPlane; // reusable

		private LineSegment3D ray; // reusable

		public RenderRasterWorker(RenderState state, Collection<ViewPort> outputs) {
			this.state = state;
			this.reusableObjects = new ReusableObjectPack();
			this.outputs = outputs;
			this.intersections = new Vector<ObjectSurfacePoint3D>();
			this.colorList = new Vector<Color>();
			this.pointOnViewPlane = new Point3D();
			this.ray = new LineSegment3D(this.pointOnViewPlane, new Point3D(), true, false);
		}

		@Override
		public void run() {
			RenderState state = getState();
			Point3D pointOnViewPlane = getPointOnViewPlane();
			pointOnViewPlane.setZ(state.getViewPlaneZ());
			int pw = state.getPixelWidth();
			int ph = state.getPixelHeight();
			int spp = state.getSamplesPerPixel();
			double vw = state.getViewPlaneBounds().getWidth();
			double vh = state.getViewPlaneBounds().getHeight();
			double vx0 = state.getViewPlaneBounds().getLeft();
			double vy0 = state.getViewPlaneBounds().getBottom();
			while (state.hasNextRenderLine()) {
				int iy = state.nextRenderLine();
				pointOnViewPlane.setY(vy0 + (ph - iy - 0.5) / ph * vh);
				for (int ix = 0; ix < pw; ix++) {
					pointOnViewPlane.setX(vx0 + (ix + 0.5) / pw * vw);
					if (spp == 1) {
						renderPixelWithoutSupersampling(ix, iy);
					} else {
						renderPixelBySupersampling(ix, iy);
					}
				}
				fireRenderingProgressUpdate(state.getScene(), state.getTotalSteps(), state.getCurrentStep(),
						state.getRasterRenderProgress(), STEP_LABEL_RAYTRACE);
			}
			notifyRenderRasterWorkerCompletion(this);
		}

		private void renderPixelWithoutSupersampling(int ix, int iy) {
			RenderState state = getState();
			ColorDepthBuffer raster = state.getRaster();
			LineSegment3D ray = getDirectedRay();
			Collection<ObjectSurfacePoint3D> intersections = getSceneIntersectionsWithRay(ray, ix, iy);
			if (!intersections.isEmpty()) {
				sortIntersectionsByDepth();
				raster.setColorAndDepth(ix, iy, getCombinedColor(), getNearestDepth());
			}
			renderPixelAtViewPorts(ix, iy, raster.getColor(ix, iy), getOutputs());
		}

		private void renderPixelBySupersampling(int ix, int iy) {
			RenderState state = getState();
			ColorDepthBuffer raster = state.getRaster();
			int sppx = state.getSamplesPerPixelX();
			int sppy = state.getSamplesPerPixelY();
			double pvw = state.getViewPlaneBounds().getWidth() / state.getPixelWidth(); // pixel view width
			double pvh = state.getViewPlaneBounds().getHeight() / state.getPixelHeight(); // pixel view height
			Point3D pointOnViewPlane = getPointOnViewPlane();
			double vx = pointOnViewPlane.getX();
			double vy = pointOnViewPlane.getY();
			double vx0 = vx - pvw / 2;
			double vy0 = vy + pvh / 2;
			for (int si = 0; si < sppy; si++) {
				int iry = iy * sppy + si;
				pointOnViewPlane.setY(vy0 - (si + 0.5) / sppy * pvh);
				for (int sj = 0; sj < sppx; sj++) {
					int irx = ix * sppx + sj;
					pointOnViewPlane.setX(vx0 + (sj + 0.5) / sppx * pvw);
					LineSegment3D ray = getDirectedRay();
					Collection<ObjectSurfacePoint3D> intersections = getSceneIntersectionsWithRay(ray, ix, iy);
					if (!intersections.isEmpty()) {
						sortIntersectionsByDepth();
						raster.setColorAndDepth(irx, iry, getCombinedColor(), getNearestDepth());
					}
				}
			}
			pointOnViewPlane.setX(vx);
			pointOnViewPlane.setY(vy);
			renderPixelAtViewPorts(ix, iy,
					raster.convoluteColor(ix * sppx, iy * sppy, state.getPixelAveragingConvolutionMatrix()),
					getOutputs());
		}

		private LineSegment3D getDirectedRay() {
			LineSegment3D ray = getRay();
			Point3D p1 = ray.getP1();
			Point3D p2 = ray.getP2();
			p2.setX(p1.getX() * 2.0);
			p2.setY(p1.getY() * 2.0);
			p2.setZ(p1.getZ() * 2.0);
			ray.invalidateDerivedProperties();
			return ray;
		}

		private Collection<ObjectSurfacePoint3D> getSceneIntersectionsWithRay(LineSegment3D ray, int ix, int iy) {
			Collection<ObjectSurfacePoint3D> intersections = getIntersections();
			intersections.clear();
			// From scene objects
			RenderState state = getState();
			RenderOptions options = state.getOptions();
			Scene scene = state.getScene();
			Point3D pointOnViewPlane = getPointOnViewPlane();
			ReusableObjectPack reusableObjects = getReusableObjects();
			Iterator<Object3D> objectsIterator = state.getViewPlaneIndex().getViewPlaneObjects(pointOnViewPlane,
					reusableObjects);
			while (objectsIterator.hasNext()) {
				Object3D object = objectsIterator.next();
				if (object.isRaytraceable()) {
					object.asRaytraceableObject().intersectWithEyeRay(ray, scene, intersections, options,
							reusableObjects);
				}
			}
			// From backdrop, if any
			ColorDepthBuffer backDrop = scene.getBackdrop();
			if (backDrop != null && options.isBackdropEnabled()) {
				Color color = backDrop.getColor(ix, iy);
				double depth = backDrop.getDepth(ix, iy);
				double z = -depth;
				double zf = z / pointOnViewPlane.getZ();
				if (zf >= 1.0) {
					double x = pointOnViewPlane.getX() * zf;
					double y = pointOnViewPlane.getY() * zf;
					intersections.add(new ObjectSurfacePoint3DImpl(null, new Point3D(x, y, z), color));
				}
			}
			return intersections;
		}

		private void sortIntersectionsByDepth() {
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (intersections.size() > 1) {
				Collections.sort(intersections, SurfacePointSorterByDepth.instance);
			}
		}

		private Color getCombinedColor() {
			Color color = null;
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (intersections.size() == 1) {
				color = intersections.get(0).getColor();
			} else if (intersections.size() > 1) {
				List<Color> colors = getColorList();
				colors.clear();
				for (ObjectSurfacePoint3D intersection : intersections) {
					colors.add(intersection.getColor());
				}
				color = ColorUtils.combineByTransparency(colors);
			}
			color = applyDarknessByDepth(color, getNearestDepth());
			return color;
		}

		private double getNearestDepth() {
			double depth = 0;
			List<ObjectSurfacePoint3D> intersections = getIntersections();
			if (!intersections.isEmpty()) {
				depth = -intersections.get(0).getPositionInCamera().getZ();
			}
			return depth;
		}

		private Color applyDarknessByDepth(Color color, double depth) {
			if (getState().getOptions().isDepthDarknessEnabled()) {
				DepthFunction df = getState().getScene().getDarknessDepthFunction();
				if (df != null) {
					double darkness = Math.max(Math.min(df.eval(depth), 1.0), 0);
					return ColorUtils.adjustBrightness(color, -darkness);
				}
			}
			return color;
		}

		private RenderState getState() {
			return state;
		}

		private ReusableObjectPack getReusableObjects() {
			return reusableObjects;
		}

		private Collection<ViewPort> getOutputs() {
			return outputs;
		}

		private List<ObjectSurfacePoint3D> getIntersections() {
			return intersections;
		}

		private List<Color> getColorList() {
			return colorList;
		}

		private Point3D getPointOnViewPlane() {
			return pointOnViewPlane;
		}

		private LineSegment3D getRay() {
			return ray;
		}

	}

	private static class SurfacePointSorterByDepth implements Comparator<ObjectSurfacePoint3D> {

		public static final SurfacePointSorterByDepth instance = new SurfacePointSorterByDepth();

		@Override
		public int compare(ObjectSurfacePoint3D sp1, ObjectSurfacePoint3D sp2) {
			double z1 = sp1.getPositionInCamera().getZ();
			double z2 = sp2.getPositionInCamera().getZ();
			if (z1 == z2) {
				return 0;
			} else if (z1 > z2) {
				return -1;
			} else {
				return 1;
			}
		}

	}

	private class DepthBlurTracker implements DepthBlurOperationProgressTracker {

		private RenderState state;

		public DepthBlurTracker(RenderState state) {
			this.state = state;
		}

		@Override
		public void operationStarted() {
		}

		@Override
		public void operationUpdate(double progress) {
			RenderState state = getState();
			fireRenderingProgressUpdate(state.getScene(), state.getTotalSteps(), state.getCurrentStep(), progress,
					STEP_LABEL_DEPTHBLUR_COMPUTE);
		}

		@Override
		public void operationCompleted() {
		}

		private RenderState getState() {
			return state;
		}

	}

}