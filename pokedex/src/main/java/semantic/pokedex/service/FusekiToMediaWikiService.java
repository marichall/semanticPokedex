package semantic.pokedex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FusekiToMediaWikiService {

    @Autowired
    private FusekiService fusekiService;

    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    @Autowired
    private WikitextParserService wikitextParserService;

    @Autowired
    private RDFGeneratorService rdfGeneratorService;

    public void processMediaWikiInfobox() throws IOException {
        String predicate = "http://schema.org/mainEntityOfPage";
        String templateType = "";

        // Étape 1 : Récupérer les pages depuis Fuseki
        String sparqlQuery = String.format(
            "SELECT ?url WHERE { ?s <%s> ?url }", predicate
        );
        ResultSet results = fusekiService.executeSelectQuery(sparqlQuery);
        List<String> pagesUrl = new ArrayList<>();
        Map<String, String> infoboxData;

        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();
            String url = solution.get("url").toString();
            pagesUrl.add(url);
        }

        for (String pageUrl : pagesUrl) {
            System.out.println("Processing page: " + pageUrl);
            String pageTitle = pageUrl.substring(pageUrl.lastIndexOf("/") + 1);

            System.out.println("Processing page: " + pageTitle);

            // Étape 2 : Appeler l'API MediaWiki pour récupérer le wikitext
            String wikitext = mediaWikiApiService.getPageWikitext(pageTitle, "&prop=wikitext|templates|images|links");
            infoboxData = null;
            if(wikitext.contains("{{Pokémon Infobox")) {
                templateType = "Pokémon Infobox";
                System.out.println("111111111111111111111");
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, templateType);

            }else if(wikitext.contains("{{RegionInfobox")) {
                templateType = "RegionInfobox";
                System.out.println("222222222222222222222");
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, templateType);

            }
            else if(wikitext.contains("{{AbilityInfobox")) {
                templateType = "AbilityInfobox";
                System.out.println("333333333333333333333");
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, templateType);

            }
            else if(wikitext.contains("{{MoveInfobox")) {
                templateType = "MoveInfobox";
                System.out.println("444444444444444444444");
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, templateType);

            } 
                
            

            System.out.println("infoboxData =" + infoboxData);
            if (infoboxData != null && !infoboxData.isEmpty()) {
                String pokemonName = infoboxData.get("name");
                
                rdfGeneratorService.generatePokemonInfoboxRdf(pageTitle, infoboxData, pageTitle);
            }
        }
    }
    
}
