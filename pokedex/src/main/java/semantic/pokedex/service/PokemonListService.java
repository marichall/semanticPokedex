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

    /**
     * Récupère la liste des noms des pokémons à l'aide de l'API.
     *
     * @return Liste des noms des Pokémon.
     */
    public List<String> getPokemonList() {
        String pageTitle = "List_of_Pokémon_by_Kanto_Pokédex_number";
        String wikitext = mediaWikiApiService.getPageWikitext(pageTitle);
        if (wikitext == null) {
            return new ArrayList<>();
        }

        // Expression régulière pour récupérer les info dans l'infobox
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