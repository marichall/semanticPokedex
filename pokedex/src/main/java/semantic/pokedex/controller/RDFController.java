package semantic.pokedex.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import semantic.pokedex.service.RDFService;

@RestController
public class RDFController {

    @Autowired
    RDFService rdfService;

    @GetMapping("/")
    public String addPokemon() {
        rdfService.addData();
        return "Data added to the triplestore!";
    }
}
