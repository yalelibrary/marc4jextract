package edu.yale.library.marc4jextract;

import org.junit.Before;
import org.junit.Test;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.*;
import org.marc4j.marc.Record;

import java.util.List;

import static org.junit.Assert.assertNull;

public class ExtractorTest {

    Record record;

    @Before
    public void setup() {
        record = loadTestRecord();
    }

    @Test
    public void extract() throws InvalidSpecException {
        List<String> results = Extractor.extract(record, "710ab", ScriptInclusion.NONE, true, null, " ");
        assert results.size() == 1;
        assert results.get(0).equals("Yasukuni Jinja (Tokyo, Japan). Shamusho");
    }


    @Test
    public void extract880None() throws InvalidSpecException {
        List<String> results = Extractor.extract(record, "245abnps", ScriptInclusion.NONE, true, null, " ");
        assert results.size() == 1;
        assert results.get(0).equals("Yasukuni Jinja : saiten to gyōji no subete");
    }

    @Test
    public void extract880Both() throws InvalidSpecException {
        List<String> results = Extractor.extract(record, "245abnps", ScriptInclusion.BOTH, true, null, " ");
        assert results.size() == 2;
        assert results.get(0).equals("Yasukuni Jinja : saiten to gyōji no subete");
        assert results.get(1).equals("靖國神社 : 祭典と行事のすべて");
    }

    @Test
    public void extract880Only() throws InvalidSpecException {
        List<String> results = Extractor.extract(record, "245abnps", ScriptInclusion.ONLY, true, null, " ");
        assert results.size() == 1;
        assert results.get(0).equals("靖國神社 : 祭典と行事のすべて");
    }

    @Test
    public void extractLanguageWithMapping() throws InvalidSpecException {
        List<String> results = Extractor.extract(record, "008[35-37]:041a", ScriptInclusion.NONE, false, null, " ", "languageMap");
        assert results.size() == 1;
        assert results.get(0).equals("Japanese");
    }

    @Test
    public void extractLanguageWithMapping2() throws InvalidSpecException {
        Record record = loadTestRecord2();
        List<String> results = Extractor.extract(record, "008[35-37]:041a", ScriptInclusion.NONE, false, null, " ", "languageMap");
        assert results.size() == 1;
        assert results.get(0).equals("English");
    }

    @Test
    public void sortAuthorExtractor() throws InvalidSpecException {
        Record record = loadTestRecordTitleThe();
        List<String> resultsRegularAuthor = Extractor.extract(record, "100:110:111", ScriptInclusion.BOTH, true, null, " ");
        List<String> resultsSortableAuthor = Extractor.extract(record,"sortAuthorExtractor");
        assert resultsRegularAuthor.size() == 1;
        assert resultsRegularAuthor.get(0).startsWith("Test, Author, author");
        assert resultsSortableAuthor.size() == 1;
        // the author sort function concats the title so that the secondary sort is by title automatically
        assert resultsSortableAuthor.get(0).startsWith("test, author,     test record");
    }

    @Test
    public void sortTitleExtractor() throws InvalidSpecException {
        Record record = loadTestRecordTitleThe();
        List<String> resultsRegularTitle = Extractor.extract(record, "245abfghknp", ScriptInclusion.NONE, true, null, " ");
        List<String> resultsSortableTitle = Extractor.extract(record,"sortTitleExtractor");
        assert resultsSortableTitle.size() == 1;
        assert resultsSortableTitle.get(0).startsWith("test record");
        assert resultsRegularTitle.size() == 1;
        assert resultsRegularTitle.get(0).startsWith("The Test Record");
    }

    @Test
    public void dateFieldChoice() throws InvalidSpecException {
        Record record = loadTestRecordBlankDate();
        List<String> extractDate = Extractor.extract(record, "260cg:264|*0|c:264|*1|c:264|*2|c:264|*3|c", ScriptInclusion.NONE, false, null, " ");
        assertNull(extractDate);
    }

