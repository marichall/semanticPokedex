package semantic.pokedex.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.query.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class CreateHtmlService {

    @Autowired
    private FusekiService fusekiService;
    
    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    public String createHtmlForPokemonInfobox(String pokemonName) {

        String sparqlQuery = String.format(
            "SELECT ?predicate ?object WHERE { <%s> ?predicate ?object }", mediaWikiApiService.URI + "/pokemon/" + pokemonName);    

        System.out.println("Query: " + sparqlQuery);
        ResultSet results = fusekiService.executeSelectQuery(sparqlQuery);
        System.out.println("Results: " + results.hasNext());


        // Initialisation de la chaîne HTML
        StringBuilder htmlBuilder = new StringBuilder();

        // Début de la page HTML
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html lang='en'>");
        htmlBuilder.append("<head>");
        htmlBuilder.append("<meta charset='UTF-8'>");
        htmlBuilder.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        htmlBuilder.append("<title>Details for " + pokemonName + "</title>");
        htmlBuilder.append("<style>");
        htmlBuilder.append("table { width: 50%; margin: 20px auto; border-collapse: collapse; }");
        htmlBuilder.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        htmlBuilder.append("th { background-color: #f4f4f4; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head>");
        htmlBuilder.append("<body>");
        htmlBuilder.append("<h1 style='text-align: center;'>Details for " + pokemonName + "</h1>");
        
        // Création du tableau
        htmlBuilder.append("<table>");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr><th>Predicate</th><th>Object</th></tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");

        // Parcours des résultats SPARQL
        while (results.hasNext()) {
            QuerySolution solution = results.nextSolution();

            // Récupération du prédicat et de l'objet
            String predicate = solution.get("predicate").toString();
            String object = solution.get("object").toString();

            // Ajouter une ligne au tableau
            htmlBuilder.append("<tr>");
            htmlBuilder.append("<td>" + predicate + "</td>");
            htmlBuilder.append("<td>" + object + "</td>");
            htmlBuilder.append("</tr>");
        }

        // Fermeture du tableau et de la page HTML
        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");
        htmlBuilder.append("</body>");
        htmlBuilder.append("</html>");

        // Retourner le HTML généré sous forme de chaîne
        return htmlBuilder.toString();
    }
}
