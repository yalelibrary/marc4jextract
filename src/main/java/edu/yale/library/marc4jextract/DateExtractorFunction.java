package edu.yale.library.marc4jextract;

import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateExtractorFunction implements ExtractorFunction {

    private static Pattern threeToFourDigitPattern = Pattern.compile("\\b([\\dx]{3,4})\\b");
    private static Pattern twoThreeDigitQuestionablePattern = Pattern.compile("\\b([\\d]{3}[\\-?u|])");

    @Override
    public void extractValues(RecordExtractor recordExtractor, List<String> values) {
        ControlField tagField = recordExtractor.controlFields.get("008");
        if (tagField == null) return;
        String value = tagField.getData();
        String date1Str = "";
        String date2Str = "";
        int date1 = 0;
        int date2 = 0;
        String dataStructure = null;
        String dateType = value.substring(6, 7);

        if (value.length() >= 11 && value.substring(7, 11).length() > 0) {
            if (!value.substring(7, 11).contains("#") && !value.substring(7, 11).equals("||||")) {
                date1Str = value.substring(7, 11);
                date1Str = date1Str.contains("|") ? date1Str.replaceAll("\\|", "u") : date1Str;
            }
            if (value.length() > 15) {
                if (!value.substring(11, 15).contains("#") && !value.substring(11, 15).equals("||||")) {
                    date2Str = value.substring(11, 15);
                    date2Str = date2Str.contains("|") ? date2Str.replaceAll("\\|", "u") : date2Str;
                }
            }

            switch (dateType) {
                case "q":
                case "c":
                case "d":
                case "u":
                case "m":
                case "k":
                case "i":
                    date1 = getDateReplace(date1Str, "u", "0");
                    date2 = getDateReplace(date2Str, "u", "9");
                    break;
                case "p":
                case "r":
                case "s":
                case "e":
                case "t":
                    date1 = getDateReplace(date1Str, "u", "0");
                    date2 = getDateReplace(date1Str, "u", "9");
                    break;
                default:
                    date1 = date2 = 0;
                    break;
            }
            if (date1 != 0) {
                dataStructure = dateComparison(date1, date2, dateType);
            }
        }
        if (dataStructure == null || dataStructure.isEmpty()) {
            dataStructure = get264260Date(recordExtractor, values, dateType);
        }
        if (dataStructure != null && !dataStructure.isEmpty()) {
            values.add(dataStructure);
        }
    }

    @Override
    public List<String> getDependentFields() {
        return Arrays.asList("264", "260");
    }

    @Override
    public boolean isControlFieldDependent() {
        return true;
    }

    private int safeStrToInt(String number) {
        if ( StringUtils.isEmpty(number) ) return 0;
        try {
            return Integer.parseInt(number.trim());
        } catch (Exception e ) {
            return 0;
        }
    }

    private int getDateReplace(String dateStr, String charc, String num) {
        dateStr = dateStr.contains(charc) ? dateStr.replaceAll(charc, num) : dateStr;
        int date = safeStrToInt(dateStr);
        return date;
    }

    private String getDateFromDataFields(String dateType,List<DataField> fields) {
        int yearStart = 0;
        int yearEnd = 0;
        String dates = null;
        String date = "";
        for (DataField field : fields) {
            Subfield subfield = field.getSubfield('c');
            if (subfield == null) continue;
            date = subfield.getData();
            if (!StringUtils.isEmpty(date)) {
                Matcher questionable = twoThreeDigitQuestionablePattern.matcher(date);
                StringBuffer dateOut = new StringBuffer();

                while (questionable.find()) {
                    questionable.appendReplacement(dateOut, questionable.group(0).replace('|', 'x').replace('?', 'x').replace('-','x').replace('u','x'));
                }
                questionable.appendTail(dateOut);
                date = dateOut.toString();
                Matcher m = threeToFourDigitPattern.matcher(date);
                String yearStartStr = null;
                String yearEndStr = null;

                if (m.find()) {
                    yearStartStr = m.group(0);
                    yearStart = getDateReplace(yearStartStr, "x","0");

                    if (dateType.equalsIgnoreCase("t") || dateType.equalsIgnoreCase("p") || dateType.equalsIgnoreCase("r") || dateType.equalsIgnoreCase("s") || dateType.equalsIgnoreCase("e")) {
                        yearEnd = getDateReplace(yearStartStr, "x","9");
                    }
                }
                while (m.find()) {
                    if (!dateType.equalsIgnoreCase("t") && !dateType.equalsIgnoreCase("p") && !dateType.equalsIgnoreCase("r") && !dateType.equalsIgnoreCase("s") && !dateType.equalsIgnoreCase("e")) {
                        yearEndStr = m.group(0);
                        yearEnd = getDateReplace(yearEndStr, "x","9");
                    }
                }
                dates = dateComparison(yearStart, yearEnd, dateType);
                if (dates != null && !dates.isEmpty()) break;
            }
        }
        return dates;
    }

    private String get264260Date(RecordExtractor recordExtractor, List<String> values, String dateType) {

        List<DataField> fields264c = recordExtractor.dataFields.get("264");
        String foundDate = null;
        if (fields264c != null) {
            foundDate = getDateFromDataFields(dateType,fields264c);
        }
        if (foundDate == null || foundDate.isEmpty()) {
            List<DataField> fields260c = recordExtractor.dataFields.get("260");
            if (fields260c !=null) {
                foundDate = getDateFromDataFields(dateType, fields260c);
            }
        }
        return foundDate;
    }

    private String  dateComparison (int date1, int date2, String dateType) {
        String dates = null;
        if (date1 != 0) {
            if (dateType.equalsIgnoreCase("q")) {
                if (date2 == 0) {
                    dates = "" + date1;
                } else if (date2 >= date1 && (date2 - date1) <= 200) {
                    if (date2 == date1 ) {
                        dates ="" + date1;
                    } else {
                        dates = date1 + "/" + date2;
                    }
                }
            } else {
                if (date2 > date1) {
                    dates = date1 + "/" + date2;
                } else if (date2 == 0 && dateType.equalsIgnoreCase("c")) {
                    date2 = 9999;
                    dates = date1 + "/" + date2;
                }   else
                 {
                     dates = ""+ date1;
                }
            }
        }
        return dates;
    }

}
