// © Maastro Clinic, 2013
package nl.maastro.eureca.aida.search.zylabpatisclient.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Produce a String of digits to easily find a charater at a certain position (in Test/Debug output).
 *
 * @author Kasper van den Berg <kasper.vandenberg@maastro.nl> <kasper@kaspervandenberg.net>
 */
public abstract class CharacterPositionRuler {
	/**
	 * Appends a digit for each call to {@link appendDigit()}.
	 */
	private static class RulerLine extends CharacterPositionRuler {
		private final StringBuilder target;
		private final int radix;

		public RulerLine(StringBuilder target_, int radix_) {
			this.target = target_;
			this.radix = radix_;
		}

		public RulerLine(int radix_) {
			this(new StringBuilder(), radix_);
		}

		@Override
		protected void appendDigit(int position) {
			int digit = (position / radix) % 10;
			String str_digit = String.format("%1d", digit);
			getTarget().append(str_digit);
		}

		@Override
		protected StringBuilder getTarget() {
			return target;
		}

		@Override
		protected String getRuler() {
			return getTarget().toString();
		}
	}	

	/**
	 * Appends a skip character of calls its delegate to append a character for each call to {@link appendDigit()}
	 */
	private static class SkippingRuler extends CharacterPositionRuler {
		private final CharacterPositionRuler delegate;
		private final int delegateMultiple;
		private final char skipChar;

		public SkippingRuler(CharacterPositionRuler delegate_, int delegateMultiple_, char skipChar_) {
			this.delegate = delegate_;
			this.delegateMultiple = delegateMultiple_;
			this.skipChar = skipChar_;
		}

		protected void appendDigit(int position) {
			boolean isDividable = (position % delegateMultiple) == 0;
			if(isDividable) {
				delegate.appendDigit(position);
			} else {
				getTarget().append(skipChar);
			}
		}

		@Override
		protected StringBuilder getTarget() {
			return delegate.getTarget();
		}

		@Override
		protected String getRuler() {
			return delegate.getRuler();
		}
	}

	/**
	 * Produces a multi line ruler composed of other rulers.
	 */
	private static class MultiLineRuler extends CharacterPositionRuler {
		private final List<CharacterPositionRuler> subrulers;
		private final StringBuilder target;
		
		public MultiLineRuler(List<CharacterPositionRuler> subrulers_) {
			this.subrulers = subrulers_;
			this.target = new StringBuilder();
		}

		public MultiLineRuler(CharacterPositionRuler... subrulers_) {
			this(Arrays.asList(subrulers_));
		}

		@Override
		protected void appendDigit(int position) {
			for (CharacterPositionRuler subruler : subrulers) {
				subruler.appendDigit(position);
			}
		}

		@Override
		protected StringBuilder getTarget() {
			return target;
		}

		@Override
		protected String getRuler() {
			for (CharacterPositionRuler ruler : subrulers) {
				getTarget().append(ruler.getTarget());
				getTarget().append("\n");
			}
			return getTarget().toString();
		}
	}

	public static String createDecimalRuler(int length) {
		CharacterPositionRuler ruler = buildDecRuler(length);
		return ruler.getRuler(length);
	}

	private static MultiLineRuler buildDecRuler(int length) {
		int nLines = Double.valueOf(Math.ceil(Math.log10(length))).intValue();
		List<CharacterPositionRuler> lines = new ArrayList<>(nLines);
		
		RulerLine unit = new RulerLine(1);
		SkippingRuler unitSkipping = new SkippingRuler(unit, 2, '.');
		lines.add(unitSkipping);

		for(int factor = 10; factor < length; factor *= 10) {
			RulerLine line = new RulerLine(factor);
			SkippingRuler skipping = new SkippingRuler(line, factor, ' ');
			lines.add(skipping);
		}
		
		return new MultiLineRuler(lines);
	}
	
	protected abstract void appendDigit(int position);
	protected abstract StringBuilder getTarget();
	protected abstract String getRuler();

	private String getRuler(int length) {
		for (int i = 1; i < length; i++) {
			appendDigit(i);
		}
		return getRuler();
	}


}
