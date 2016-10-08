package pawlowsky;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Klasse, die mithilfe der Controller-Klasse ChessboardXMLController, soll
 * zuerst das XML mit dem XSD file eines Schachbretts validiert werden,
 * anschliessend wird das aktuelle Schachbrett ausgegeben, geaendert und neu
 * ausgegeben.
 * 
 * @author Gabriel Pawlowsky
 * @version 2011-12-04
 * 
 */
public class ChessboardMain {

	/**
	 * main-Methode
	 * 
	 * @param args
	 *            Konsolen-parameter, xml, xsd und ein optionales
	 *            Output-xml-file
	 */
	public static void main(String[] args) throws SAXException, IOException,
			TransformerException, ParserConfigurationException {
		// xml, xsd und Output-xml-file Pfade werden hier hinein gespeichert
		String xsdfile = "";
		String xmlfile = "";
		String outputfile = "";
		if (args.length != 2 && args.length != 3) {
			// Standard-werte, die gesetzt werden, wenn der Benutzer falsche
			// oder keine Konsolenparameter uebergibt
			System.err
					.println("Works like this: \nfile.jar input.xml schema.xsd [output.xml]");
			System.exit(1);
		} else if (args.length == 2) {
			// Setzen der werte fuer den xml und xsd Pfad
			xmlfile = args[0];
			xsdfile = args[1];
			// Bennen des Output-files mit "output.xml" falls die Moeglichkeit
			// es im Parameter zu setzen nicht wahrgenommen wurde
			outputfile = "output.xml";
		} else {
			xmlfile = args[0];
			xsdfile = args[1];
			outputfile = args[2];
		}

		// Erzeugen des Controllers, der das Schachbrett XML steuert
		ChessboardXMLController controller = new ChessboardXMLController(
				xmlfile, xsdfile, outputfile);

		// Validieren des XML mit dem Schema und gegebenenfalls fangen der
		// Exceptions
		try {
			controller.validate();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// Zeichnen des derzeitigen Schachbretts und gegebenfalls fangen der
		// Exceptions
		try {
			System.out.println("Chessboard in the beginning:");
			controller.print();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// Eingabe des Spielzugs durch den Benutzer ermoeglichen
		System.out.println("starting point (letter and number)");
		String startPos = new BufferedReader(new InputStreamReader(System.in))
				.readLine();
		System.out.println("endpoint (letter and number)");
		String endPos = new BufferedReader(new InputStreamReader(System.in))
				.readLine();

		// Durchfuehren des Spielzuges, erneute Ausgabe des Schachbretts und
		// gegebenenfalls abfangen der Exceptions
		try {
			System.out.println("After setting " + startPos.charAt(0)
					+ startPos.charAt(1) + " to " + endPos.charAt(0)
					+ endPos.charAt(1));
			controller.changeFigure(startPos, endPos);
			controller.writeXML();
			controller.print();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}