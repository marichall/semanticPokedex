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
1. Open the project in VS Code.
2. Run the project using the Spring Boot Dashboard.
3. Once Spring Boot is running, navigate to `http://localhost:8080/pokemon/Bulbasaur` in your browser. This displays the entity for Bulbasaur, parsed from its infobox.
4. A Turtle file containing the entire knowledge graph (`KG.ttl`) can be found in the repository.