    @Test // single date s
    public void dateExtractor() throws InvalidSpecException {
        List<String> results = Extractor.extract(record,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1986");
    }

    @Test // use 008 date1
    public void dateExtractorWithTypeRTwo008Date1() throws InvalidSpecException {
       Record record = loadTestRecord2();
        List<String> results = Extractor.extract(record,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1980");
    }

    @Test // Type T use 008 date1
    public void dateExtractorWithTypeTTwo008Date2() throws InvalidSpecException {
        Record record3 = loadTestRecord3();
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("2016");
    }

    @Test // use 264c
    public void dateExtractorTest264No008() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s        miu     o      00 0 eng d", "July 2019", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("2019");
    }


    @Test // use 260c
    public void dateExtractorTest260No008() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s        miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1954");
    }

    @Test // use 260 c
    public void dateExtractorTest008MoreThan100DateTypeQ() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223q11u12021miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1954");
    }

    @Test // replace u with 0 in date1, with 9 in date2
    public void dateExtractorTest008LessThan100DateTypeQ() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223q195u202umiu     o      00 0 eng d", "July No Date", "Sept 1958");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1950/2029");
    }

    @Test // range of dates type d
    public void dateExtractorTest008MoreThanDateTypeD() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223d193120190miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1931/2019");
    }

    @Test // range of dates type d 9999
    public void dateExtractorTest008DateTypeD() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223d1931uuuu0miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1931/9999");
    }

    @Test // e replace u to 0 single use
    public void dateExtractorTest008ReplaceOneUDateTypeE() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223e198u0615miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1980/1989");
    }

    @Test
    public void dateExtractorTest008ReplaceTwoUDateTypeS() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s19uu2uuumiu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1900/1999");
    }

    @Test
    public void dateExtractorTest008Replace4UTypeS() throws InvalidSpecException {
        Record record = loadTestRecord4();
        List<String> results = Extractor.extract(record,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("2022");
    }

    @Test // p use date1
    public void dateExtractorTest008IntegerDate2DateTypeP() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223p198918561miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1989");
    }

    @Test // use date1 Type P
    public void dateExtractorTest008NonIntegerDate2DateTypeP() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223p198u18671miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1980/1989");
    }

    @Test // R use 008 date1
    public void dateExtractorTest0082DateTypeR() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223r199u1887miu     o      00 0 eng d", "July No Date", "1999?");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1990/1999");
    }

    @Test  // R use 260c date1
    public void dateExtractorTest008NonIntegerDate2DateTypeRUse260() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223r      u21miu     o      00 0 eng d", "July No Date", "July 1954, Sept. 1955");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.get(0).equals("1954");
    }

    @Test // two dates 9999
    public void dateExtractorTest0082DateTypeU() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223u1948uuuumiu     o      00 0 eng d", "July No Date", "?");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1948/9999");
    }

    @Test // two dates type C 9999
    public void dateExtractorTest0082DateTypeC() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223c19849999miu     o      00 0 eng d", "", "?");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1984/9999");
    }

    @Test // two dates
    public void dateExtractorTest0082DateTypeD() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223d19842016miu     o      00 0 eng d", "", "1984-2016");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1984/2016");
    }

    @Test // two dates K
    public void dateExtractorTest0082DateTypeK() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223k179218966miu     o      00 0 eng d", "", "1792 -1896");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1792/1896");
    }

    @Test // two dates K 260
    public void dateExtractorTest0082DateTypeK260() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223k    miu     o      00 0 eng d", "", "1792 - 1896");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1792/1896");
    }

    @Test // two dates i date1 date2
    public void dateExtractorTest0082DateTypeI() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223i18uu18966miu     o      00 0 eng d", "", "18--?-1896");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1800/1896");
    }

    @Test // 264 type q
    public void dateExtractorTest0082DateType264Q() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223q17uu1u96miu     o      00 0 eng d", "178u and 179u", "18--? - 1896");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1780/1799");
    }

    @Test // 264 type q within 200
    public void dateExtractorTest0082DateType264Q2() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223q178u189umiu     o      00 0 eng d", "178u and 189u", "17--? - 189u");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1780/1899");
    }

    @Test // type p 260
    public void dateExtractorTest008NonIntegerDate2DateTypePNull() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223p       miu     o      00 0 eng d", "July No Date", "Sept 198x- and 198u");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1980/1989");
    }


    @Test
    public void dateExtractorTest008NonIntegerDate2DateTypePTwoU() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s      miu     o      00 0 eng d", "July 199x", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1990/1999");
    }

    @Test  // null
    public void dateExtractorTest008NonIntegerDate2DateTypeRNull() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223r    miu     o      00 0 eng d", "July No Date", "Sept 18 century");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results == null;
    }

    @Test // dateType n
    public void dateExtractorTest008DateTypeN() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("940705n gw n ger u", "", "[n.d.");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results == null;
    }

    @Test // R use 260
    public void dateExtractorTest008NonIntegerDate2DateTypeR() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223r   u  miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1954");
    }

    @Test //S use 264
    public void dateExtractorTest008264() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s   u  miu     o      00 0 eng d", "July 1955", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1955");
    }

    @Test // dateTypeC  Test replace |
    public void dateExtractorTest008DateTypeC() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223c19||2021miu     o      00 0 eng d", "July No Date", "Sept 1954");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1900/2021");
    }

    @Test // dateTypeT
    public void dateExtractorTest008DateTypeT() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223t199|2021miu     o      00 0 eng d", "July No Date", "Sept 199?");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1990/1999");
    }

    @Test // dateTypeT 264
    public void dateExtractorTest008DateTypeT264() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223t    miu     o      00 0 eng d", "199-", "Sept 199?");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() == 1;
        assert results.get(0).equals("1990/1999");
    }

    @Test  // S use 260
    public void dateExtractorTest008O260() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223s   u  miu     o      00 0 eng d", "July", "Sept 1956");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1956");
    }

    @Test // 9999
    public void dateExtractorTest008O260ContinuesTypeC() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223c   u  miu     o      00 0 eng d", "", "1950-9999");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1950/9999");
    }

    @Test // replace -- and 9999
    public void dateExtractorTest008O260ContinuesTypeC2649999() throws InvalidSpecException {
        Record record3 = loadTestDateRecord("191223c    u  miu     o      00 0 eng d", "", "1950-");
        List<String> results = Extractor.extract(record3,"dateExtractor");
        assert results.size() ==1;
        assert results.get(0).equals("1950/9999");
    }

    private Record loadTestRecord() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/558505.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestRecord2() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/55580.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestRecord3() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/12532681.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestRecord4() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/15593592.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestRecordTitleThe() {
        // mocking marc records is probably not worth the trouble.
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/15572040.marc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestRecordBlankDate() {
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/11749433.mrc"));
        if ( marcReader.hasNext() ) {
            return marcReader.next();
        }
        throw new RuntimeException("Unable to load test marc record.");
    }

    private Record loadTestDateRecord(String data008, String data264c, String data260c) {
        MarcReader marcReader = new MarcStreamReader( this.getClass().getResourceAsStream("/15572040.marc"));
        if ( marcReader.hasNext() ) {
            Record r = marcReader.next();
            List<ControlField> cfs = r.getControlFields();
            ControlField cf008 = r.getControlFields().stream().filter(cf->cf.getTag().equals("008")).findFirst().orElse(null);
            cf008.setData(data008);
            setOrAddFieldAndSubfield(r, "264", "c", data264c);
            setOrAddFieldAndSubfield(r, "260", "c", data260c);
            return r;
        }
        throw new RuntimeException("Unable to load test marc record");
    }

    private void setOrAddFieldAndSubfield( Record r, String tag, String subField, String value ) {
        MarcFactory marcFactory = MarcFactory.newInstance();
        final boolean[] fieldFound = {false};
        r.getDataFields().stream().filter(df->df.getTag().equals(tag)).forEach(
                dataField -> {
                    Subfield subfield = dataField.getSubfield(subField.charAt(0));
                    if (subfield == null) {
                        if ( !fieldFound[0] ) {
                            subfield = marcFactory.newSubfield(subField.charAt(0), value);
                            dataField.addSubfield(subfield);
                        }
                    } else {
                        subfield.setData(value);
                    }
                    fieldFound[0] = true;
                }
        );
        if ( !fieldFound[0]) {
            DataField dataField = marcFactory.newDataField(tag, ' ', ' ', subField, value);
            r.addVariableField(dataField);
        }
    }

}