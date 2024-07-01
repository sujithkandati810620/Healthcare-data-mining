package gov.nih.nlm.nls.metamap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import se.sics.prologbeans.PBTerm;
import org.apache.commons.text.similarity.JaroWinklerDistance;

public class MetaMapAnnotator {

    private static Properties configProp = new Properties();

    private static final String EMPTY_STRING = "";
    private static final String PATH_TO_RESOURCES = "./resources/";

    MetaMapApi api;
    private static List<String> ignoredWords = new ArrayList<String>();
    private static List<String> includePOSTags = new ArrayList<String>();

    public MetaMapAnnotator(String serverhost, int serverport) {
        this.api = new MetaMapApiImpl();
        this.api.setHost(serverhost);
        this.api.setPort(serverport);
    }

    public MetaMapAnnotator() {
        this.api = new MetaMapApiImpl();
    }

    public static void main(String[] args) {

        try {
            init();
            processWebUserPostData();
        } catch (Exception ex) {
            Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

    }

    private static void init() throws IOException {
        File configFile = new File(PATH_TO_RESOURCES + "MetaMapAnnotatorConfig.properties");
        FileInputStream configStream = new FileInputStream(configFile);
        configProp.load(configStream);
        populateIgnoredWordsList();
        populatePOSTags();
    }

    private static void populateIgnoredWordsList() {
        Reader in = null;
        try {
            File file = new File(PATH_TO_RESOURCES + configProp.getProperty("ignored_words_file_name"));
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                ignoredWords.add(record.get(0));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void populatePOSTags() {
        Reader in = null;
        try {
            File file = new File(PATH_TO_RESOURCES + configProp.getProperty("include_pos_tags_file_name"));
            in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                includePOSTags.add(record.get(0));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MetaMapAnnotator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setTimeout(int interval) {
        this.api.setTimeout(interval);
    }

ate static void processWebUserPostData() throws FileNotFoundException, IOException, Exception {
        File dir = new File(configProp.getProperty("web_scraper_csv_folder"));
                String serverhost = MetaMapApi.DEFAULT_SERVER_HOST;
        int serverport = MetaMapApi.DEFAULT_SERVER_PORT;
        int timeout = -1;

        PrintStream output = System.out;
        MetaMapAnnotator frontEnd = new MetaMapAnnotator(serverhost, serverport);
        List<String> options = new ArrayList<>();
        options.add("-y");
        options.add("--restrict_to_sts");
        options.add("dsyn,sosy,topp,clnd,bpoc");
        options.add("--unique_acros_abbrs_only");
        options.add("--no_derivational_variants");
        options.add("--TAGGER_SERVER");
        options.add("localhost");
        options.add("--composite_phrases");
        options.add("4");

        if (timeout > -1) {
            frontEnd.setTimeout(timeout);
        }
        File[] csvFilesList = dir.listFiles((File directory, String filename) -> filename.endsWith(".csv"));
        for (File file : csvFilesList) {
            Reader in = new FileReader(file);
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            String outputFile = PATH_TO_RESOURCES + file.getName();
            CSVFormat csvFileFormat = CSVFormat.EXCEL.withHeader("PostNumber", "DiseaseId", "DiseaseName", "SymptomId", "SymptomName", "TreatmentId", "TreatmentName", "DrugId", "DrugName", "BodypartId", "BodypartName");
            CSVPrinter csvFilePrinter;
            try (FileWriter fileWriter = new FileWriter(outputFile)) {
                csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                for (CSVRecord record : records) {
                    long recordNumber = record.getRecordNumber();
                    String diseaseCategory = record.get(0);
                    String postLink = record.get(1);
                    String postHeading = record.get(2);
                    String postContent = record.get(3);

                    diseaseCategory = stripNonASCII(diseaseCategory);
                    postHeading = stripNonASCII(postHeading);
                    postContent = stripNonASCII(postContent);

                    System.out.println("----------------------------------------------------");
                    triggerMetaMap(frontEnd, output, options, csvFilePrinter, recordNumber, diseaseCategory, postHeading, postContent);
                    System.out.println("----------------------------------------------------");
                }
                fileWriter.flush();
            }
            csvFilePrinter.close();
        }
        
        frontEnd.api.disconnect();

    }

    private static String stripNonASCII(String inputText) {
        String result = inputText;
        result = result.replaceAll("[^\\x00-\\x7F]", EMPTY_STRING);
        result = result.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", EMPTY_STRING);
        result = result.replaceAll("\\p{C}", EMPTY_STRING);
        result = result.trim();
        return result;
    }

    private static void triggerMetaMap(MetaMapAnnotator mmFrontEnd, PrintStream output,List<String> options, CSVPrinter csvFilePrinter, long recordNumber, String category, String postHeading, String postContent) throws Exception {

        if (!"".equals(postContent) && !"".equals(category)) {
            mmFrontEnd.process(csvFilePrinter, recordNumber, category, postContent, output, options);
        }
    }

    private void process(CSVPrinter csvFilePrinter, long recordNumber, String category, String terms, PrintStream out, List<String> serverOptions) throws Exception {
        if (serverOptions.size() > 0) {
            api.setOptions(serverOptions);
        }
        int diseasesCount = 0;
        HashMap<String, String> diseaseDict = new HashMap<>();
        HashMap<String, String> symptomDict = new HashMap<>();
        HashMap<String, String> treatmentDict = new HashMap<>();
        HashMap<String, String> drugsDict = new HashMap<>();
        HashMap<String, String> bodyPartDict = new HashMap<>();
        List<Result> categoryMM = api.processCitationsFromString(category);
        String categoryMMName = EMPTY_STRING;
        String categoryMMId = EMPTY_STRING;
        for (Result result : categoryMM) {
            if (result != null) {
                for (Utterance utterance : result.getUtteranceList()) {
                    for (PCM pcm : utterance.getPCMList()) {
                        for (Mapping map : pcm.getMappingList()) {
                            for (Ev mapEv : map.getEvList()) {
                                if (mapEv.getSemanticTypes().contains("dsyn")) {
                                    categoryMMName = mapEv.getPreferredName();
                                    categoryMMId = mapEv.getConceptId();
                                }
                            }
                        }
                    }
                }

            }

        }
        List<Result> resultList = api.processCitationsFromString(terms);

        for (Result result : resultList) {
            if (result != null) {
                for (Utterance utterance : result.getUtteranceList()) {
                    for (PCM pcm : utterance.getPCMList()) {
                        out.println(" Phrase: " + pcm.getPhrase().getPhraseText());
                        out.println("  Mappings:");
                        for (Mapping map : pcm.getMappingList()) {
                            for (Ev mapEv : map.getEvList()) {
                                boolean filterOut = false;
                                for (String ignoreWord : ignoredWords) {
                                    for (String matchedWord : mapEv.getMatchedWords()) {
                                        if (matchedWord.toLowerCase().equals(ignoreWord.toLowerCase())) {
                                            if (mapEv.getMatchedWords().size() == 1) {
                                                filterOut = true;
                                            }
                                        }
                                    }
                                }
                                if (!filterOut) {
                                    String diseaseName = EMPTY_STRING, symptomName = EMPTY_STRING, diseaseId = EMPTY_STRING, symptomId = EMPTY_STRING, treatmentName = EMPTY_STRING, treatmentId = EMPTY_STRING, drugName = EMPTY_STRING, drugId = EMPTY_STRING, bodyPartName = EMPTY_STRING, bodypartId = EMPTY_STRING;
                                    if (mapEv.getSemanticTypes().contains("dsyn")) {
                                        diseaseName = mapEv.getPreferredName();
                                        diseaseId = mapEv.getConceptId();
                                        diseaseDict.put(diseaseId, diseaseName);
                                        diseasesCount += 1;
                                    }
                                    if (mapEv.getSemanticTypes().contains("sosy")) {
                                        symptomName = mapEv.getPreferredName();
                                        symptomId = mapEv.getConceptId();
                                        symptomDict.put(symptomId, symptomName);
                                    }
                                    if (mapEv.getSemanticTypes().contains("topp")) {
                                        treatmentName = mapEv.getPreferredName();
                                        treatmentId = mapEv.getConceptId();
                                        treatmentDict.put(treatmentId, treatmentName);
                                    }
                                    if (mapEv.getSemanticTypes().contains("clnd")) {
                                        drugName = mapEv.getPreferredName();
                                        drugId = mapEv.getConceptId();
                                        drugsDict.put(drugId, drugName);
                                    }
                                    if (mapEv.getSemanticTypes().contains("bpoc")) {
                                        bodyPartName = mapEv.getPreferredName();
                                        bodypartId = mapEv.getConceptId();
                                        bodyPartDict.put(bodypartId, bodyPartName);

                                    }
                                }
                            }
                        }
                    }
                }

            } else {
                out.println("The result instance is NULL!");
            }
        }
        if (!EMPTY_STRING.equals(categoryMMId)) {
            if (diseasesCount == 0) {
                diseaseDict.put(categoryMMId, categoryMMName);
            }
            if (diseasesCount > 1) {
                diseaseDict.clear();
                diseaseDict.put(categoryMMId, categoryMMName);
            }
        }
        if (diseaseDict.size() == 1) {
            for (Map.Entry<String, String> entry : diseaseDict.entrySet()) {
                csvFilePrinter.printRecord(recordNumber, entry.getKey(), entry.getValue(), EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
            }
            for (Map.Entry<String, String> entry : symptomDict.entrySet()) {
                csvFilePrinter.printRecord(recordNumber, EMPTY_STRING, EMPTY_STRING, entry.getKey(), entry.getValue(), EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
            }
            for (Map.Entry<String, String> entry : treatmentDict.entrySet()) {
                csvFilePrinter.printRecord(recordNumber, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, entry.getKey(), entry.getValue(), EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
            }
            for (Map.Entry<String, String> entry : drugsDict.entrySet()) {
                csvFilePrinter.printRecord(recordNumber, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, entry.getKey(), entry.getValue(), EMPTY_STRING, EMPTY_STRING);
            }
            for (Map.Entry<String, String> entry : bodyPartDict.entrySet()) {
                csvFilePrinter.printRecord(recordNumber, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING, entry.getKey(), entry.getValue());
            }
        }
        this.api.resetOptions();
    }

    private List<PBTerm> listArgs(PBTerm compoundTerm) {
        List<PBTerm> elements = new ArrayList<PBTerm>();
        for (int i = 1; i <= compoundTerm.getArity(); i++) {
            elements.add(compoundTerm.getArgument(i));
        }
        return elements;
    }

    private List<PBTerm> listElements(PBTerm listTerm) {
        List<PBTerm> elements = new ArrayList<PBTerm>();
        for (int i = 1; i <= listTerm.length(); i++) {
            elements.add(TermUtils.getListElement(listTerm, i));
        }
        return elements;
    }

    private List<String> listAtomTermses(PBTerm mincoManTerm) {
        List<String> atomTermsList = new ArrayList<String>();
        if (mincoManTerm.isListCell()) {
            for (PBTerm elem : listElements(mincoManTerm)) {
                List<String> subMatches = listAtomTermses(elem);
                atomTermsList.addAll(subMatches);
            }
        } else if (mincoManTerm.isCompound()) {
            atomTermsList.add("arg:" + mincoManTerm.getName());
            for (PBTerm elem : listArgs(mincoManTerm)) {
                List<String> subMatches = listAtomTermses(elem);
                atomTermsList.addAll(subMatches);
            }
        } else if (mincoManTerm.isAtom()) {
            atomTermsList = new ArrayList<String>();
            atomTermsList.add(mincoManTerm.getName());
            return atomTermsList;
        }
        return atomTermsList;
    }

    private String getPOSMatch(HashMap<String, String> posTagList, String matchedWord) {
        for (HashMap.Entry<String, String> posTagEntry : posTagList.entrySet()) {
            String key = posTagEntry.getKey();
            if (key.toLowerCase().equals(matchedWord.toLowerCase())) {
                return posTagEntry.getValue();
            }
        }
        for (HashMap.Entry<String, String> posTagEntry : posTagList.entrySet()) {
            String key = posTagEntry.getKey();
            if (key.toLowerCase().contains(matchedWord.toLowerCase())) {
                return posTagEntry.getValue();
            }
        }
        for (HashMap.Entry<String, String> posTagEntry : posTagList.entrySet()) {
            String key = posTagEntry.getKey();
            if (matchedWord.toLowerCase().contains(key.toLowerCase())) {
                return posTagEntry.getValue();
            }
        }
        return EMPTY_STRING;
    }

    private boolean isAcronym(String conceptName, String matchedWords) {
        String match = matchedWords;
        match = match.substring(1, match.length() - 1);

        if (!(conceptName.toLowerCase().contains(match.toLowerCase()) || match.toLowerCase().contains(conceptName.toLowerCase()))) {
            return true;
        }
        JaroWinklerDistance dist = new JaroWinklerDistance();
        double measure = dist.apply(conceptName, matchedWords);
        if (measure > 0.8) {
            return false;
        }
        return false;
    }

    private HashMap<String, String> listInputMatches(PBTerm mincoManTerm) {
        HashMap<String, String> termlist = new HashMap<String, String>();
        List<String> atomTermsList = listAtomTermses(mincoManTerm);
        PeekingIterator<String> iter = new PeekingIterator<>(atomTermsList.iterator());
        while (iter.hasNext()) {

            if (iter.next().equals("arg:inputmatch")) {
                String tag, word = EMPTY_STRING;
                word = iter.next();
                while (iter.hasNext()) {
                    String p = iter.peek();
                    if (!p.startsWith("arg:")) {
                        word = word + " " + iter.next();
                    } else if (p.startsWith("arg:tag")) {
                        iter.next();
                        tag = iter.next();
                        termlist.put(word, tag);
                    } else {
                        break;
                    }
                }
            }
        }
        return termlist;
    }
}
