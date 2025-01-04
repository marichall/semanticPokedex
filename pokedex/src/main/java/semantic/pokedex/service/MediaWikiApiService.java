package semantic.pokedex.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MediaWikiApiService {

    private static final String API_ENDPOINT = "https://bulbapedia.bulbagarden.net/w/api.php";

    @Autowired
    private RestTemplate restTemplate;

    public String getPageWikitext(String pageTitle) {
        String url = API_ENDPOINT + "?action=parse&format=json&page=" + 
                     encodeTitle(pageTitle) + "&prop=wikitext";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        // Traitement de la réponse de l'API
        // La réponse est en JSON, on utilise Jackson ObjectMapper pour lire la réponse

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
            return root.path("parse").path("wikitext").path("*").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String getPokemonPageWikitext(String pokemonName) {
        String pageTitle = pokemonName + "_(Pokémon)";
        return getPageWikitext(pageTitle);
    }

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

    private List<String> getPagesUsingTemplateWithContinue(String templateName, String blcontinue) {
        String url = API_ENDPOINT + "?action=query&list=backlinks&bltitle=Template:" 
                     + encodeTitle(templateName) + "&blfilterredir=nonredirects&bllimit=max&blcontinue=" 
                     + blcontinue + "&format=json";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<String> pages = new ArrayList<>();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());

            com.fasterxml.jackson.databind.JsonNode backlinks = root.path("query").path("backlinks");
            if (!backlinks.isMissingNode()) {
                for (com.fasterxml.jackson.databind.JsonNode backlink : backlinks) {
                    String title = backlink.path("title").asText();
                    pages.add(title);
                }
            }

            // Récupérer la suite si nécessaire
            com.fasterxml.jackson.databind.JsonNode cont = root.path("continue").path("blcontinue");
            if (!cont.isMissingNode()) {
                pages.addAll(getPagesUsingTemplateWithContinue(templateName, cont.asText()));
            }

            return pages;
        } catch (Exception e) {
            e.printStackTrace();
            return pages;
        }
    }

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

            // Gérer la pagination
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
    

    private String encodeTitle(String title) {
        return title.replace(" ", "_");
    }
}
