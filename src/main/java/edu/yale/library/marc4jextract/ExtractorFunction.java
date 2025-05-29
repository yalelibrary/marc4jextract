package edu.yale.library.marc4jextract;

import java.util.List;

public interface ExtractorFunction {
    void extractValues( RecordExtractor recordExtractor, List<String> values);
    List<String> getDependentFields();
    boolean isControlFieldDependent();
}
