/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.maastro.eureca.aida.indexer.concurrencyTestUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author kasper
 */
public class DurationMeasurement {
	private final AtomicLong startTime = new AtomicLong();
	private final AtomicLong endTime = new AtomicLong();

	public void start() {
		startTime.set(System.nanoTime());
	}

	public void stop() {
		endTime.set(System.nanoTime());
	}

	public long getDuration(TimeUnit unit) throws IllegalStateException {
		if(startTime.get() == 0 || endTime.get() == 0) {
			throw new IllegalStateException("Call start() and stop() before calling duration");
		}
		long nsResult = endTime.get() - startTime.get();
		return unit.convert(nsResult, TimeUnit.NANOSECONDS);
	}
			
	@Override
	public String toString() {
		try {
			long duration = getDuration(TimeUnit.NANOSECONDS);
			double durationInSec = duration / 1.0E-9;
			return Double.toString(durationInSec) +"s";
		} catch(IllegalStateException ex) {
			return super.toString();
		}
	}
}
