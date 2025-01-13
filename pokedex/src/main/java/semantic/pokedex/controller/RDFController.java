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
import org.springframework.web.bind.annotation.RestController;

import semantic.pokedex.service.CategoryPageService;
import semantic.pokedex.service.MediaWikiApiService;
import semantic.pokedex.service.PokemonListService;
import semantic.pokedex.service.RDFGeneratorService;
import semantic.pokedex.service.FusekiService;
import semantic.pokedex.service.FusekiToMediaWikiService;
import semantic.pokedex.service.TemplateData;
import semantic.pokedex.service.TsvParserService;
import semantic.pokedex.service.WikitextParserService;

@RestController
public class RDFController {

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

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String addPokemon() {
        fusekiService.addBulbasaurData();
        return "D";
    }

    @GetMapping(value = "/generateRdfBulbasaur", produces = "application/rdf+xml")
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


    @GetMapping(value = "/generateRdfPokemonList", produces = "text/plain")
    public String generateRdfPokemonList(String infoBoxType) throws IOException {
        List<String> pokemonList = pokemonListService.getPokemonList();
        if (pokemonList.isEmpty()) {
            return "Failed to retrieve Pokémon list.";
        }
        int count = 0;
        String pokemonWikitext = "";
        for (String pokemonName : pokemonList) {

            // System.out.println("Processing " + pokemonName);
            pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
            String templateType = (infoBoxType != null) ? infoBoxType : "Pokémon Infobox";
            Map<String, String> infoboxData = parserService.extractTemplateParameters(pokemonWikitext, templateType);
            if (infoboxData != null && !infoboxData.isEmpty()) {
                rdfGeneratorService.generatePokemonInfoboxRdf(pokemonName, infoboxData, infoboxData.get("name"));
            } else {
                System.out.println("No infobox found for " + pokemonName + ".");
            }

            count++;
        }

        return "RDF generation completed for " + count + " Pokémon.";
    }

    @GetMapping(value = "/generateAllInfoboxForAllPokemons", produces = "text/plain")
    public String generateAllInfoboxForAllPokemons() throws IOException {
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes();
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }
        //Liste des infobox, pour pas toutes les faire
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
        System.err.println(infoboxTypes);

        List<String> listOfPages = null;
        String pageWikitext = "";
        List<TemplateData> templates = null; 
        List<TemplateData> infoBoxToParse = null;
        Map<String, String> params = null;
        // Première boucle : boucle sur la liste des infobox
        for(String iterator : infoBoxes){
            // Ici, on récupère la liste des pages qui utilisent cette infobox
            listOfPages = mediaWikiApiService.getPagesUsingTemplate(iterator);

            // System.err.println("Treating category : " + iterator + "\n\n");
            // System.err.println("SIZE OF LIST OF PAGES " + listOfPages.size() + "\n\n");
            // System.err.println("PAGES \n\n" + listOfPages + "\n\n");

            // 2eme boucle :  boucle sur la liste des pages.
            for(String page : listOfPages){
                // System.err.println("Treating page : " + page + "\n\n");
                pageWikitext = mediaWikiApiService.getPageWikitext(page);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                templates = parserService.parseTemplates(pageWikitext);
                infoBoxToParse = templates.stream().filter(t -> iterator.equalsIgnoreCase(t.getName())).collect(Collectors.toList());
                page = page.replaceAll("[/:\\\\]", "_");
                // 3eme boucle : Pour chaque page, on récupère les paramètre de l'infobox, et on parse.
                for (TemplateData t : infoBoxToParse) {
                    params = t.getParams();
                    rdfGeneratorService.generatePokemonInfoboxRdf(TEMPLATE_TO_PREFIX.get(iterator), params, page); //here, the first argument gives us the type of template in order to correctly identify each resource
            }
        }
    }
    return "RDF generation completed ";
    }

    @GetMapping(value = "/generateTriplesForAllPages", produces = "text/plain")
    public String generateTriplesForAllPages() {
        Model model = mediaWikiApiService.generateTriplesForAllPages();
        fusekiService.addModel(model);
        return "Triples generated for all pages.";
    }


    @GetMapping(value="/generateTriplesWithTsvFile", produces = "text/plain")
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
                 Model model = tsvParser.generateRdfForPokemonInTsvFile(pokemonData, "ability");
                 fusekiService.addModel(model);
                 return "RDF generated for all Pokémon in the TSV file.";
             }

         } catch (IOException e) {
             e.printStackTrace();
             return "Error reading the TSV file.";
         }
         return "RDF generated for all Pokémon in the TSV file.";
    }

    @GetMapping(value = "/processMediaWikiInfobox", produces = "text/plain")
    public String processMediaWikiInfobox() throws IOException {
        fusekiToMediaWikiService.processMediaWikiInfobox();
        return "MediaWiki infobox processing completed.";
    }

}
