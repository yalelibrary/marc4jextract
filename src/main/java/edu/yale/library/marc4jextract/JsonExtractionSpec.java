package edu.yale.library.marc4jextract;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.marc4j.marc.Record;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonExtractionSpec {

    private List<Extractor> extractors = new ArrayList<>();

    public JsonExtractionSpec() {
    }

    public Map<String, List<String>> extractValues(Record marcRecord) {
        RecordExtractor recordExtractor = new RecordExtractor(marcRecord);
        long l = System.currentTimeMillis();
        Map<String, List<String>> ret = recordExtractor.extractValues(extractors);
        return ret;
    }

    public void loadExtractors( String json ) throws InvalidSpecException {
        loadExtractors( json.getBytes() );
    }

    public void loadExtractors( byte[] json ) throws InvalidSpecException {
        loadExtractors( new ByteArrayInputStream(json));
    }

    public void loadExtractors(InputStream jsonSpecInputStream) throws InvalidSpecException {
        // load the extractors as json....
        // create extractor list
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<ExtractorRecord> entries = null;
        try {
            entries = mapper.readValue(
                    jsonSpecInputStream,
                    TypeFactory.defaultInstance()
                            .constructCollectionType(List.class, ExtractorRecord.class));
        } catch (IOException e) {
            throw new InvalidSpecException("Unable to read specification for extraction", e);
        }

        for (ExtractorRecord entry : entries) {
            Extractor e = new Extractor(entry.name, entry.fieldSpec,
                    entry.scriptInclusion, entry.trimPunctuation, entry.filter, entry.delimiter,entry.extractFunction,
                    entry.keepSubfieldOrder);
            e.compile();
            extractors.add(e);
        }
    }

    private static class ExtractorRecord {
        private String name;
        private String fieldSpec;
        private ScriptInclusion scriptInclusion;
        private boolean trimPunctuation = true;
        private String filter;
        private String delimiter = " ";
        private String extractFunction;
        private boolean keepSubfieldOrder;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFieldSpec() {
            return fieldSpec;
        }

        public void setFieldSpec(String fieldSpec) {
            this.fieldSpec = fieldSpec;
        }

        public boolean isTrimPunctuation() {
            return trimPunctuation;
        }

        public void setTrimPunctuation(boolean trimPunctuation) {
            this.trimPunctuation = trimPunctuation;
        }

        public String getFilter() {
            return filter;
        }

        public void setFilter(String filter) {
            this.filter = filter;
        }

        public ScriptInclusion getScriptInclusion() {
            return scriptInclusion;
        }

        public void setScriptInclusion(ScriptInclusion scriptInclusion) {
            this.scriptInclusion = scriptInclusion;
        }

        public String getDelimiter() {
            return delimiter;
        }

        public void setDelimiter(String delimiter) {
            this.delimiter = delimiter;
        }
        public String getExtractFunction() {
            return extractFunction;
        }

        public void setExtractFunction(String extractFunction) {
            this.extractFunction = extractFunction;
        }

        public boolean isKeepSubfieldOrder() {
            return keepSubfieldOrder;
        }

        public void setKeepSubfieldOrder(boolean keepSubfieldOrder) {
            this.keepSubfieldOrder = keepSubfieldOrder;
        }
    }
}
