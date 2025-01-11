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
            conn.load(model);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addBulbasaurData() {
        addModel(ModelFactory.createDefaultModel());
        conn.update("INSERT DATA { <Bulbasaur> a <Pokemon> }"); // add the triple to the triplestore
    }

    public void addData(String subject, String object) {
        Model model = ModelFactory.createDefaultModel();

        conn.load(model); // add the content of model to the triplestore
        conn.update("INSERT DATA { < " + subject + "> a <" + object + "> }"); // add the triple to the triplestore
    }

    // public void addData(String subject, String predicate, String object) {
    //     conn.querySelect(null, null);
    // }

    // Je pense qu'il faudrait faire une requete savoir si la valeur existe deja avant de l'inserer
    //public void selectPokemon(List<Map<String,String>> englishPokemonName) {
    //    for(int i=0; i<2; i++){
    //        Map<String, String> row = englishPokemonName.get(i);
    //        String pokemonName = row.get("label");
    //        String pokemonId = row.get("id");
    //        String sparqlQuery = "SELECT ?s WHERE { ?s <http://localhost:8080/pokemon/name> '" + pokemonName + "' }";
    //        System.out.println(sparqlQuery);
    //        
    //        QueryExecution queryExecution = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
    //        
    //        try {
    //            
    //
    //        } catch (Exception e) {
    //            System.err.println("Erreur lors de l'exécution de la requête SPARQL : " + e.getMessage());
    //        } finally {
    //            // Libérer les ressources
    //            queryExecution.close();
    //        }
//
    //        
    //    }
    //}
      
}
