package semantic.pokedex.service;

import org.apache.jena.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class CreateHtmlService {

    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    private static final String API_ENDPOINT = "https://pokeapi.co/api/v2/pokemon/";

    public String createHtmlInfobox(String type, String name) {

        String sparqlQuery = String.format(
            "SELECT ?predicate ?object WHERE { <%s> ?predicate ?object }", mediaWikiApiService.URI + "/"+ type +"/" + name);    

        ResultSet results = fusekiService.executeSelectQuery(sparqlQuery);

        if (results == null) {
            return "<html><body><h1>impossible de récupérer les données pour " + name + ".</h1></body></html>";
        }

        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang='en'>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlBuilder.append("<title>Details for " + name + "</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("table { width: 50%; margin: 20px auto; border-collapse: collapse; }");
        htmlBuilder.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        htmlBuilder.append("th { background-color: #f4f4f4; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<h1 style='text-align: center;'>Details for " + name + "</h1>");
        
        if ("pokemon".equalsIgnoreCase(type)) {
            String imageUrl = getPokemonImage(name);
            htmlBuilder.append("<div style='text-align: center;'>");
            htmlBuilder.append("<img src='" + imageUrl + "' alt='Image of " + name + "' style='width: 300px; height: 300px;'>");
            htmlBuilder.append("</div>");
        }
        htmlBuilder.append("<table>");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr><th>Parameter</th><th>Value</th></tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");

        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();

            String predicate = solution.get("predicate").toString();
            predicate = predicate.replace(mediaWikiApiService.URI + "/", "");
            predicate = predicate.replace("http://schema.org/", "");
            String object = solution.get("object").toString();

            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>" + predicate + "</td>");
            if (object.startsWith("http")) {
                htmlBuilder.append("<td><a href='" + object + "'>" + object + "</a></td>");
            } else if (predicate.contains("ability")) {
                    htmlBuilder.append("<td><a href='" + mediaWikiApiService.URI + "/ability/" + object + "/html'>" + object + "</a></td>");
            } else if (predicate.contains("move")) {
                    htmlBuilder.append("<td><a href='" + mediaWikiApiService.URI + "/move/" + object + "/html'>" + object + "</a></td>");
            } else if (predicate.contains("location")) {
                    htmlBuilder.append("<td><a href='" + mediaWikiApiService.URI + "/location/" + object + "/html'>" + object + "</a></td>");
            } else{
                htmlBuilder.append("<td>" + object + "</td>");
            }
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }

    public String getPokemonImage(String pokemonName) {
        String url = API_ENDPOINT + pokemonName.toLowerCase();

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode spriteNode = root.path("sprites").path("front_default");
            if (!spriteNode.isMissingNode()) {
                return spriteNode.asText();
            } else {
                return "Image non trouvée pour le Pokémon " + pokemonName;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la récupération de l'image pour " + pokemonName + ": " + e.getMessage();
        }
    }

    public String createTurtleDescription(String type, String name) {
        String sparqlQuery = String.format(
            "SELECT ?predicate ?object WHERE { <%s> ?predicate ?object }", mediaWikiApiService.URI + "/" + type + "/" + name);

        ResultSet results = fusekiService.executeSelectQuery(sparqlQuery);

        if (results == null) {
            return "# No data found for the entity: " + name;
        }

        StringBuilder turtleBuilder = new StringBuilder();
        turtleBuilder.append("@prefix ex: <http://example.org/> .\n");
        turtleBuilder.append("@prefix schema: <http://schema.org/> .\n\n");

        String entityUri = mediaWikiApiService.URI + "/" + type + "/" + name;
        turtleBuilder.append("<" + entityUri + ">\n");

        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();

            String predicate = solution.get("predicate").toString();
            String object = solution.get("object").toString();

            if (predicate.startsWith("http://schema.org/")) {
                predicate = "schema:" + predicate.replace("http://schema.org/", "");
            } else {
                predicate = "<" + predicate + ">";
            }

            if (object.startsWith("http")) {
                object = "<" + object + ">";
            } else {
                object = '"' + object + '"';
            }

            turtleBuilder.append("    " + predicate + " " + object + " ;\n");
        }

        if (turtleBuilder.lastIndexOf(";") != -1) {
            turtleBuilder.replace(turtleBuilder.lastIndexOf(";"), turtleBuilder.lastIndexOf(";") + 1, ".");
        }

        return turtleBuilder.toString();
    }


    public String createHtmlTurtleDescription(String type, String name) {
        String turtleDescription = createTurtleDescription(type, name);
        String[] lines = turtleDescription.split("\n");

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang='en'>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlBuilder.append("<title>Turtle Description for " + name + "</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("pre { background-color: #f4f4f4; padding: 10px; border: 1px solid #ddd; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<h1 style='text-align: center;'>Turtle Description for " + name + "</h1>");
        htmlBuilder.append("<pre>");

        for (String line : lines) {
            htmlBuilder.append(line.replace("<", "&lt;").replace(">", "&gt;").replace(" ", "&nbsp;")).append("<br>");
        }

        htmlBuilder.append("</pre>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        return htmlBuilder.toString();
    }
}
