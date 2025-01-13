package semantic.pokedex.service;

import java.util.*;

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

     /**
     * Méthode helper utilisée pour gérer la pagination des résultats de l'API MediaWiki.
     * Elle récupère les pages suivantes utilisant le template spécifié en utilisant le token de continuation.
     *
     * @param templateName Le nom du template dont on veut trouver les pages qui l'utilisent.
     * @param eicontinue   Le token de continuation fourni par l'API MediaWiki pour récupérer la prochaine série de résultats.
     * @return Une liste de titres de pages utilisant le template spécifié.
     */
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

            // Récupérer la suite si nécessaire
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

     /**
     * Récupère toutes les pages qui utilisent un template spécifique en gérant la pagination via des tokens de continuation.
     *
     * @param templateName Le nom du template dont on veut trouver les pages utilisant.
     * @return Une liste de titres de pages utilisant le template spécifié.
     */
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

            // On gère la pagination s'il y en a
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

    /**
     * Récupère toutes les pages du wiki
     * 
     * @return Une liste de toutes les pages trouvés
     */
    public List<String> getAllPages() {
        List<String> allPages = new ArrayList<>();
        String apcontinue = null;
    
        do {
            // Construire l'URL avec le paramètre de pagination si nécessaire
            String url = API_ENDPOINT + "?action=query&list=allpages&aplimit=max&format=json";
            if (apcontinue != null) {
                url += "&apcontinue=" + apcontinue;
            }
    
            // Appeler l'API pour obtenir les pages
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    
            try {
                // Lire la réponse JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
    
                // Extraire les pages de la réponse
                com.fasterxml.jackson.databind.JsonNode pages = root.path("query").path("allpages");
                if (!pages.isMissingNode()) {
                    for (com.fasterxml.jackson.databind.JsonNode page : pages) {
                        String title = page.path("title").asText();
                        allPages.add(title);
                    }
                }
    
                // Gerer la pagination s'il y en a
                com.fasterxml.jackson.databind.JsonNode cont = root.path("continue").path("apcontinue");
                apcontinue = cont.isMissingNode() ? null : cont.asText();
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        // Continuer tant qu'il y a des pages suivantes
        } while (apcontinue != null); 
        System.out.println(allPages.size());
        return allPages;
    }

    /**
     * Génère des triplets pour toutes les pages du wiki
     * 
     * @return Un model RDF contenant les triplets de toutes les pages
     */
    public Model generateTriplesForAllPages() {
        List<String> allPages = getAllPages();
        Model model = ModelFactory.createDefaultModel();
        Property mainEntityOfPage = model.createProperty("http://schema.org/mainEntityOfPage");

        Resource pageResource;
        Resource entityResource;
        
        for (String page : allPages) {
            pageResource = model.createResource(bullbapediaWikiUrl + "/" + encodeTitle(page));
            entityResource = model.createResource(URI + "/" + encodeTitle(page));
            model.add(entityResource, mainEntityOfPage, pageResource);

        }
    
        return model;
    }    
       

    public String encodeTitle(String title) {
        return title.replace(" ", "_");
    }
}
