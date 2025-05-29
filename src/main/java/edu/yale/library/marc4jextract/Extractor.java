package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Extractor {

    private String spec;
    private String name;
    private ScriptInclusion scriptInclusion;
    private boolean trimPunctuation;
    private String filter;
    private String delimiter;
    private Pattern filterPattern;
    private String extractFunction;
    List<ExtractorComponent> extractorComponentList;
    private boolean keepSubfieldOrder = false;


    Extractor(String name,
              String spec,
              ScriptInclusion scriptInclusion,
              boolean trimPunctuation,
              String filter,
              String delimiter,
              String extractFunction,
              boolean keepSubfieldOrder) {
        this.name = name;
        this.spec = spec;
        this.scriptInclusion = scriptInclusion;
        this.trimPunctuation = trimPunctuation;
        this.filter = filter;
        this.delimiter = delimiter;
        this.extractFunction = extractFunction;
        this.keepSubfieldOrder = keepSubfieldOrder;
    }


    Extractor(String name,
              String spec,
              ScriptInclusion scriptInclusion,
              boolean trimPunctuation,
              String filter,
              String delimiter,
              String extractFunction) {
        this.name = name;
        this.spec = spec;
        this.scriptInclusion = scriptInclusion;
        this.trimPunctuation = trimPunctuation;
        this.filter = filter;
        this.delimiter = delimiter;
        this.extractFunction = extractFunction;
    }

    Extractor(String name,
              String spec,
              ScriptInclusion scriptInclusion,
              boolean trimPunctuation,
              String filter,
              String delimiter) {
        this.name = name;
        this.spec = spec;
        this.scriptInclusion = scriptInclusion;
        this.trimPunctuation = trimPunctuation;
        this.filter = filter;
        this.delimiter = delimiter;
    }



    public static List<String> extract(Record record, String spec, ScriptInclusion scriptInclusion, boolean trimPunctuation, String filter, String delimiter) throws InvalidSpecException {
        Extractor extractor = new Extractor("single", spec, scriptInclusion, trimPunctuation, filter, delimiter, null).compile();
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> valueMap = recordExtractor.extractValues(Arrays.stream(new Extractor[]{extractor}).collect(Collectors.toList()));
        return valueMap.get("single");
    }

    public static List<String> extract(Record record, String extractFunction) throws InvalidSpecException {
        Extractor extractor = new Extractor("single",null,null,false,null,null, extractFunction).compile();
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> valueMap = recordExtractor.extractValues(Arrays.stream(new Extractor[]{extractor}).collect(Collectors.toList()));
        return valueMap.get("single");
    }

    public static List<String> extract(Record record, String spec, ScriptInclusion scriptInclusion, boolean trimPunctuation, String filter, String delimiter, String extractFunction) throws InvalidSpecException {
        Extractor extractor = new Extractor("single", spec, scriptInclusion, trimPunctuation, filter, delimiter, extractFunction).compile();
        RecordExtractor recordExtractor = new RecordExtractor(record);
        Map<String, List<String>> valueMap = recordExtractor.extractValues(Arrays.stream(new Extractor[]{extractor}).collect(Collectors.toList()));
        return valueMap.get("single");
    }

    public Extractor compile() throws InvalidSpecException {
        if (spec != null) {
            String[] specComponents = this.spec.split(":");
            extractorComponentList = new ArrayList<>();
            for (String specComponent : specComponents) {
                if (specComponent.contains("X")) {
                    if (specComponent.startsWith("XX", 1)) {
                        for (int i = 0; i < 100; i++) {
                            extractorComponentList.add(new ExtractorComponent(specComponent.substring(0, 1) +
                                    String.format("%02d", i) + specComponent.substring(3)));
                        }
                    } else if (specComponent.startsWith("X", 2)) {
                        for (int i = 0; i < 10; i++) {
                            extractorComponentList.add(new ExtractorComponent(specComponent.substring(0, 2) +
                                    String.format("%d", i) + specComponent.substring(3)));
                        }
                    }
                } else {
                    extractorComponentList.add(new ExtractorComponent(specComponent));
                }
            }
            if (!StringUtils.isEmpty(filter)) {
                try {
                    filterPattern = Pattern.compile(filter);
                } catch (Exception e) {
                    throw new InvalidSpecException("Unable to parse filter", e);
                }
            }
        }
        return this;
    }


    List<String> extractValues(RecordExtractor recordExtractor) {
        List<String> ret = new ArrayList<>();
        componentExtract(recordExtractor, ret);
        if (extractFunction != null) {
            ExtractorFunction extractorFunction = FunctionMap.get(extractFunction);
            if ( extractorFunction != null ) {
                extractorFunction.extractValues(recordExtractor, ret);
            }
        }
        return ret;
    }

    private void componentExtract(RecordExtractor recordExtractor, List<String> values) {
        List<ValueAndId> valuesWithIds = new ArrayList<>();
        if ( extractorComponentList != null ) {
            for (ExtractorComponent component : extractorComponentList) {
                component.extract(recordExtractor, valuesWithIds);
            }
        }
        // sort by id, which is the order in the record
        Collections.sort(valuesWithIds, (o1, o2) -> (int)(o1.id - o2.id));
        for (ValueAndId value: valuesWithIds ) {
            values.add(value.value);
        }
    }

    String getName() {
        return name;
    }

    String getExtractFunction() {
        return extractFunction;
    }

    static class ValueAndId {
        long id;
        String value;

        public ValueAndId(long id, String value) {
            this.value = value;
            this.id = id;
        }
    }

    class ExtractorComponent {

        int range1 = -1;
        int range2 = -1;
        char indicator1 = '*';
        char indicator2 = '*';
        String fieldTag;
        String sfSpec;

        public ExtractorComponent(String specComponent) {
            parseSpec(specComponent);
        }

        public boolean isControl() {
            return fieldTag.startsWith("00");
        }

        private void parseSpec(String specComponent) {
            specComponent = specComponent.replace("_ATOZ_", "abcdefghijklmnopqrstuvwxyz");
            fieldTag = specComponent.substring(0, 3);
            if (specComponent.length() > 3) {
                int ix = 3;
                if (specComponent.charAt(ix) == '|') {
                    indicator1 = specComponent.charAt(4);
                    indicator2 = specComponent.charAt(5);
                    ix += 4;
                }
                if (specComponent.length() > ix) {
                    if (specComponent.charAt(ix) == '[') {
                        String range = specComponent.substring(ix + 1, specComponent.indexOf(']', ix));
                        if (range.indexOf('-') >= 0) {
                            range1 = Integer.parseInt(range.substring(0, range.indexOf('-')));
                            range2 = Integer.parseInt(range.substring(range.indexOf('-') + 1, range.length()));
                        } else {
                            range1 = Integer.parseInt(range);
                        }
                    } else {
                        sfSpec = specComponent.substring(ix);
                    }
                }
            }
        }

        private void extract(RecordExtractor record, List<ValueAndId> valuesWithIds) {
            if (isControl()) {
                ControlField controlField = record.controlFields.get(fieldTag);
                if (controlField == null) return;
                String value = controlField.getData();
                if (range1 >= 0 && range2 > 0 && range1 < value.length() && range1 < range2) {
                    valuesWithIds.add(new ValueAndId(controlField.getId(), controlField.getData().substring(range1, Math.min(value.length(), range2 + 1))));
                } else if (range1 >= 0 && range1 < value.length()) {
                    valuesWithIds.add(new ValueAndId(controlField.getId(), controlField.getData().substring(range1, range1 + 1)));
                } else {
                    valuesWithIds.add(new ValueAndId(controlField.getId(), value));
                }
            } else {
                if (scriptInclusion != ScriptInclusion.ONLY) {
                    List<DataField> fields = record.dataFields.get(fieldTag);
                    extractFromDataFields(valuesWithIds, fields);
                }
                if (scriptInclusion != ScriptInclusion.NONE) {
                    List<DataField> fields880 = record.linked880s.get(fieldTag);
                    extractFromDataFields(valuesWithIds, fields880);
                }
            }
        }

        private Comparator<Subfield> standardComparator(String sfSpec) {
            return Comparator.comparingInt(subfield -> sfSpec.indexOf(subfield.getCode()));
        }

        private Comparator<Subfield> nonSorting() {
            return new Comparator<Subfield>() {
                @Override
                public int compare(Subfield o1, Subfield o2) {
                    return 0;
                }
            };
        }

        private void extractFromDataFields( List<ValueAndId> valuesWithIds, List<DataField> fields) {
            if (fields != null) {
                for (DataField field : fields) {
                    if ((indicator1 == '*' || field.getIndicator1() == indicator1)
                            && (indicator2 == '*' || field.getIndicator2() == indicator2)) {
                        List<String> r = new ArrayList<>();
                        if (sfSpec != null && sfSpec.length() > 1) {
                            Comparator<Subfield> comparator = keepSubfieldOrder ? nonSorting() : standardComparator(sfSpec);
                            List<String> vals = field.getSubfields().stream().
                                    filter(subfield -> sfSpec.indexOf(subfield.getCode()) >= 0).
                                    sorted(comparator).
                                    map(subfield ->
                                            subfield.getData()).
                                    filter(s ->
                                            !StringUtils.isEmpty(s)).collect(Collectors.toList());
                            if (vals.size()>0) {
                                r.add(vals.stream().collect(Collectors.joining(delimiter)));
                            }
                        } else if (sfSpec != null && sfSpec.length() == 1) {
                            Comparator<Subfield> comparator = standardComparator(sfSpec);
                            r.addAll(field.getSubfields().stream().
                                    filter(subfield -> sfSpec.indexOf(subfield.getCode()) >= 0).
                                    sorted(comparator).
                                    map(subfield ->
                                            subfield.getData()).filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList()));
                        } else {
                            List<String> vals = field.getSubfields().stream().
                                    filter(subfield -> Character.isAlphabetic(subfield.getCode())).
                                    map(subfield ->
                                            subfield.getData()).
                                    filter(s ->
                                            !StringUtils.isEmpty(s)).collect(Collectors.toList());
                            if (vals.size()>0) {
                                r.add(vals.stream().collect(Collectors.joining(delimiter)));
                            }
                        }
                        if (trimPunctuation) {
                            r = r.stream().map(s -> StringFormatter.trimPunctuation(s)).collect(Collectors.toList());
                        }
                        if (filterPattern != null) {
                            r = r.stream().map(s -> filterPattern.matcher(s).replaceFirst("$1")).collect(Collectors.toList());
                        }
                        for (String value : r) {
                            valuesWithIds.add(new ValueAndId(field.getId(),value));
                        }
                    }
                }
            }
        }
    }
}


