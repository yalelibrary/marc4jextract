package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.Record;
import org.marc4j.marc.DataField;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.VariableField;
import org.marc4j.marc.Subfield;

import java.util.*;

class RecordExtractor {

    private Record record;
    Map<String, List<DataField>> dataFields = new HashMap<>();
    Map<String, List<DataField>> linked880s = new HashMap<>();
    Map<String, ControlField> controlFields;

    public RecordExtractor(Record record) {
        this.record = record;
    }

    public Map<String, List<String>> extractValues(List<Extractor> extractors) {
        Map<String, List<String>> ret = new HashMap<>();
        loadInterestingFields(extractors);
        for (Extractor extractor : extractors) {
            List<String> v = extractor.extractValues(this);
            if (v != null && !v.isEmpty()) {
                ret.put(extractor.getName(), v);
            }
        }
        return ret;
    }

    private void loadInterestingFields(List<Extractor> extractors) {
        Set<String> fieldTags = new HashSet<>();
        boolean hasControls = false;
        for (Extractor extractor : extractors) {
            if (extractor.extractorComponentList != null) {
                for (Extractor.ExtractorComponent component : extractor.extractorComponentList) {
                    if (!component.isControl()) {
                        fieldTags.add(component.fieldTag);
                    } else {
                        hasControls = true;
                    }
                }
            }
            String extractFunctionName = extractor.getExtractFunction();
            if ( !StringUtils.isEmpty(extractFunctionName) ) {
                ExtractorFunction extractFunction = FunctionMap.get(extractFunctionName);
                if ( extractFunction != null ) {
                    fieldTags.addAll(extractFunction.getDependentFields());
                    hasControls = hasControls || extractFunction.isControlFieldDependent();
                }
            }
        }
        fieldTags.add("880");
        String tags[] = fieldTags.toArray(new String[fieldTags.size()]);
        List<VariableField> fields = record.getVariableFields(tags);
        long ix = 0;
        for (VariableField field : fields) {
            if (field instanceof DataField) {
                field.setId(ix++);
                if (field.getTag().equals("880")) {
                    Subfield subfield = ((DataField) field).getSubfield('6');
                    if (subfield != null ) {
                        String linkage = subfield.getData();
                        String fldId = linkage.substring(0, 3);
                        if (fieldTags.contains(fldId)) {  // only store ones related to fields we're interested in
                            linked880s.compute(fldId, (s, linkedFields) -> {
                                if (linkedFields == null) {
                                    linkedFields = new ArrayList<>();
                                }
                                linkedFields.add((DataField) field);
                                return linkedFields;
                            });
                        }
                    }
                } else {
                    dataFields.compute(field.getTag(), (s, variableFields) -> {
                        if (variableFields == null) {
                            variableFields = new ArrayList<>();
                        }
                        variableFields.add((DataField) field);
                        return variableFields;
                    });
                }
            }
        }
        ix = -5000;
        if (hasControls) {
            controlFields = new HashMap<>();
            List<ControlField> controlFields = record.getControlFields();
            for (ControlField controlField : controlFields) {
                controlField.setId(ix++);
                this.controlFields.put(controlField.getTag(), controlField);
            }
        }
    }

    public Record getRecord() {
        return record;
    }
}
