package semantic.pokedex.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        // For testing, limit to first 10 Pokémon
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

    @GetMapping(value = "/generateAllInfoboxForAllPokemons", produces = "text/turtle")
    public String generateAllInfoboxForAllPokemons() {
        List<String> pokemonList = pokemonListService.getPokemonList();
        if (pokemonList.isEmpty()) {
            return "Failed to retrieve Pokémon list.";
        }
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes();
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }
        int countPokemon = 0;
        int countInfoboxType = 0;
        String pokemonWikitext = "";
        // For all pokemons
        // juste pour tester on prend les 10 premiers
        int limit = 1;
        int processedCount = 0;
        for (String pokemonName : pokemonList) {
            if (processedCount >= limit) {
                break;
            }
            // System.out.println("Processing " + pokemonName);
            Map<String, String> infoboxData = null;
            countInfoboxType = 0;
            pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
            // For all infobox types
            for (String infoboxType : infoboxTypes) {
                Map<String, String> currentInfoboxData = parserService.extractTemplateParameters(pokemonWikitext, infoboxType);
                if (currentInfoboxData != null && !currentInfoboxData.isEmpty()) {
                    if (infoboxData == null) {
                        infoboxData = currentInfoboxData;
                    } else {
                        infoboxData.putAll(currentInfoboxData);
                    }
                    countInfoboxType++;
                    System.out.println("Infobox " + infoboxType + " found for " + pokemonName + ".");
                } else {
                    // System.out.println("No infobox " + infoboxType + " found for " + pokemonName + ".");
                }
            }
            if(countInfoboxType == 0 || infoboxData == null){
                // System.out.println("Any infobox found for " + pokemonName + ".");
            }else{
                rdfGeneratorService.generateRdf(pokemonName, infoboxData);
                // System.out.println("RDF generation completed for " + countInfoboxType + " infobox types for " + pokemonName + ".");
            }
            System.out.println("RDF generation completed for " + countInfoboxType + " infobox types for " + pokemonName + ".");
            countPokemon++;
            processedCount++;
        }

        return "RDF generation completed for " + countPokemon + " Pokémon.";
    }
}
