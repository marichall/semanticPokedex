package semantic.pokedex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public void extractInfoboxFromMediaWiki() throws IOException {
        String predicate = "http://schema.org/mainEntityOfPage";

        // Retrieve the URLs of the pages that have our predicate
        String sparqlQuery = String.format(
            "SELECT ?url WHERE { ?s <%s> ?url }", predicate
        );
        ResultSet results = fusekiService.executeSelectQuery(sparqlQuery);
        List<String> pagesUrl = new ArrayList<>();
        Map<String, String> infoboxData;

        // Storing our URLs in a list
        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();
            String url = solution.get("url").toString();
            pagesUrl.add(url);
        }

        // Extraction of the desired infoboxes
        for (String pageUrl : pagesUrl) {
            String pageTitle = pageUrl.substring(pageUrl.lastIndexOf("/") + 1);

            String wikitext = mediaWikiApiService.getPageWikitext(pageTitle, "&prop=wikitext|templates|images|links");
            infoboxData = null;
            if(wikitext.contains("{{Pokémon Infobox")) {
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, "Pokémon Infobox");

            }else if(wikitext.contains("{{RegionInfobox")) {
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, "RegionInfobox");

            }
            else if(wikitext.contains("{{AbilityInfobox")) {
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, "AbilityInfobox");

            }
            else if(wikitext.contains("{{MoveInfobox")) {
                infoboxData = wikitextParserService.extractTemplateParameters(wikitext, "MoveInfobox");

            } 
                
            // RDF generation
            if (infoboxData != null && !infoboxData.isEmpty()) {
                String pageName = mediaWikiApiService.encodeTitle(pageTitle);
                String regex = "^(.*?)_\\((.*?)\\)$";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(pageName);

                if (matcher.matches()) {
                    String valueType = matcher.group(1); 
                    String type = matcher.group(2);
                                     
                    rdfGeneratorService.generateInfoboxRdf(type, infoboxData, valueType);
                }
            }
        }
    }
    
}
