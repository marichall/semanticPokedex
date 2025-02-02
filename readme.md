# Semantic Web Project: Bulbapedia Knowledge Graph

## Overview
This project, completed by second-year master's students from DSC:
- BAYAZID Hany.
- MARICHAL-ROSIÈRE Léa.

It aims to build a knowledge graph for Bulbapedia using semantic web technologies.

## Project Technologies
- **Language & Framework:** Java with Spring Boot
- **Java Version:** Java 21
- **RDF Processing:** Apache Jena for creating RDF triples and interfacing with Fuseki
- **Parsing:** Bliki framework for parsing infoboxes

## How to Launch the Project
### 1. Open the Project
- Open the project in **VS Code**.

### 2. Run the Project
- Navigate to the **Spring Boot Dashboard** (hexagon with a power button icon on the left sidebar in VS Code).
- Click the **Run** button next to `pokedex`.
- All required dependencies are pre-configured in the `pom.xml`.

### 3. Start Fuseki
- Launch the Fuseki server and create a dataset named `PokemonDataset` (case-sensitive).

### 4. Generate RDF Triples
   - Now that both fuseki and our application is running, open your browser, and go to `http://localhost:8080/api/generateAllInfoboxForAllPokemons`. <br> This page will create the RDF triples for all the pokemons, moves, locations, and abilities.
   This call will take a long time before it's complete, so if you don't want to wait, we suggest you to wait 5 minutes, and stop the spring boot server by pressing the squared stop button, or by pressing Ctrl + C in the VS Code terminal.
   If the program is interrupted, you need to run the localhost:8080 server again, like explained in step 2.

### 5. Generate MainEntityOf Triples 
   - In order to generate the triples that describe our resources as "MainEntityOf" another page, go to `http://localhost:8080/api/generateTriplesForAllPages`, this may also take some time (about 10 - 15 minutes), but a lot less than the one in step 4.

### 6. Add External Knowledge 
   - Once step 6 is done running or in case you decided to interrupt the server, you can go to `http://localhost:8080/api/generateTriplesWithTsvFile`, in order to add external knowledge from the provided tsvfile.

### 7. Export Data to Turtle Format . 
   - In order to export all the data in a turtle format, you can navigate to `http://localhost:8080/api/writeInTurtleFile`. This will export all the added data from PokemonDataset in fuseli, and export it as a turtle file named KG.ttl.

### 8. View Entities 
   - If you would like to see a pokemon, a move, a location, or an ability in an html format, naviagte to `http://localhost:8080/category/name/html` in your browser. This displays the entity name for a category (categories are pokemon,move, location,     ability) , parsed from their infoboxes.
   For example :
     - `http://localhost:8080/pokemon/Bulbasaur/html` will give you the html representation of the pokemon Bulbasaur. (If you used /turtle, you will optain the turtle representation).
     - `http://localhost:8080/move/Pound/html` will give you the html representation of the move Pound.
     - `http://localhost:8080/ability/Blaze/html` will give you the html representation of the blaze ability.
     - `http://localhost:8080/location/Abandoned_Ship/html` will give you the html representaion of the location Abandoned Ship.
     - In case you want to view the RDF of an entity in the browser, use the same link, ie `http://localhost:8080/pokemon/Bulbasaur/html`, but replace `html` with `turtle`.

## Useful Links for the categories 
   - The lists of moves, pokemons, abilities and locations can be found here :
     - List of moves : https://bulbapedia.bulbagarden.net/wiki/List_of_moves
     - List of pokemons and abilites : https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_Ability
     - List of locations : https://bulbapedia.bulbagarden.net/wiki/List_of_locations_by_name

## SHACL
   - If you want to see the SHACL file, it is in the pokedex folder, and its name is shacl.ttl.
