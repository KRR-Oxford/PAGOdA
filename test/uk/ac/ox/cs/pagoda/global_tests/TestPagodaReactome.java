package uk.ac.ox.cs.pagoda.global_tests;

import org.testng.annotations.Test;
import uk.ac.ox.cs.pagoda.Pagoda;
import uk.ac.ox.cs.pagoda.util.TestUtil;

import java.io.IOException;
import java.nio.file.Paths;

public class TestPagodaReactome {

    @Test(groups = {"justExecute"})
    public void justExecute() throws IOException {
        String ontoDir = TestUtil.getConfig().getProperty("ontoDir");

        Pagoda.builder()
              .ontology(Paths.get(ontoDir, "reactome/biopax-level3-processed.owl"))
              .data(Paths.get(ontoDir, "reactome/data/sample_10.ttl"))
              .query(Paths.get(ontoDir, "reactome/test.sparql"))
              .classify(true)
              .hermit(true)
              .build()
              .run();
    }

    @Test(groups = {"sygenia"})
    public void justExecute_sygenia() throws IOException {
        String ontoDir = TestUtil.getConfig().getProperty("ontoDir");

        Pagoda.builder()
              .ontology(Paths.get(ontoDir, "reactome/biopax-level3-processed.owl"))
              .data(Paths.get(ontoDir, "reactome/data/sample_10.ttl"))
              .query(Paths.get(ontoDir, "reactome/reactome_sygenia_queries.sparql"))
              .classify(true)
              .hermit(true)
              .build()
              .run();
    }

    @Test(groups = {"existential"})
    public void justExecute_existential() throws IOException {
        String ontoDir = TestUtil.getConfig().getProperty("ontoDir");

        Pagoda.builder()
                .ontology(Paths.get(ontoDir, "reactome/biopax-level3-processed.owl"))
                .data(Paths.get(ontoDir, "reactome/data/sample_10.ttl"))
//                .query(Paths.get(ontoDir, "reactome/existential_queries.sparql"))
                .query(Paths.get(ontoDir, "reactome/Queries_by_Feier-et-al.sparql"))
                .build()
                .run();
    }

}
