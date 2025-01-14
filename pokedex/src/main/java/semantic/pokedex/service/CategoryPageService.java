package semantic.pokedex.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryPageService {

    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    /**
     * Get the list of infobox types from the category "Category:Infobox templates".
     *
     * @return List of infobox types.
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
