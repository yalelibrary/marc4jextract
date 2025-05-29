package edu.yale.library.marc4jextract;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Array;
import java.util.*;

public class LanguageMapFunction implements ExtractorFunction {

    @Override
    public void extractValues(RecordExtractor recordExtractor, List<String> values) {
        List<String> rawLanuageCodes = new ArrayList<>(values);
        values.clear();
        for ( String rawLanguageCode : rawLanuageCodes ) {
            String[] codes = rawLanguageCode.split(",");  // some marc records have comma separated lists
            for ( String code : codes ) {
                //  This split breaks a string into strings of length 3.
                //  From Traject:
                //  # sometimes multiple language codes are jammed together in one subfield, and
                //  # we need to separate ourselves. sigh.
                String[] threeLetterCodes = code.split("(?<=\\G...)");
                for ( String threeLetterCode : threeLetterCodes ) {
                    if (threeLetterCode.length()==3) {
                        String languageName = mapLanguageCode(threeLetterCode);
                        if (languageName != null && !values.contains(languageName)) {
                            values.add(languageName);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<String> getDependentFields() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isControlFieldDependent() {
        return false;
    }

    private static Map<String,String> languageCodeMap;

    public static String mapLanguageCode(String code) {
        if ( languageCodeMap == null ) {
            languageCodeMap = loadLanguageCodeMap();
        }
        String ret = languageCodeMap.get(code);
        return (ret != null) ? ret : code; // fall back to returning code if not found in map.
    }

    private static Map<String, String> loadLanguageCodeMap() {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, String> languageMap = null;
        try {
            languageMap = mapper.readValue(
                    Extractor.class.getClassLoader().getResourceAsStream("language-map.json"),
                    TypeFactory.defaultInstance()
                            .constructMapType(HashMap.class, String.class, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unable to load language map");
        }
        return languageMap;
    }
}
