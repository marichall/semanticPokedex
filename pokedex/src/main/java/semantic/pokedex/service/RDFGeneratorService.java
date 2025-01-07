package semantic.pokedex.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;
import java.io.File;

@Service
public class RDFGeneratorService {
    String exNS = "http://example.org/pokemon/";
    String schemaNS = "http://schema.org/";

    @Autowired
    private RDFService rdfService;

    /**
     * Génère un modèle RDF depuis des paramètres dans l'infobox.
     *
     * @param infoboxParams Une map des propriétés contenues dans l'infobox.
     * @return Modèle Jena Model représentant le graphe RDF.
     */
    public Model generateRdfForBulbasaur(Map<String, String> infoboxParams) {
        Model model = ModelFactory.createDefaultModel();

        // Définition de préfix (pour l'instant ça n'a aucun sens)
        
        model.setNsPrefix("ex", exNS);
        model.setNsPrefix("schema", schemaNS);

        // Create a resource for Bulbasaur
        Resource bulbasaur = model.createResource(exNS + "Bulbasaur")
                .addProperty(RDF.type, model.createResource(schemaNS + "Thing"))
                .addProperty(model.createProperty(schemaNS + "name"), "Bulbasaur");

        // Map infobox parameters to RDF properties
        for (Map.Entry<String, String> entry : infoboxParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Property property = model.createProperty(exNS, key);

            bulbasaur.addProperty(property, value);
        }

        return model;
    }

    /**
     * Sérialise le modèle RDF en XML.
     *
     * @param model Modèle Jena.
     * @return Représentation en XML.
     */
    public String serializeRDF(Model model) {
        StringWriter out = new StringWriter();
        model.write(out, "RDF/XML");
        return out.toString();
    }


    public void generatePokemonInfoboxRdf(String pokemonName, Map<String, String> infoboxData) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("pokemon", exNS);
        model.setNsPrefix("rdf", RDF.getURI());

        Resource pokemonResource = model.createResource(exNS + encodeName(pokemonName));
        // Boucle sur l'info box et les propriétés
        for (Map.Entry<String, String> entry : infoboxData.entrySet()) {
            String key = entry.getKey().replace(" ", "_").toLowerCase();
            String value = entry.getValue();

            Property property = model.createProperty(exNS, key);
            // For simplicity, all values are literals. Enhance by defining proper types or linking to other resources.
            pokemonResource.addProperty(property, value);
        }
        // String filePath = "InfoBox/" + encodeName(pokemonName) + ".ttl";
        // Écriture du parsing de l'infobox dans un fichier ttl (pour l'instant)
        rdfService.addModel(model); // Appel à une méthode pour ajouter le modèle dans Fuseki
    }

    private String encodeName(String name) {
        return name.replace(" ", "_");
    }
}