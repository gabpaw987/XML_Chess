package pawlowsky;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Klasse, die Methoden zur Ausgabe, Validierung, Veraenderung und persistenten
 * Speicherung von xml files bereitstellt.
 * 
 * @author Gabriel
 * @version 2011-12-04
 * 
 */

public class ChessboardXMLController {
	// Attribute: xml und xsd pfad und der pfad des output-files
	private String xmlpath;
	private String xsdpath;
	private String outputpath;
	// Attribut: DocumentBuilder welcher es ermoeglicht, ein xml Dokument als
	// Document einzulesen
	private Document document;

	/**
	 * Kostruktor
	 * 
	 * @param xmlpath
	 *            Pfad zur xml-Datei
	 * @param xsdpath
	 *            Pfad zur xsd-Datei
	 * @param outputpath
	 *            Pfad zur output-Datei
	 */
	public ChessboardXMLController(String xmlpath, String xsdpath,
			String outputpath) {
		// xml, xsd und output Pfad setzen
		this.xmlpath = xmlpath;
		this.xsdpath = xsdpath;
		this.outputpath = outputpath;
		// xml-Document mithilfe eines DocumentBuilders und des Pfades erzeugen
		try {
			this.document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(this.xmlpath);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Methode, die es ermoeglicht das xml-file mit dem xsd-file validieren zu
	 * lasen und die moegliche Fehler bei der Validierung ausgibt.
	 */
	public void validate() throws ParserConfigurationException, SAXException,
			IOException {
		// Schema-Validator mithilfe einer Schmeafcactory, die ein Schema aus
		// dem xsd erstellt und daraus einen Validator erzeugt
		Validator validator = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema")
				.newSchema(new File(xsdpath)).newValidator();

		// Erzeugt ein DOM(Document Object Model) des xml-files, zur spaeteren
		// validierung
		DOMSource source = new DOMSource(document);

		// Validierung durchfuehren und anschliessende Ausgabe, dass
		// funktioniert hat, oder was fuer ein Fehler aufgetreten ist
		try {
			validator.validate(source);
			System.out.println(xmlpath + " is valid!");
		} catch (SAXException ex) {
			System.out.println(xmlpath + " is not valid because ");
			System.out.println(ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Methode, die es ermoeglicht das gespeicherte xml-Dokument in halbwegs
	 * ansehbarer Qualitaet auszugeben.
	 */
	public void print() throws SAXException, IOException {
		// Gibt alle Elemente des xmls anhand des Tags "POSITION" als NodeList
		// zurueck
		NodeList positions = document.getElementsByTagName("POSITION");

		// Schleife die alle Elemente durchgeht
		for (int i = 0; i < positions.getLength(); i++) {
			// Speichert das aktuelle Element zuerst mit seiner position
			Element position = (Element) positions.item(i);
			// dann mit seiner art(king, queen, pawn, ...)
			Element piece = (Element) position.getParentNode();
			// und dann mit seiner farbe
			Element pieces = (Element) piece.getParentNode();
			// Gibt die farbe, dann die art und dann die Position des Elements
			// aus
			System.out
					.println((pieces.getTagName().equals("WHITEPIECES") ? "White "
							: "Black ")
							+ piece.getTagName().toLowerCase()
							+ ": "
							+ position.getAttribute("COLUMN")
							+ position.getAttribute("ROW"));
		}
	}

	/**
	 * Methode, die eine Figur von einer uebergebenen startposition auf eine
	 * uebergebene endposition im Document Attribut setzt
	 * 
	 * @param startPos
	 *            Position der Figur zu Beginn
	 * @param endPos
	 *            Position der Figur nach dem umsetzen
	 */
	public void changeFigure(String startPos, String endPos)
			throws SAXException, IOException,
			TransformerFactoryConfigurationError, TransformerException {
		// Gibt alle Elemente des xmls anhand des Tags "POSITION" als NodeList
		// zurueck
		NodeList positions = document.getElementsByTagName("POSITION");

		// Schleife die alle Elemente durchgeht
		for (int i = 0; i < positions.getLength(); i++) {
			// Speichert die position des aktuellen Elements
			Element position = (Element) positions.item(i);
			// Wenn man beim richtigen Element angelangt ist...
			if ((position.getAttribute("COLUMN") + position.getAttribute("ROW"))
					.equals(startPos.toUpperCase())) {
				// wird die Position auf die neue Position geaendert
				position.setAttribute("COLUMN",
						("" + endPos.charAt(0)).toUpperCase());
				position.setAttribute("ROW",
						("" + endPos.charAt(1)).toUpperCase());
			}
		}
		// Nachdem die Position der Figur gesetzt wurde muss ueberprueft werden,
		// ob der Zug auf ein legitimes Feld geschehen ist, ist er das nicht
		// wird er nicht durchgefuehrt
		try {
			// Validierung durchfuehren, falls dies nicht mehr moeglich ist,
			// muss der Zug rewidiert werden
			validate();
		} catch (SAXException e) {
			// Ausgabe, dass es nicht funktioniert hat
			System.out.println("Change cannot be done because it´s invalid!");
			try {
				// Das Dokument wieder auf das urspruengliche XML setzen
				this.document = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder().parse(this.xmlpath);
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Methode, die die moeglicherweise zuvor vollzogene Aenderungen durch z.B.
	 * changeFigure in ein neues Output-XML File schreibt.
	 */
	public void writeXML() {
		// Speichert das DOM des wahrscheinlich schon veraenderten xmls(als
		// document) als sorce
		Source source = new DOMSource(this.document);
		// und das aktuelle output file als StreamResult, damit es veraendert
		// werden kann
		StreamResult result = new StreamResult(new File(this.outputpath));

		// Erzeugt einen Transformer und aendert damit die Datei von der
		// alten auf die neue geaenderte mit der neuen Position
		try {
			TransformerFactory.newInstance().newTransformer()
					.transform(source, result);
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerFactoryConfigurationError tcee) {
			tcee.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}
	}
}