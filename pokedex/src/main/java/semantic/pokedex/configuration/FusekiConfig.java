package semantic.pokedex.configuration;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FusekiConfig {

    @Bean
    public Dataset dataset() {
        return TDB2Factory.createDataset();
    }

    @Bean
    public CommandLineRunner startFusekiServer() {
        return args -> {
            Dataset dataset = TDB2Factory.createDataset();
            FusekiServer server = FusekiServer.create()
                    .port(3030) 
                    .add("/dataset", dataset)
                    .build();

            System.out.println("Démarrage de Fuseki...");
            server.start();
        };
    }
}
