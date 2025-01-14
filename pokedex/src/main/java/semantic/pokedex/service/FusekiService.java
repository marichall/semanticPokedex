package semantic.pokedex.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class FusekiService {

    String url = "http://localhost:3030/PokemonDataset";
    String sparqlEndpoint = url + "/sparql";
    String updateEndpoint = url + "/update";
    String graphStore = url + "/data";
    RDFConnection conn = RDFConnectionFactory.connect(sparqlEndpoint, updateEndpoint, graphStore);

    public void addModel(Model model) {
        try {
            // add the content of model to the triplestore
            conn.load(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBulbasaurData() {
        addModel(ModelFactory.createDefaultModel());
        // add the triple to the triplestore
        conn.update("INSERT DATA { <Bulbasaur> a <Pokemon> }");
    }

    public void addData(String subject, String object) {
        Model model = ModelFactory.createDefaultModel();

        conn.load(model); 
        conn.update("INSERT DATA { < " + subject + "> a <" + object + "> }");
    }

    public org.apache.jena.query.ResultSet executeSelectQuery(String sparqlQuery) {
        try {
            return conn.query(sparqlQuery).execSelect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error executing select query");
            return null;
        }
    }
      
}
