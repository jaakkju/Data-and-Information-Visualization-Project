/*
 *  Copyright 2013, Enguerrand de Rochefort modified by Juhani Jaakkola
 * 
 * This file is part of xdat.
 *
 * xdat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * xdat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with xdat.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package big.marketing.xdat;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Class that provides a static method to parse a string and return a double.
 * No constructor is needed because the class is only used in static context.
 */

public class NumberParser {
	static Logger logger = Logger.getLogger(NumberParser.class);

	/**
	 * Addresses issues with the standard parsing method of NumberFormat
	 * by verifying the parse position after parsing to ensure that all the input was parsed.
	 * 
	 * @param string the string to be parsed
	 * @return the double value
	 * @throws ParseException the parse exception
	 */
	public static float parseNumber(String string) throws ParseException {
		NumberFormat nf = NumberFormat.getInstance(Locale.US); // TODO Main.getUserPreferences().getLocale());
		ParsePosition parsePosition = new ParsePosition(0);
		Number number = nf.parse(string, parsePosition);
		if (number == null) {
			throw new ParseException("Failed to parse: " + string, 0);
		} else if (parsePosition.getIndex() < string.length()) {
			throw new ParseException("Could not parse whole string", parsePosition.getErrorIndex());
		} else if ((Pattern.matches(".*\\..{0,2}?\\..*", string) || Pattern.matches(".*,.{0,2}?,.*", string))) {
			logger.info(string + " parsed OK (not whole input) ParsePos:" + parsePosition.getIndex() + ", Parse Result: " + number);
			throw new ParseException(" Recognized wrong distance of digit grouping symbols ", parsePosition.getErrorIndex());
		} else {
			return number.floatValue();
		}
	}
}
