package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.DataField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SortAuthorExtractorFunction implements ExtractorFunction {

    @Override
    public void extractValues(RecordExtractor recordExtractor, List<String> values) {
        StringBuilder sortableAuthor = new StringBuilder();
        List<String> primaryFields = Arrays.asList("100","110","111");
        List<String> primaryFieldValues = new ArrayList<>();
        for ( String fieldTag : primaryFields ) {
            List<DataField> fields = recordExtractor.dataFields.get(fieldTag);
            if ( fields != null ) {
                for (DataField field : fields) {
                    primaryFieldValues.addAll(
                            field.getSubfields().stream().
                                    filter(subfield -> Character.isAlphabetic(subfield.getCode())).
                                    map(subfield ->
                                            subfield.getData()).
                                    filter(s ->
                                            !StringUtils.isEmpty(s)).collect(Collectors.toList())
                    );
                }
            }
            if (primaryFieldValues.size() > 0 ) break;
        }
        if ( primaryFieldValues.size() > 0 ) {
            sortableAuthor.append(primaryFieldValues.get(0));
        }
        List<String> titleFields = Arrays.asList("240","245");
        for ( String fieldTag : titleFields ) {
            List<DataField> fields = recordExtractor.dataFields.get(fieldTag);
            if ( fields != null ) {
                for (DataField field : fields) {
                    String titleString = field.getSubfields().stream().map(sf -> sf.getData()).filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.joining(" "));
                    if (StringUtils.isEmpty(titleString)) {
                        continue;
                    }
                    String nonFilingString = "" + field.getIndicator2();
                    try {
                        int nonFiling = Integer.parseInt(nonFilingString);
                        if (nonFiling > 0) {
                            titleString = titleString.substring(nonFiling);
                        }
                    } catch (Exception e) {
                        //
                    }
                    if (!StringUtils.isEmpty(titleString)) {
                        sortableAuthor.append("     " + titleString);
                    }
                }
            }
        }
        String value = StringFormatter.trimPunctuation(sortableAuthor.toString().trim());
        if (!StringUtils.isEmpty(value)) {
            values.add(value.toLowerCase());
        }
    }


    @Override
    public List<String> getDependentFields() {
        return Arrays.asList("100","110","111","240","245");
    }

    @Override
    public boolean isControlFieldDependent() {
        return false;
    }

}
