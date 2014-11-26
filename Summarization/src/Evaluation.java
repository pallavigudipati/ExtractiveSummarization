import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Evaluation {

    public static String getTextFromP(Node pNode) {
        String text = "";
        NodeList childNodes = pNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); ++i) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element && childNode.getNodeName() == "DIV") {
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

    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream stream = new FileInputStream("cmplg-xml/9404003.xml");
        Document document = builder.parse(stream);

        Paper paper = new Paper();

        NodeList nodeList = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                switch (node.getNodeName()) {
                    case "TITLE": // System.out.println(node.getLastChild().getTextContent().trim());
                        paper.title = node.getLastChild().getTextContent().trim();
                        break;
                    case "ABSTRACT":
                        NodeList childNodes = node.getChildNodes();
                        for (int j = 0; j < childNodes.getLength(); ++j) {
                            Node childNode = childNodes.item(j);
                            if (childNode instanceof Element && childNode.getNodeName() == "P") {
                                // System.out.println(childNode.getLastChild().getTextContent().trim());
                                Node test = childNode.getLastChild();
                                paper.abstractTruth += childNode.getLastChild().getTextContent().trim();
                            }
                        }
                        break;
                    case "BODY":
                        NodeList divNodes = node.getChildNodes();
                        for (int j = 0; j < divNodes.getLength(); ++j) {
                            Node divNode = divNodes.item(j);
                            if (divNode instanceof Element && divNode.getNodeName() == "DIV") {
                                // System.out.println(getTextFromDiv(divNode));
                                paper.body += getTextFromDiv(divNode);
                            }
                        }
                        break;
                }
            }
        }
        System.out.println(paper.abstractTruth);
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