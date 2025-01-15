package semantic.pokedex.service;

import java.util.*;

import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MediaWikiApiService {

    private static final String API_ENDPOINT = "https://bulbapedia.bulbagarden.net/w/api.php";
    public final String URI = "http://localhost:8080"; 
    private String bullbapediaWikiUrl = "https://bulbapedia.bulbagarden.net/wiki"; 

    @Autowired
    private RestTemplate restTemplate;

    // Get the wikitext of a page
    public String getPageWikitext(String pageTitle) {
        String url = API_ENDPOINT + "?action=parse&format=json&page=" + 
                     encodeTitle(pageTitle) + "&prop=wikitext";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Processing the API response
        // The response is in JSON, we use Jackson ObjectMapper to read the response
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            return root.path("parse").path("wikitext").path("*").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Same as getPageWikitext but with a prop parameter
    public String getPageWikitext(String pageTitle, String prop) {
        String url = API_ENDPOINT + "?action=parse&format=json&page=" + 
                     encodeTitle(pageTitle) + prop;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            return root.path("parse").path("wikitext").path("*").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Get the wikitext of a pokemon page
    public String getPokemonPageWikitext(String pokemonName) {
        String pageTitle = pokemonName + "_(Pokémon)";
        return getPageWikitext(pageTitle);
    }

    // get pages with a category name
    public List<String> getPagesInCategory(String category) {
        String url = API_ENDPOINT + "?action=query&list=categorymembers&cmtitle=" 
                        + category + "&cmlimit=max&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        try {    
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
    
            com.fasterxml.jackson.databind.JsonNode members = root.path("query").path("categorymembers");
            if (members.isMissingNode()) {
                return new ArrayList<>();
            }
    
            List<String> pages = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode member : members) {
                String title = member.path("title").asText();
                pages.add(title);
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Get all pages with template name and continue param (recursive function)
    private List<String> getPagesUsingTemplateWithContinue(String templateName, String eicontinue) {
        String url = API_ENDPOINT + "?action=query&list=backlinks&bltitle=Template:" 
                     + encodeTitle(templateName) + "&eilimit=max&eicontinue=" 
                     + eicontinue + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<String> pages = new ArrayList<>();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());

            com.fasterxml.jackson.databind.JsonNode backlinks = root.path("query").path("embeddedin");
            if (!backlinks.isMissingNode()) {
                for (com.fasterxml.jackson.databind.JsonNode backlink : backlinks) {
                    String title = backlink.path("title").asText();
                    pages.add(title);
                }
            }

            // Get the next page if exist
            com.fasterxml.jackson.databind.JsonNode cont = root.path("continue").path("eicontinue");
            if (!cont.isMissingNode()) {
                pages.addAll(getPagesUsingTemplateWithContinue(templateName, cont.asText()));
            }

            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return pages;
        }
    }

    // Get all pages using a template
    public List<String> getPagesUsingTemplate(String templateName) {
        String url = API_ENDPOINT + "?action=query&list=embeddedin&eititle=Template:"
                                + encodeTitle(templateName) + "&eilimit=max&format=json";
                     
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<String> pages = new ArrayList<>();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());

            com.fasterxml.jackson.databind.JsonNode backlinks = root.path("query").path("embeddedin");
            if (!backlinks.isMissingNode()) {
                for (com.fasterxml.jackson.databind.JsonNode backlink : backlinks) {
                    String title = backlink.path("title").asText();
                    pages.add(title);
                }
            }
            // Get the next page if exist
            com.fasterxml.jackson.databind.JsonNode cont = root.path("continue").path("eicontinue");
            if (!cont.isMissingNode()) {
                pages.addAll(getPagesUsingTemplateWithContinue(templateName, cont.asText()));
            }

            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return pages;
        }
    }

    // get all wiki pages
    public List<String> getAllPages() {
        List<String> allPages = new ArrayList<>();
        String apcontinue = null;
    
        do {
            // Construct the URL with the pagination parameter if necessary
            String url = API_ENDPOINT + "?action=query&list=allpages&aplimit=max&format=json";
            if (apcontinue != null) {
                url += "&apcontinue=" + apcontinue;
            }
    
            // Call the API to get the pages
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    
            try {
                // Read the JSON response
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
    
                // Extract the pages from the response
                com.fasterxml.jackson.databind.JsonNode pages = root.path("query").path("allpages");
                if (!pages.isMissingNode()) {
                    for (com.fasterxml.jackson.databind.JsonNode page : pages) {
                        String title = page.path("title").asText();
                        allPages.add(title);
                    }
                }
                // Get the next page if exist
                com.fasterxml.jackson.databind.JsonNode cont = root.path("continue").path("apcontinue");
                apcontinue = cont.isMissingNode() ? null : cont.asText();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        // Continue as long as there are more pages
        } while (apcontinue != null); 
        System.out.println(allPages.size());
        return allPages;
    }

    // Generate triples for all wiki pages
    public Model generateTriplesForAllPages() {
        List<String> allPages = getAllPages();
        Model model = ModelFactory.createDefaultModel();
        Property mainEntityOfPage = model.createProperty("http://schema.org/mainEntityOfPage");

        Resource pageResource;
        Resource entityResource;
        String pokemonName = "";
        for (String page : allPages) {
            pageResource = model.createResource(bullbapediaWikiUrl + "/" + encodeTitle(page));
            if (page.contains("(Pokémon)") ||page.contains("(Pokemon)")) {
                pokemonName = page.replace(" (Pokémon)", "");
                pokemonName = page.replace(" (Pokemon)", "");
                entityResource = model.createResource(URI + "/pokemon/" + encodeTitle(pokemonName));
            }else if(page.contains("(Ability)")){
                pokemonName = page.replace(" (Ability)", "");
                entityResource = model.createResource(URI + "/ability/" + encodeTitle(pokemonName));
            }
            else if(page.contains("(move)")){
                pokemonName = page.replace(" (move)", "");
                entityResource = model.createResource(URI + "/move/" + encodeTitle(pokemonName));
            }
            else if(page.contains("(Location)")){
                pokemonName = page.replace(" (Location)", "");
                entityResource = model.createResource(URI + "/location/" + encodeTitle(pokemonName));
            }
            else{
                entityResource = model.createResource(URI + "/" + encodeTitle(page));
            }
            model.add(entityResource, mainEntityOfPage, pageResource);

        }
    
        return model;
    }    
       

    public String encodeTitle(String title) {
        return title.replace(" ", "_");
    }
}
