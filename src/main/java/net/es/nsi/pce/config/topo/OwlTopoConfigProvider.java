package net.es.nsi.pce.config.topo;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.es.nsi.pce.config.OwlConfigProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class OwlTopoConfigProvider extends OwlConfigProvider {

	static String NSNetwork = "http://www.glif.is/working-groups/tech/dtox#NSNetwork";
	static String HASSTP = "http://www.glif.is/working-groups/tech/dtox#hasSTP";
	static String CONNECTEDTO = "http://www.glif.is/working-groups/tech/dtox#connectedTo";

	private HashMap<String, TopoNetworkConfig> configs = new HashMap<String, TopoNetworkConfig>();
	private Model model = ModelFactory.createDefaultModel();

	static OwlTopoConfigProvider instance;

	private OwlTopoConfigProvider() {

	}

	public static OwlTopoConfigProvider getInstance() {
		if (instance == null) {
			instance = new OwlTopoConfigProvider();
		}
		return instance;
	}

	/*
	 * Takes the URI of a STP and generates TopoStpConfig object.
	 */
	private TopoStpConfig parseHasSTP(Resource subject, String net) {

		Selector selector = new SimpleSelector(subject, null, (RDFNode) null);
		StmtIterator iter = model.listStatements(selector);
		if (iter.hasNext() == false) {
			return null;
		}

		TopoStpConfig stp = new TopoStpConfig();
		String stpID = subject.toString();
		stp.localId = stpID; // stpID.substring(stpID.lastIndexOf(":") + 1,
								// stpID.length());

		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();

			if (predicate.toString().equalsIgnoreCase(CONNECTEDTO)) {
				String remoteID = object.toString();
				stp.remoteLocalId = remoteID;

				// Find the remote network id of the STP
				Selector remoteSelector = new SimpleSelector(null,
						ResourceFactory.createProperty(HASSTP), object);
				StmtIterator remoteIter = model.listStatements(remoteSelector);
				if (remoteIter.hasNext()) {
					stp.remoteNetworkId = remoteIter.nextStatement()
							.getSubject().toString();
				}

			} else {
				// TODO (AH): Log unparsed predicates.
				// System.out.println(stmt.getSubject() + " " +
				// stmt.getPredicate().toString() + " " +
				// stmt.getObject().toString());
			}
		}
		return stp;
	}

	/*
	 * Takes the URI of a NSNetwork and generates TopoNetworkConfig object.
	 */
	private TopoNetworkConfig parseNetwork(Resource subject) {
		TopoNetworkConfig net = new TopoNetworkConfig();
		net.stps = new HashSet<TopoStpConfig>();

		Selector selector = new SimpleSelector(subject,
				ResourceFactory.createProperty(HASSTP), (RDFNode) null);
		StmtIterator iter = model.listStatements(selector);
		while (iter.hasNext()) {
			Statement stmt = iter.nextStatement();
			TopoStpConfig stp = parseHasSTP(stmt.getObject().asResource(),
					subject.toString());
			if (stp != null) {
				net.stps.add(stp);
			}
		}

		return net;
	}

	public void loadConfig() throws Exception {
		// System.out.println("loading "+this.getFilename());

		File configFile = new File(this.getFilename());
		if (isFileUpdated(configFile)) {
			InputStream in = FileManager.get().open(this.getFilename());

			model.read(in, null);
			Selector selector = new SimpleSelector(null, null,
					ResourceFactory.createResource(NSNetwork));
			StmtIterator iter = model.listStatements(selector);
			while (iter.hasNext()) {
				Resource subject = iter.nextStatement().getSubject();
				TopoNetworkConfig net = parseNetwork(subject);
				if (net != null) {
					configs.put(subject.toString(), net);
				}
			}
		}

	}

	public TopoNetworkConfig getConfig(String networkId) {
		return configs.get(networkId);
	}

	public Set<String> getNetworkIds() {
		return configs.keySet();
	}

}
