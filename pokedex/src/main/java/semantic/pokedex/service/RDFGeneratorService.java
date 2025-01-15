package semantic.pokedex.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Service
public class RDFGeneratorService {
    String exNS = "http://localhost:8080/";
    String schemaNS = "http://schema.org/";

    @Autowired
    private FusekiService fusekiService;

    
    // Generates an RDF model from infobox parameters.
    public Model generateRdfForBulbasaur(Map<String, String> infoboxParams) {
        Model model = ModelFactory.createDefaultModel();
        
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

    // Serialize RDF model to XML
    public String serializeRDF(Model model) {
        StringWriter out = new StringWriter();
        model.write(out, "RDF/XML");
        return out.toString();
    }

    // Generate RDF for a Pokemon and add it to the Fuseki dataset
    public void generateInfoboxRdf(String prefix, Map<String, String> infoboxData, String pageName) throws IOException {
        MyWikiModel wikiModel = new MyWikiModel(
        "", 
        "https://bulbapedia.bulbagarden.net/wiki/${title}"
        );

        Model model = ModelFactory.createDefaultModel();
        String namespace = exNS + prefix + "/"; 
        model.setNsPrefix(prefix, namespace);
        model.setNsPrefix("rdf", RDF.getURI());

        Resource pokemonResource = model.createResource(namespace + encodeName(pageName));
        for (Map.Entry<String, String> entry : infoboxData.entrySet()) {
            String key = entry.getKey().replace(" ", "_").toLowerCase();
            String rawValue = entry.getValue(); 
    
            String renderedHtml = wikiModel.render(rawValue);
            Document doc = Jsoup.parse(renderedHtml);
    
            for (Element a : doc.select("a")) {
                String href = a.attr("href"); 
                a.text(href); 
            }
    
            String replacedText = doc.text();
    
            Property property = model.createProperty(exNS, key);
            pokemonResource.addProperty(property, replacedText);
        }
        fusekiService.addModel(model);
    }

    public String encodeName(String name) {
        return name.replace(" ", "_");
    }

    // private static String extractTextBetweenParentheses(String input) {
    //     Pattern pattern = Pattern.compile("\\((.*?)\\)");
    //     Matcher matcher = pattern.matcher(input);
    // 
    //     // Vérifier si une correspondance est trouvée
    //     if (matcher.find()) {
    //         return matcher.group(1); // Retourne le contenu capturé
    //     }
    // 
    //     return null;
    // }
}