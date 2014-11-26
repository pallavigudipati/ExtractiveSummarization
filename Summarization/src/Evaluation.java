import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.StringUtils;

public class Evaluation {

    public static String getTextFromP(Node pNode) {
        String text = "";
        NodeList childNodes = pNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element
                    && childNode.getNodeName() == "DIV") {
                text += getTextFromDiv(childNode);
            } else if (childNode.getNodeName() == "#text") {
                text += childNode.getTextContent();
            }
        }
        return text;
    }

    public static String getTextFromDiv(Node divNode) {
        String text = "";
        NodeList childNodes = divNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                if (childNode.getNodeName() == "P") {
                    text += getTextFromP(childNode);
                } else if (childNode.getNodeName() == "DIV") {
                    text += getTextFromDiv(childNode);
                }
            }
        }
        return text;
    }

    public static Paper extractFromXML(Document document) {
        Paper paper = new Paper();
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                if (node.getNodeName() == "TITLE") {
                    paper.title = node.getLastChild().getTextContent().trim();
                } else if (node.getNodeName() == "ABSTRACT") {
                    NodeList childNodes = node.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); ++j) {
                        Node childNode = childNodes.item(j);
                        if (childNode instanceof Element && childNode.getNodeName() == "P") {
                            paper.abstractTruth += getTextFromP(childNode);
                        }
                    }
                } else if (node.getNodeName() == "BODY") {
                    NodeList divNodes = node.getChildNodes();
                    for (int j = 0; j < divNodes.getLength(); ++j) {
                        Node divNode = divNodes.item(j);
                        if (divNode instanceof Element && divNode.getNodeName() == "DIV") {
                            paper.body += getTextFromDiv(divNode);
                        }
                    }
                }
            }
        }
        return paper;
    }

    public static void cleanUpAndWrite(String rawContent, String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(rawContent));
        String cleanContent = "";
        for (List sentence : dp) {
            cleanContent += StringUtils.join(sentence, " ");
        }
        try { 
            File outputFile = new File(fileName);
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            } 
            FileWriter fileWriter = new FileWriter(outputFile.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(cleanContent);
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println("Error in writing to " + fileName + " " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        File dir = new File("cmplg-xml");
        File[] directoryListing = dir.listFiles();
        int i = 1;
        if (directoryListing != null) {
          for (File child : directoryListing) {
              System.out.println(child.getName());
              String filePath = child.getAbsolutePath();
              String rootFileName = child.getName().split("\\.")[0];
              InputStream stream = new FileInputStream(filePath);
              Document document = builder.parse(stream);
              Paper paper = extractFromXML(document);
              cleanUpAndWrite(paper.abstractTruth, "PapersDataset/" + rootFileName 
                      + "_abstract.txt");
              cleanUpAndWrite(paper.body, "PapersDataset/" + rootFileName + "_body.txt");
              System.out.println(i + ":" + rootFileName);
              i += 1;
          }
        }
    }
}

class Paper {
    String title = "";
    String abstractTruth = "";
    String body = "";

    public String toString() {
        return title + "\n\n" + abstractTruth + "\n\n" + body;
    }
}