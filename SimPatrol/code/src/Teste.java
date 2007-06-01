import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import model.agent.ClosedSociety;
import model.graph.Graph;
import org.xml.sax.SAXException;
import control.parser.GraphTranslator;
import control.simulator.CycledSimulator;
import control.simulator.RealTimeSimulator;

public class Teste {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {		
		Graph grafo = GraphTranslator.getGraph("c:/teste.txt");
		System.out.println(grafo.toXML(0));		
		
		// trecho do simulador de tempo real...
		//RealTimeSimulator simulator = new RealTimeSimulator(600, grafo, null, null);
		//simulator.startSimulation();
		
		// trecho do simulador em ciclos...
		ClosedSociety soc = new ClosedSociety();
		CycledSimulator simulator = new CycledSimulator(60, grafo, null, null);
		simulator.addSociety(soc);
		soc.start();
		simulator.startSimulation();
		//soc.stop();				
	}

}
