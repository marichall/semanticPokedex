package semantic.pokedex.service;

import info.bliki.wiki.model.WikiModel;
import info.bliki.wiki.tags.WPATag;
import info.bliki.htmlcleaner.ContentToken;
import info.bliki.wiki.filter.WikipediaPreTagParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class personnalisée qui étend WikiModel de Bliki pour intercepter et collecter les templates.
 * Permet d'extraire les informations des templates présents dans le wikitext.
 */
public class MyWikiModel extends WikiModel {

    // On garde ici tous les templates rencontrés
    public final List<TemplateData> templates = new ArrayList<>();

    public MyWikiModel(String imageBaseURL, String linkBaseURL) {
        super(imageBaseURL, linkBaseURL);
    }

    /**
     * Override de la méthode correspondant à la doc :
     * public void substituteTemplateCall(String templateName,
     *                                    Map<String,String> parameterMap,
     *                                    Appendable writer)
     *     throws IOException
     */
    // Cette méthode n'est pas utilisée (On en aura peut-être besoin plus tard, à voir.)
    @Override
    public void substituteTemplateCall(String templateName,
                                       Map<String, String> parameterMap,
                                       Appendable writer) throws IOException {
        //Stockage de la template dans la liste
        TemplateData data = new TemplateData(templateName, parameterMap);
        templates.add(data);

        super.substituteTemplateCall(templateName, parameterMap, writer);
    }

    @Override
public void appendInternalLink(
    final String topic,
    final String hashSection,
    final String topicDescription,
    String cssClass,
    boolean parseRecursive,
    boolean topicExists
) {
    // S’il s’agit d’un lien de type "Template:..."
    if (topic.startsWith("Template:")) {
        // Construis l’URL qui pointe vers, par ex., Bulbapedia
        String templateUrl = getWikiBaseURL().replace("${title}", topic);
        
        // Crée un tag HTML <a> (implémenté par Bliki dans WPATag)
        WPATag aTagNode = new WPATag();
        // On peut définir l’attribut "href"
        aTagNode.addAttribute("href", templateUrl, true);

        // Ajout d'un titre, si besoin
        aTagNode.addAttribute("title", topic, true);

        // Cas où on souhaiterait utiliser la classe CSS
        if (cssClass != null) {
            aTagNode.addAttribute("class", cssClass, true);
        }

        // Empile le tag dans la stack de Bliki
        pushNode(aTagNode);
        
        // Ajout du texte du lien (soit `topicDescription`, soit le topic)
        String description = (topicDescription != null) 
                               ? topicDescription.trim() 
                               : topic;
        
        if (parseRecursive) {
            // Au besoin, ré-analyse la description comme du wiki
            WikipediaPreTagParser.parseRecursive(description, this, false, true);
        } else {
            // Sinon, on ajoute juste du texte brut
            aTagNode.addChild(new ContentToken(description));
        }

        // Et on ferme le tag ici
        popNode();

        // On return pour ne pas appeler la logique par défaut
        return;

    } else {
        // Sinon, on laisse faire le comportement standard de Bliki
        super.appendInternalLink(
            topic, 
            hashSection, 
            topicDescription, 
            cssClass, 
            parseRecursive, 
            topicExists
        );
    }
}


    public List<TemplateData> getCollectedTemplates() {
        return templates;
    }
    
}