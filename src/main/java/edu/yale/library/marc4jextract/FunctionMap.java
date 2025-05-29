package edu.yale.library.marc4jextract;

import java.util.HashMap;
import java.util.Map;

public class FunctionMap {

    private static Map<String, ExtractorFunction> functionMap;
    static {
        functionMap = new HashMap<>();
        functionMap.put("sortTitleExtractor", new SortTitleExtractorFunction());
        functionMap.put("languageMap", new LanguageMapFunction());
        functionMap.put("dateExtractor", new DateExtractorFunction());
        functionMap.put("sortAuthorExtractor", new SortAuthorExtractorFunction());
        functionMap.put("subjectHeadingExtractor", new SubjectHeadingExtractorFunction());
    }

    public static ExtractorFunction get(String functionName) {
        return  functionMap.get(functionName);
    }
}
