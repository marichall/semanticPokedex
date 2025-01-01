package semantic.pokedex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryPageService {

    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    /**
     * Récupère la liste des types d'infobox à partir de la catégorie "Category:Infobox templates".
     *
     * @return Liste des types d'infobox.
     */
    public List<String> getInfoboxTypes() {

        String categoryTitle = "Category:Infobox_templates";
        List<String> pages = mediaWikiApiService.getPagesInCategory(categoryTitle);
        if(pages == null) {
            return new ArrayList<>();
        }

        List<String> infoboxTypes = pages.stream()
                .filter(page -> page.startsWith("Template:"))
                .map(page -> page.replace("Template:", "").trim())
                .collect(Collectors.toList());
        
        return infoboxTypes;
    }
}
