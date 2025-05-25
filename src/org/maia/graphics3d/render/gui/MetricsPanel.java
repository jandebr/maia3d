package org.maia.graphics3d.render.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.maia.graphics2d.Metrics2D;
import org.maia.graphics3d.Metrics3D;
import org.maia.graphics3d.model.scene.SceneUtils.ModelMetrics;

@SuppressWarnings("serial")
public class MetricsPanel extends JPanel {

	public MetricsPanel(ModelMetrics modelMetrics, Metrics2D metrics2d, Metrics3D metrics3d, long renderTimeMs) {
		buildUI(modelMetrics, metrics2d, metrics3d, renderTimeMs);
	}

	protected void buildUI(ModelMetrics modelMetrics, Metrics2D metrics2d, Metrics3D metrics3d, long renderTimeMs) {
		JTabbedPane tpane = new JTabbedPane();
		tpane.addTab(RenderUIResources.metricsModelTabTitle, buildModelMetricsPanel(modelMetrics));
		tpane.addTab(RenderUIResources.metricsComputeTabTitle,
				buildComputeMetricsPanel(metrics2d, metrics3d, renderTimeMs));
		add(tpane);
	}

	protected JComponent buildModelMetricsPanel(ModelMetrics modelMetrics) {
		JPanel panel = new JPanel(new GridLayout(0, 2, 16, 2));
		panel.add(buildMetricNameLabel("Vertices"));
		panel.add(buildMetricValueLabel(modelMetrics.getVertices()));
		panel.add(buildMetricNameLabel("Vertices (unique)"));
		panel.add(buildMetricValueLabel(modelMetrics.getUniqueVertices()));
		panel.add(buildMetricNameLabel("Edges"));
		panel.add(buildMetricValueLabel(modelMetrics.getEdges()));
		panel.add(buildMetricNameLabel("Edges (unique)"));
		panel.add(buildMetricValueLabel(modelMetrics.getUniqueEdges()));
		panel.add(buildMetricNameLabel("Faces"));
		panel.add(buildMetricValueLabel(modelMetrics.getFaces()));
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(panel, BorderLayout.NORTH);
		parent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		return parent;
	}

	protected JComponent buildComputeMetricsPanel(Metrics2D metrics2d, Metrics3D metrics3d, long renderTimeMs) {
		JPanel panel = new JPanel(new GridLayout(0, 2, 16, 2));
		panel.add(buildMetricNameLabel("Render time"));
		panel.add(buildMetricValueLabel(formatRenderTime(renderTimeMs)));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Matrix multiplications"));
		panel.add(buildMetricValueLabel(metrics3d.getMatrixMultiplications()));
		panel.add(buildMetricNameLabel("Matrix inversions"));
		panel.add(buildMetricValueLabel(metrics3d.getMatrixInversions()));
		panel.add(buildMetricNameLabel("Point transformations"));
		panel.add(buildMetricValueLabel(metrics3d.getPointTransformations()));
		panel.add(buildMetricNameLabel("Point normalizations"));
		panel.add(buildMetricValueLabel(metrics3d.getPointNormalizations()));
		panel.add(buildMetricNameLabel("Point inside polygon checks"));
		panel.add(buildMetricValueLabel(metrics3d.getPointInsidePolygonChecks()));
		panel.add(buildMetricNameLabel("Vector dot products"));
		panel.add(buildMetricValueLabel(metrics3d.getVectorDotProducts()));
		panel.add(buildMetricNameLabel("Vector cross products"));
		panel.add(buildMetricValueLabel(metrics3d.getVectorCrossProducts()));
		panel.add(buildMetricNameLabel("Vector normalizations"));
		panel.add(buildMetricValueLabel(metrics3d.getVectorNormalizations()));
		panel.add(buildMetricNameLabel("Vector angles"));
		panel.add(buildMetricValueLabel(metrics3d.getVectorAnglesInBetween()));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Line with line intersections"));
		panel.add(buildMetricValueLabel(metrics2d.getLineWithLineIntersections()));
		panel.add(buildMetricNameLabel("Line with plane intersections"));
		panel.add(buildMetricValueLabel(metrics3d.getLineWithPlaneIntersections()));
		panel.add(buildMetricNameLabel("Bounding box computations"));
		panel.add(buildMetricValueLabel(metrics3d.getBoundingBoxComputations()));
		addSpacer(panel);
		panel.add(buildMetricNameLabel("Eye ray object intersection checks"));
		panel.add(buildMetricValueLabel(metrics3d.getEyeRayWithObjectIntersectionChecks()));
		panel.add(buildMetricNameLabel("Eye ray object intersections"));
		panel.add(buildMetricValueLabel(metrics3d.getEyeRayWithObjectIntersections()));
		panel.add(buildMetricNameLabel("Point to light source traversals"));
		panel.add(buildMetricValueLabel(metrics3d.getSurfacePositionToLightSourceTraversals()));
		panel.add(buildMetricNameLabel("Light ray object intersection checks"));
		panel.add(buildMetricValueLabel(metrics3d.getLightRayWithObjectIntersectionChecks()));
		panel.add(buildMetricNameLabel("Light ray object intersections"));
		panel.add(buildMetricValueLabel(metrics3d.getLightRayWithObjectIntersections()));
		JPanel parent = new JPanel(new BorderLayout());
		parent.add(buildComputeMetricsDescription(), BorderLayout.NORTH);
		parent.add(panel, BorderLayout.CENTER);
		parent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		return parent;
	}

	protected void addSpacer(JPanel panel) {
		panel.add(Box.createGlue());
		panel.add(Box.createGlue());
	}

	protected JLabel buildComputeMetricsDescription() {
		JLabel label = new JLabel(RenderUIResources.metricsComputeDescription);
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
		return label;
	}

	protected JLabel buildMetricNameLabel(String name) {
		JLabel label = new JLabel(name);
		return label;
	}

	protected JLabel buildMetricValueLabel(long value) {
		JLabel label = buildMetricValueLabel(Metrics3D.format(value));
		if (value == 0) {
			label.setForeground(Color.GRAY);
		}
		return label;
	}

	protected JLabel buildMetricValueLabel(String value) {
		JLabel label = new JLabel(value);
		label.setForeground(new Color(194, 60, 8));
		return label;
	}

	private String formatRenderTime(long renderTimeMs) {
		long renderTimeSecs = renderTimeMs / 1000L;
		int hours = (int) (renderTimeSecs / 3600L);
		renderTimeSecs -= hours * 3600L;
		int minutes = (int) (renderTimeSecs / 60L);
		renderTimeSecs -= minutes * 60L;
		int seconds = (int) renderTimeSecs;
		int milliSeconds = (int) (renderTimeMs % 1000L);
		return formatTime(hours, minutes, seconds, milliSeconds);
	}

	private String formatTime(int hours, int minutes, int seconds, int milliSeconds) {
		StringBuilder sb = new StringBuilder();
		sb.append(formatWithMinimumDigits(hours, 2)).append(":").append(formatWithMinimumDigits(minutes, 2)).append(":")
				.append(formatWithMinimumDigits(seconds, 2)).append(".")
				.append(formatWithMinimumDigits(milliSeconds, 3));
		return sb.toString();
	}

	private String formatWithMinimumDigits(int n, int minDigits) {
		String str = String.valueOf(n);
		while (str.length() < minDigits) {
			str = "0" + str;
		}
		return str;
	}

}