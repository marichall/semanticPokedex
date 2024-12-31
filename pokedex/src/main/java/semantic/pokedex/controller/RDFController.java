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
            String filePath = "../../../../InfoBox/Bullbasaur_Infobox.txt";
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

    @GetMapping(value = "/generateAllRdf", produces = "text/plain")
    public String generateAllRdf() {
        // Étape 1 : Récupérer la liste des types d'infobox
        List<String> infoboxTypes = categoryPageService.getInfoboxTypes("Category:Infobox templates");
        if (infoboxTypes.isEmpty()) {
            return "Failed to retrieve infobox types.";
        }

        int totalCount = 0;
        StringBuilder report = new StringBuilder();

        // Étape 2 : Traiter chaque type d'infobox
        for (String infoboxType : infoboxTypes) {
            report.append("Processing infobox type: ").append(infoboxType).append("\n");

            // Étape 3 : Récupérer les pages associées à ce type d'infobox
            List<String> pageTitles = mediaWikiApiService.getPagesUsingTemplate(infoboxType);
            if (pageTitles.isEmpty()) {
                report.append("No pages found for infobox type: ").append(infoboxType).append("\n");
                continue;
            }

            int typeCount = 0;

            // Étape 4 : Parcourir chaque page et extraire les données
            for (String pageTitle : pageTitles) {
                System.out.println("Processing page: " + pageTitle);

                // Récupérer le wikitext de la page
                String wikitext = pokemonListService.getPokemonInfoBox(pageTitle);
                Map<String, String> infoboxData = parserService.extractTemplateParameters(wikitext, infoboxType);

                if (infoboxData != null && !infoboxData.isEmpty()) {
                    // Générer le RDF pour cette page
                    rdfGeneratorService.generateRdf(pageTitle, infoboxData);
                    typeCount++;
                } else {
                    System.out.println("No infobox data found for page: " + pageTitle);
                }
            }

            totalCount += typeCount;
            report.append("Completed processing for infobox type: ").append(infoboxType)
                .append(" (").append(typeCount).append(" pages processed)\n");
        }

        return "RDF generation completed for " + totalCount + " pages.\n" + report.toString();
    }

}
