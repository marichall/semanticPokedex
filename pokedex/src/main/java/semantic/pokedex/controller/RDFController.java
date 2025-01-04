package semantic.pokedex.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import semantic.pokedex.service.CategoryPageService;
import semantic.pokedex.service.MediaWikiApiService;
import semantic.pokedex.service.PokemonListService;
import semantic.pokedex.service.RDFGeneratorService;
import semantic.pokedex.service.RDFService;
import semantic.pokedex.service.TemplateData;
import semantic.pokedex.service.WikitextParserService;

@RestController
public class RDFController {

    @Autowired
    RDFService rdfService;

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


    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String addPokemon() {
        rdfService.addData();
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
    public String generateRdfPokemonList(@RequestParam(required = false) String infoBoxType) {
        List<String> pokemonList = pokemonListService.getPokemonList();
        if (pokemonList.isEmpty()) {
            return "Failed to retrieve Pokémon list.";
        }
        int count = 0;
        String pokemonWikitext = "";
        for (String pokemonName : pokemonList) {

            System.out.println("Processing " + pokemonName);
            pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
            String templateType = (infoBoxType != null) ? infoBoxType : "Pokémon Infobox";
            Map<String, String> infoboxData = parserService.extractTemplateParameters(pokemonWikitext, templateType);
            if (infoboxData != null && !infoboxData.isEmpty()) {
                rdfGeneratorService.generateRdf(pokemonName, infoboxData);
                // System.err.println("RDF for " + pokemonName + " generated.");
            } else {
                System.out.println("No infobox found for " + pokemonName + ".");
            }

            count++;
        }

        return "RDF generation completed for " + count + " Pokémon.";
    }

    // Pas utilisé, à voir par la suite si on supprime
    @GetMapping(value = "/generateRdfInfoboxTypes", produces = "text/plain")
    public String generateRdfInfoboxTypes() {
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes();
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }
        int count = 0;
        for (String infoboxType : infoboxTypes) {
            System.out.println("Processing " + infoboxType);
            generateRdfPokemonList(infoboxType);
            count++;
        }
        return "RDF generation completed for " + count + " infobox types, for all pokemons.";
    }




    @GetMapping(value = "/generateAllInfoboxForAllPokemons", produces = "text/plain")
    public String generateAllInfoboxForAllPokemons() {
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes();
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }
        List<String> infoBoxes = new ArrayList<String>() {{
            add("BattleEInfobox");
            add("Character Infobox");
            add("MoveInfobox");
        }};
        System.err.println(infoboxTypes);

        // List<String> transcludedPages = null;
        List<String> listOfPages = null;
        String pageWikitext = "";
        List<TemplateData> templates = null; 
        List<TemplateData> infoBoxToParse = null;
        Map<String, String> params = null;
        for(String iterator : infoBoxes){
            listOfPages = mediaWikiApiService.getPagesUsingTemplate(iterator);
            System.err.println("Treating category : " + iterator + "\n\n");
            System.err.println("SIZE OF LIST OF PAGES " + listOfPages.size() + "\n\n");
            System.err.println("PAGES \n\n" + listOfPages + "\n\n");
            for(String page : listOfPages){
                System.err.println("Treating page : " + page + "\n\n");
                pageWikitext = mediaWikiApiService.getPageWikitext(page);
                templates = parserService.parseTemplates(pageWikitext);
                infoBoxToParse = templates.stream().filter(t -> iterator.equalsIgnoreCase(t.getName())).collect(Collectors.toList());
                page = page.replaceAll("[/:\\\\]", "_");
                for (TemplateData t : infoBoxToParse) {
                    params = t.getParams();
                    rdfGeneratorService.generateRdf(page, params);
            }
        }
    }
        
        
        
        // System.err.println(transcludedPages + "\n     SIZE = " + transcludedPages.size());
        // Gets the text of the main page and prints it.
        // String hiddenRuins = mediaWikiApiService.getPageWikitext(transcludedPages.get(0));
        // System.err.println("\n\n\n" + hiddenRuins);
        int countPokemon = 0;
        // For all pokemons
        // juste pour tester on prend les "limit" premiers
        // int limit = 1;
        // int processedCount = 0;
        // System.out.println("Processing " + pokemonName);
        // String pokemonWikitext = mediaWikiApiService.getPageWikitext(transcludedPages.get(1));
    //     System.err.println("THE PAGE 0 IS " + transcludedPages.get(1));
    //     List<TemplateData> templates = parserService.parseTemplates(pokemonWikitext);
    //     List<TemplateData> moveInfoBoxes = templates
    // .stream()
    // .filter(t -> "MoveInfobox".equalsIgnoreCase(t.getName()))
    // .collect(Collectors.toList());
    // Map<String, String> params = null;
    //     for (TemplateData t : moveInfoBoxes) {
    //         params = t.getParams();
    //         rdfGeneratorService.generateRdf(transcludedPages.get(1), params);
    //         System.err.println(params);
    //         // String moveName = params.get("name");
            // String power = params.get("jname");
            // System.out.println("Nom du template : " + moveName);
            // System.out.println("Paramètres       : " + power);
            // System.out.println("-----------------------------------");
        // }
        // System.err.println("THIS IS THE WIKITEXT " + pokemonWikitext);
        // System.err.println("\n\n\n\n");
        // Map<String, String> infoboxData = parserService.extractTemplateParameters(pokemonWikitext, "MoveInfobox");
        // System.err.println("THE INFO BOX IS " + infoboxData);
        // rdfGeneratorService.generateRdf(transcludedPages.get(0), infoboxData);
        // pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
        // For all infobox types
        // System.err.println(infoboxTypes);
        return "RDF generation completed for " + countPokemon + " Pokémon.";
    }
}
