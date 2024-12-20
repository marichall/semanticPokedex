package semantic.pokedex.service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.VCARD;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseInfobox {

        String infobox = extractInfobox(wikitext);
        // Étape 4: Créer un graphe RDF avec Jena
        Model model = ModelFactory.createDefaultModel();
        Resource bulbasaurResource = model.createResource("http://example.org/pokemon/Bulbasaur");

        // Expression régulière pour parser le fichier
        Pattern p = Pattern.compile("\\|([^=]+)=([^\\n]+)");
        Matcher matcher = p.matcher(infobox);
        while (matcher.find()) {
            String property = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            Property pred = model.createProperty("http://example.org/pokemon#" + property);
            Literal obj = model.createLiteral(value);
            model.add(bulbasaurResource, pred, obj);
        }

        // Étape 5: Sérialiser le modèle RDF en XML
        model.write(System.out, "RDF/XML");
    }

    // Méthode pour extraire l'infobox du wikitext
    public static String extractInfobox(String wikitext) {
        // Ici, nous pouvons supposer que l'infobox commence par {{Pokémon Infobox et se termine par }}
        Pattern pattern = Pattern.compile("\\{\\{Pokémon Infobox(.+?)\\}\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(wikitext);
        if (matcher.find()) {
            return matcher.group(1);  // Retourne l'infobox
        }
        return "";
    }
}
}