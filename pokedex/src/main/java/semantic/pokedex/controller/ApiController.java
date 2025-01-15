package semantic.pokedex.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import semantic.pokedex.service.CategoryPageService;
import semantic.pokedex.service.CreateHtmlService;
import semantic.pokedex.service.MediaWikiApiService;
import semantic.pokedex.service.PokemonListService;
import semantic.pokedex.service.RDFGeneratorService;
import semantic.pokedex.service.FusekiService;
import semantic.pokedex.service.FusekiToMediaWikiService;
import semantic.pokedex.service.TemplateData;
import semantic.pokedex.service.TsvParserService;
import semantic.pokedex.service.WikitextParserService;

@RestController
public class ApiController {

    @Autowired
    FusekiService fusekiService;

    @Autowired
    private WikitextParserService parserService;

    @Autowired
    private RDFGeneratorService rdfGeneratorService;

    @Autowired
    private PokemonListService pokemonListService;

    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    @Autowired
    private CategoryPageService categoryPageService;

    @Autowired
    private TsvParserService tsvParser;

    @Autowired
    private FusekiToMediaWikiService fusekiToMediaWikiService;

    @Autowired
    private CreateHtmlService createHtmlService;

    /**
     * Endpoint to add Bulbasaur data to Fuseki.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/addBulbasaurToFuseki", produces = MediaType.TEXT_PLAIN_VALUE)
    public String addPokemon() {
        fusekiService.addBulbasaurData();
        return "Added Bulbasaur data to Fuseki.";
    }

    /**
     * Endpoint to generate Bulbasaur data in rdf.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/generateRdfBulbasaur", produces = "application/rdf+xml")
    public String generateRdfForBulbasaur() {
        try {
            String filePath = "../../../../InfoBox/Bullbasaur_Infobox.txt";
            String infoboxType = "Pokémon Infobox";
            Map<String, String> infoboxParams = parserService.parseInfobox(filePath, infoboxType) ;
            Model rdfModel = rdfGeneratorService.generateRdfForBulbasaur(infoboxParams);
            return rdfGeneratorService.serializeRDF(rdfModel);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading the wikitext file.";
        }
    }

    /**
     * Endpoint to generate rdf for all Pokémon in the Pokémon list.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/generateRdfPokemonList", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateRdfPokemonList(String infoBoxType) throws IOException {
        List<String> pokemonList = pokemonListService.getPokemonList();
        if (pokemonList.isEmpty()) {
            return "Failed to retrieve Pokémon list.";
        }
        int count = 0;
        String pokemonWikitext = "";
        for (String pokemonName : pokemonList) {

            pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
            String templateType = (infoBoxType != null) ? infoBoxType : "Pokémon Infobox";
            Map<String, String> infoboxData = parserService.extractTemplateParameters(pokemonWikitext, templateType);
            if (infoboxData != null && !infoboxData.isEmpty()) {
                rdfGeneratorService.generateInfoboxRdf("pokemon", infoboxData, infoboxData.get("name"));
            } else {
                System.out.println("No infobox found for " + pokemonName + ".");
            }

            count++;
        }

        return "RDF generation completed for " + count + " Pokémon.";
    }

    /**
     * Endpoint to generate rdf for all Pokémon in the Pokémon list.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/generateAllInfoboxForAllPokemons", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateAllInfoboxForAllPokemons() throws IOException {
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes();
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }
        // List of infoboxes to process, not all
        List<String> infoBoxes = new ArrayList<String>() {{
            // add("AbilityInfobox/header");
            // add("Infobox location");
            // add("MoveInfobox");
            add("Pokémon Infobox");
        }};
        final Map<String, String> TEMPLATE_TO_PREFIX = new HashMap<>();

        // Key = the exact template name from Bulbapedia
        // Value = the short prefix used in your triple store
        TEMPLATE_TO_PREFIX.put("Pokémon Infobox", "pokemon");
        TEMPLATE_TO_PREFIX.put("Infobox location", "location");
        TEMPLATE_TO_PREFIX.put("AbilityInfobox/header", "ability");
        TEMPLATE_TO_PREFIX.put("MoveInfobox", "move");

        List<String> listOfPages = null;
        String pageWikitext = "";
        List<TemplateData> templates = null; 
        List<TemplateData> infoBoxToParse = null;
        Map<String, String> params = null;
        for(String iterator : infoBoxes){
            // Here, we get the list of pages that use this infobox
            listOfPages = mediaWikiApiService.getPagesUsingTemplate(iterator);

            for(String page : listOfPages){
                pageWikitext = mediaWikiApiService.getPageWikitext(page);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                templates = parserService.parseTemplates(pageWikitext);
                infoBoxToParse = templates.stream().filter(t -> iterator.equalsIgnoreCase(t.getName())).collect(Collectors.toList());
                page = page.replaceAll("[/:\\\\]", "_");
                page = page.replaceAll("\\s*\\([^\\)]*\\)", "");
                // create the rdf for each infobox
                for (TemplateData t : infoBoxToParse) {
                    params = t.getParams();
                    // Here, the first argument gives us the type of template in order to correctly identify each resource
                    rdfGeneratorService.generateInfoboxRdf(TEMPLATE_TO_PREFIX.get(iterator), params, page);
                    System.out.println("RDF generated for " + page);
            }
        }
    }
    return "RDF generation completed ";
    }

    /**
     * Endpoint to generate triples for all Pages found.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/generateTriplesForAllPages", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateTriplesForAllPages() {
        Model model = mediaWikiApiService.generateTriplesForAllPages();
        fusekiService.addModel(model);
        return "Triples generated for all pages.";
    }

    /**
     * Endpoint to generate triples with the tsv file.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value="/api/generateTriplesWithTsvFile", produces = MediaType.TEXT_PLAIN_VALUE)
    public String parsingController() {
        List<String> ListOfTypes = new ArrayList<String>() {{
            add("ability");
            add("location");
            add("move");
            add("pokemon");
        }};

         try {
             
             List<Map<String,String>> tsvData = tsvParser.parseTsv("pokedex_i18n/pokedex-i18n.tsv");
             for(String type : ListOfTypes){
                 List<Map<String,String>> pokemonData = tsvParser.filterPokemonData(tsvData, type);
                 Model model = tsvParser.generateRdfForPokemonInTsvFile(pokemonData, type);
                 fusekiService.addModel(model);
             }
             return "RDF generated for all Pokémon in the TSV file.";

         } catch (IOException e) {
             e.printStackTrace();
             return "Error reading the TSV file.";
         }
    }

    /**
     * Endpoint to extract infobox from MediaWiki.
     * 
     * @return A plain text message indicating the result of the operation.
     */
    @GetMapping(value = "/api/extractInfoboxFromMediaWiki", produces = MediaType.TEXT_PLAIN_VALUE)
    public String processMediaWikiInfobox() throws IOException {
        fusekiToMediaWikiService.extractInfoboxFromMediaWiki();
        return "MediaWiki infobox processing completed.";
    }

    /**
     * Endpoint to have a turtle or an html of our results.
     * 
     * @return the html page.
     */
    @GetMapping(value = "/{type}/{name}/{acceptHeader}")
    public String getEntity(@PathVariable String type, @PathVariable String name, @PathVariable String acceptHeader) {
        if (acceptHeader.contains("turtle")) {
            return createHtmlService.createHtmlTurtleDescription(type, name);
        } else { 
            return createHtmlService.createHtmlInfobox(type, name);
        }
    }
}
