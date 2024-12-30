package semantic.pokedex.service;

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

    private String encodeTitle(String title) {
        return title.replace(" ", "_");
    }
}
