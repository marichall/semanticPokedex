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


    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    public String addPokemon() {
        rdfService.addData();
        return "D";
    }

    @GetMapping(value = "/generate-rdf", produces = "application/rdf+xml")
    public String generateRDF() {
        try {
            String filePath = "/home/hany/FAC/M2/S9/SemanticWeb/Projet/semanticWeb/pokedex/InfoBox/Bullbasaur_Infobox.txt";
            Map<String, String> infoboxParams = parserService.parseInfobox(filePath);
            Model rdfModel = rdfGeneratorService.generateRDF(infoboxParams);
            return rdfGeneratorService.serializeRDF(rdfModel);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading the wikitext file.";
        }
    }


    @GetMapping(value = "/generateRdf", produces = "text/plain")
    public String generateRdf() {
        List<String> pokemonList = pokemonListService.getPokemonList();
        if (pokemonList.isEmpty()) {
            return "Failed to retrieve Pokémon list.";
        }
        int count = 0;
        // For testing, limit to first 10 Pokémon
        String pokemonWikitext = "";
        for (String pokemonName : pokemonList) {

            System.out.println("Processing " + pokemonName);
            pokemonWikitext = pokemonListService.getPokemonInfoBox(pokemonName);
            Map<String, String> infoboxData = parserService.extractTemplateParameters(pokemonWikitext, "Pokémon Infobox");
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
}
