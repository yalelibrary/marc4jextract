package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.DataField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SortTitleExtractorFunction implements ExtractorFunction {

    @Override
    public void extractValues(RecordExtractor recordExtractor, List<String> values) {
        List<DataField> fld245 = recordExtractor.dataFields.get("245");
        String firstValue = null;
        String nonFiling = null;
        for ( DataField dataField : fld245 ) {
            String s1 = dataField.getSubfieldsAsString("a");
            String s2 = dataField.getSubfieldsAsString("b");
            firstValue = Arrays.asList(s1,s2).stream().filter(s->!StringUtils.isEmpty(s)).collect(Collectors.joining(" "));
            nonFiling = ""+dataField.getIndicator2();
            if (!StringUtils.isEmpty(firstValue)) {
                break;
            }
        }
        if ( StringUtils.isEmpty(firstValue)) {
            for ( DataField dataField : fld245 ) {
                firstValue = dataField.getSubfieldsAsString("k");
                nonFiling = ""+dataField.getIndicator2();
                if (!StringUtils.isEmpty(firstValue)) {
                    break;
                }
            }
        }
        if (!StringUtils.isEmpty(firstValue)) {
            try {
                int nonFilingCount = Integer.parseInt(nonFiling);
                firstValue = firstValue.substring(nonFilingCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isEmpty(firstValue)) {
            firstValue = StringFormatter.trimPunctuation(firstValue);
            values.add(firstValue.toLowerCase());
        }
    }


    @Override
    public List<String> getDependentFields() {
        return Arrays.asList("245");
    }

    @Override
    public boolean isControlFieldDependent() {
        return false;
    }

}
