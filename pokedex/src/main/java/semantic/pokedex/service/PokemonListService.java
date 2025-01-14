package semantic.pokedex.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PokemonListService {

    @Autowired
    private MediaWikiApiService mediaWikiApiService;

    // Get pokemon list with the API
    public List<String> getPokemonList() {
        String pageTitle = "List_of_Pokémon_by_Kanto_Pokédex_number";
        String wikitext = mediaWikiApiService.getPageWikitext(pageTitle);
        if (wikitext == null) {
            return new ArrayList<>();
        }

        // Regular expression to get the info in the infobox
        String regex = "\\{\\{rdex\\|[^|]*\\|[^|]*\\|([^|]+)\\|";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(wikitext);
        List<String> pokemonNames = new ArrayList<>();


        while (matcher.find()) {
            String name = matcher.group(1).trim();
            pokemonNames.add(name);    
        }
        return pokemonNames;
    }

    // public String getPokemonInfoBox(String pokemonName){
    //     String pokemonWikitext = mediaWikiApiService.getPokemonPageWikitext(pokemonName);
    //     return pokemonWikitext;
    // }
}