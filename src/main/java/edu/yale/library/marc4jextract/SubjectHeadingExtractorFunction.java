package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.*;
import java.util.stream.Collectors;

public class SubjectHeadingExtractorFunction implements ExtractorFunction {

    private final Map<String, String> additionalSubfieldsMap = new HashMap<>();
    private final List<String> defaultSubfieldLists = Arrays.asList("v", "x", "y", "z");
    {
        additionalSubfieldsMap.put("600", "abcdgjq");
        additionalSubfieldsMap.put("610", "abcdfgt");
        additionalSubfieldsMap.put("611", "acdegnqu");
        additionalSubfieldsMap.put("630", "adfhklmnoprst");
        additionalSubfieldsMap.put("650", "abcdg");
        additionalSubfieldsMap.put("651", "abcdfgh");
        additionalSubfieldsMap.put("690", "abcdg");
        additionalSubfieldsMap.put("691", "abcdfgh");
        additionalSubfieldsMap.put("692", "abcdgjq");
        additionalSubfieldsMap.put("693", "abcdg");
        additionalSubfieldsMap.put("694", "acdgnqu");
        additionalSubfieldsMap.put("695", "adfhklmnoprst");
    }

    @Override
    public void extractValues(RecordExtractor recordExtractor, List<String> subjectHeadings) {
        // look through the 600 fields and create subject headings
            for (DataField field : recordExtractor.getRecord().getDataFields()) {
                if (field.getTag().matches("6\\d\\d")) {
                    List<String> subfieldCodesList = new ArrayList<>(defaultSubfieldLists);
                    String additional = additionalSubfieldsMap.get(field.getTag());
                    if (additional != null) {
                        subfieldCodesList.add(0, additional);
                    }

                    List<ValuePosition> subjectComponents = new ArrayList<>();
                    // look for each subfields group, and combine values.
                    for (String subfieldCodes : subfieldCodesList) {
                        if (subfieldCodes.length() == 1) {
                            int pos = 0;
                            for (Subfield subfield : field.getSubfields()) {
                                if (subfieldCodes.indexOf(subfield.getCode()) >= 0) {
                                    String value = subfield.getData();
                                    value = StringFormatter.trimPunctuation(value);
                                    if (!StringUtils.isEmpty(value)) {
                                        subjectComponents.add(new ValuePosition(value, pos));
                                    }
                                }
                                pos++;
                            }
                        } else {
                            // concat into one field
                            List<String> subfieldValues = new ArrayList<>();
                            int pos = 0;
                            int ix = 0;
                            for (Subfield subfield : field.getSubfields()) {
                                if (subfieldCodes.indexOf(subfield.getCode()) >= 0) {
                                    if (pos == 0) pos = ix;
                                    subfieldValues.add(subfield.getData());
                                }
                                ix++;
                            }
                            String value = subfieldValues.stream().collect(Collectors.joining(" "));
                            value = StringFormatter.trimPunctuation(value);
                            if (!StringUtils.isEmpty(value)) {
                                subjectComponents.add(new ValuePosition(value, pos));
                            }
                        }
                    }

                    if (subjectComponents.size() > 0) {
                        // sort by subfield position in the field
                        subjectComponents.sort(Comparator.comparingInt(o -> o.position));

                        String label = subjectComponents.stream().map(c -> c.value).collect(Collectors.joining(" > "));
                        if (!subjectHeadings.contains(label)) {
                            subjectHeadings.add(label);
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


    public class ValuePosition {
        private final String value;
        private final int position;
        public ValuePosition(String value, int position) {
            this.value = value;
            this.position = position;
        }
    }
}
