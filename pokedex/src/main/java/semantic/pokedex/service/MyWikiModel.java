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
    @Override
    public void substituteTemplateCall(String templateName,
                                       Map<String, String> parameterMap,
                                       Appendable writer) throws IOException {
        // 1. Stocker le template dans la liste
        TemplateData data = new TemplateData(templateName, parameterMap);
        templates.add(data);

        // 2. Laisser Bliki gérer l’expansion/rendu du template comme d’habitude
        super.substituteTemplateCall(templateName, parameterMap, writer);
    }

    public List<TemplateData> getCollectedTemplates() {
        return templates;
    }
    
}