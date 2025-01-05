package semantic.pokedex.service;

import semantic.pokedex.service.TemplateData;
import info.bliki.wiki.model.WikiModel;
import info.bliki.api.Template;
import info.bliki.wiki.model.IWikiModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<TemplateData> getCollectedTemplates() {
        return templates;
    }
    
}