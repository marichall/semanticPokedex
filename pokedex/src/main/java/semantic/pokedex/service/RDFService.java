package semantic.pokedex.service;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.vocabulary.VCARD;
import org.springframework.stereotype.Service;

@Service
public class RDFService {

    String url = "http://localhost:3030/PokemonDataset";
    String sparqlEndpoint = url + "/sparql";
    String updateEndpoint = url + "/update";
    String graphStore = url + "/data";
    RDFConnection conn = RDFConnectionFactory.connect(sparqlEndpoint, updateEndpoint, graphStore);

    public void addData() {
        Model model = ModelFactory.createDefaultModel();

        conn.load(model); // add the content of model to the triplestore
        conn.update("INSERT DATA { <Bulbasaur> a <Pokemon> }"); // add the triple to the triplestore
    }

    public void addData(String subject, String object) {
        Model model = ModelFactory.createDefaultModel();

        conn.load(model); // add the content of model to the triplestore
        conn.update("INSERT DATA { < " + subject + "> a <" + object + "> }"); // add the triple to the triplestore
    }
}
