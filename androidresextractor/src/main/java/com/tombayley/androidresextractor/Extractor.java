package com.tombayley.androidresextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Extractor {

    // Config
    protected String resourcePath;
    protected String resourceTypeFileName = "strings.xml";
    protected String outputPath;
    protected String outputFileName;

    // Android resource constants
    public static final String ANDROID_VALUES_DIR_PREFIX = "values";
    public static final String ANDROID_STRINGS_XML_STRING = "string";
    public static final String ANDROID_STRINGS_XML_NAME = "name";
    public static final String ANDROID_RES_XML_ROOT_NAME = "resources";

    protected static final String ENCODING = "UTF-8";

    // Key: Attribute names of resource elements to be extracted
    // Value: The attribute name to be used for the extracted resource element
    protected HashMap<String, String> resToExtract;

    // Key: Directory name. E.g. values-es
    // Values: Map of extracted element attributes and values
    protected HashMap<String, HashMap<String, String>> extractedResources = new HashMap<>();

    protected boolean isBuilderValid = false;

    /**
     * Extractor constructor
     *
     * @param builder config used for the extraction
     */
    public Extractor(ExtractorBuilder builder) {
        resToExtract = builder.resourcesToExtract;
        if (resToExtract == null) {
            print("Resources to extract not set");
            return;
        }

//        resourceTypeFileName = builder.resourceTypeFileName;
//        if (resourceTypeFileName == null) {
//            print("Resource type file name not set");
//            return;
//        }

        resourcePath = builder.resourcesPath;
        if (resourcePath == null) {
            print("Resource path not set");
            return;
        }

        outputFileName = builder.outputFileName;
        if (outputFileName == null) {
            outputFileName = resourceTypeFileName;
        }

        outputPath = builder.outputPath;

        isBuilderValid = true;
    }

    /**
     * Begins to extraction
     */
    public void start() {
        if (!isBuilderValid) {
            print("Builder not valid");
            return;
        }

        File resRootDir = new File(resourcePath);
        if (!resRootDir.exists()) {
            print("Resource path does not exist");
            return;
        }

        File[] resDirs = resRootDir.listFiles();
        if (resDirs == null) {
            print("Resource pathname does not denote a directory / IO error");
            return;
        }

        // Loops through all dirs in defined resource dir
        for (File resDir : resDirs) {
            String valuesDirName = resDir.getName();

            // Resources are in "values(-*)" directories
            if (!valuesDirName.contains(ANDROID_VALUES_DIR_PREFIX)) continue;

            File[] resFiles = resDir.listFiles();
            if (resFiles == null) {
                print("Resource values dir for " + valuesDirName +" does not denote a directory / IO error");
                continue;
            }

            // Loops through all files in a values directory
            for (File resFile : resFiles) {
                if (!resFile.getName().equals(resourceTypeFileName)) continue;

                try {
                    extractXmlElements(resFile, valuesDirName);
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            saveExtractedResources();
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        print("DONE");
    }

    /**
     * Extracts xml elements from a given file that are specified with the ExtractorBuilder
     *
     * @param resFile
     * @param valuesDirName
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected void extractXmlElements(File resFile, String valuesDirName) throws IOException, ParserConfigurationException, SAXException {
        Document doc = readXmlFile(resFile);

        HashMap<String, String> extractedFileElements = new HashMap<>();

        NodeList nodeList = doc.getElementsByTagName(ANDROID_STRINGS_XML_STRING);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Element element = (Element) node;
            String attributeValue = element.getAttribute(ANDROID_STRINGS_XML_NAME);

            if (!isAttributeNeeded(attributeValue)) continue;

            extractedFileElements.put(attributeValue, element.getTextContent());
        }

        if (extractedFileElements.isEmpty()) return;

        extractedResources.put(valuesDirName, extractedFileElements);
    }

    /**
     * Checks if an attribute is one that was specified to be extracted
     *
     * @param attributeValue
     * @return
     */
    protected boolean isAttributeNeeded(String attributeValue) {
        return resToExtract.containsKey(attributeValue);
    }

    /**
     * Saves the extracted resources to the output path.
     * Saved files are placed in a dir with the same name as the original parent dir name of the file
     *  they were extracted from
     *
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    protected void saveExtractedResources() throws IOException, ParserConfigurationException, TransformerException {
        for (Map.Entry<String, HashMap<String, String>> extractedRes : extractedResources.entrySet()) {
            String dirName = extractedRes.getKey();
            HashMap<String, String> extractedElements = extractedRes.getValue();

            File ressourceFile = new File(
                    outputPath + File.separator + dirName,
                    outputFileName
            );

            Document document = createNewXmlDoc();
            Element root = document.createElement(ANDROID_RES_XML_ROOT_NAME);

            for (Map.Entry<String, String> element : extractedElements.entrySet()) {
                Element newElement = document.createElement(ANDROID_STRINGS_XML_STRING);
                newElement.setAttribute(ANDROID_STRINGS_XML_NAME, resToExtract.get(element.getKey()));
                newElement.setTextContent(element.getValue());
                root.appendChild(newElement);
            }

            document.appendChild(root);
            saveDocToFile(document, ressourceFile);
        }
    }

    /**
     * Creates a new xml document
     *
     * @return blank xml document
     * @throws ParserConfigurationException
     */
    protected Document createNewXmlDoc() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    /**
     * Saves an xml document to a specified file
     *
     * @param doc
     * @param file
     * @throws IOException
     * @throws TransformerException
     */
    protected void saveDocToFile(Document doc, File file) throws IOException, TransformerException {
        file.getParentFile().mkdirs();
        file.createNewFile();

        doc.setXmlStandalone(true);

        DOMSource source = new DOMSource(doc);

        FileOutputStream out = new FileOutputStream(file);
        StreamResult result = new StreamResult(out);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, ENCODING);
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        out.write(("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>\n").getBytes(ENCODING));

        transformer.transform(source, result);
    }

    /**
     * Writing System.out.println each time is too long...
     *
     * @param o
     */
    protected void print(Object o) {
        System.out.println(o);
    }

    /**
     * Reads a specified xml file
     *
     * @param file
     * @return xml document
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected Document readXmlFile(File file) throws IOException, ParserConfigurationException, SAXException {
        InputStream inputStream = new FileInputStream(file);
        Reader reader = new InputStreamReader(inputStream, ENCODING);
        InputSource is = new InputSource(reader);
        is.setEncoding(ENCODING);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        return doc;
    }

    /**
     * Builder for configuring the extractor
     */
    public static class ExtractorBuilder {
        HashMap<String, String> resourcesToExtract = null;
//        String resourceTypeFileName = null;
        String resourcesPath = null;
        String outputPath = "output";
        String outputFileName = null;

        /**
         * A map of attribute names to extract (the key) and a new attribute name to be used to save
         * the extracted element with (the value)
         *
         * @param toExtract
         * @return
         */
        public ExtractorBuilder setResourcesToExtract(HashMap<String, String> toExtract) {
            resourcesToExtract = toExtract;
            return this;
        }

        /**
         * Used to set the type of resource to extract from eventually.
         * E.g. "strings.xml", "colors.xml" etc
         */
//        public ExtractorBuilder setResourceTypeFileName(String name) {
//            resourceTypeFileName = name;
//            return this;
//        }

        /**
         * Path to dir containing Android resources
         *
         * @param path
         * @return
         */
        public ExtractorBuilder setResourcesPath(String path) {
            resourcesPath = path;
            return this;
        }

        /**
         * Output path of extracted xml
         *
         * @param path
         * @return
         */
        public ExtractorBuilder setOutputPath(String path) {
            outputPath = path;
            return this;
        }

        /**
         * Sets the name of the resource files output.
         * Examples:
         *  - Can just be "strings.xml"
         *  - If extracting a group of related elements e.g. strings containing certain questions
         *      the name can be set to "questions.xml"
         *
         * @param name
         * @return
         */
        public ExtractorBuilder setOutputFileName(String name) {
            outputFileName = name;
            return this;
        }
    }

}

