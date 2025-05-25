package org.maia.graphics3d.model;

public interface Memoise {

	/**
	 * Free up memory that is either unused or that can be transparently recomputed on-demand
	 */
	void releaseMemory();

}