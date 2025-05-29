package edu.yale.library.marc4jextract;

import java.util.regex.Pattern;

public class StringFormatter {

    private static Pattern trailingPunc = Pattern.compile(" *[,\\/;:] *\\Z");
    private static Pattern trailingPeriod = Pattern.compile("( *[\\p{IsAlphabetic},\\w]{3,})\\.\\Z");
    private static Pattern trailingSquareBracket = Pattern.compile("\\A\\[?([^\\[\\]]+)\\]?\\Z");

    // Trims punctuation, but leaves trailing period preceded by 2 or fewer letters.
    public static String trimPunctuation(String s) {
        if (s == null) return null;
        s = trailingPunc.matcher(s).replaceAll("");
        s = trailingPeriod.matcher(s).replaceAll("$1");
        s = trailingSquareBracket.matcher(s).replaceAll("$1");
        s = s.trim();
        if (s.equals(".")) s = ""; // remove trailing period, if that's all there is
        return s;
    }
}
