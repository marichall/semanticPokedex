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

    private final Dataset dataset;
    String url = "http://localhost:3030/PokemonDataset";
    String sparqlEndpoint = url + "/sparql";
    String updateEndpoint = url + "/update";
    String graphStore = url + "/data";
    RDFConnection conn = RDFConnectionFactory.connect(sparqlEndpoint, updateEndpoint, graphStore);
    
    public RDFService(Dataset dataset) {
        this.dataset = dataset;
    }

    public void addData() {
        Model model = ModelFactory.createDefaultModel();

        Resource bulbasaur = model.createResource("http://example.org/pokemon/Bulbasaur")
                .addProperty(VCARD.FN, "Bulbasaur")
                .addProperty(VCARD.NICKNAME, "Seed Pokémon");

        dataset.begin();
        try {
            dataset.getDefaultModel().add(model);
            dataset.commit();
        } catch (Exception e) {
            dataset.abort();
            throw e;
        } finally {
            dataset.end();
        }
    }
}
