package org.metaborg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.Lists;

@State(Scope.Benchmark)
public class ParseTableGenerationBenchmark {
    
    public enum Language {
        OCAML, JAVA
    }

    @Param({ "JAVA" })
    public static Language a_lang;    
    
    @Param({ "true", "false" })
    public static boolean c_isDataDependent;
    
    @Param({ "true", "false" })
    public static boolean d_solvesDeepConflicts;
    
    @Param({ "true", "false" })
    public static boolean b_isLazyGeneration;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        public final static String[] languages = { "OCaml", "Java" };
        public final static String[] mainSDF3normModule = { "OCaml", "java-front" };
    }

    @State(Scope.Benchmark)
    public static class BenchmarkTableGenerationConfig {
        public final static String[] config =
            { "regular", "dataDependent", "original", "lazyRegular", "lazyDataDependent" };

        File mainFile;

        ParseTableGenerator ptg;

        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            if(a_lang.equals(Language.OCAML) ) {
                throw new RuntimeException();
            }            
            
            if(!d_solvesDeepConflicts && (b_isLazyGeneration || c_isDataDependent)) {
                throw new RuntimeException();
            }
            
            mainFile = new File("normalizedGrammars/" + BenchmarkLanguages.languages[a_lang.ordinal()] + "/normalized/"
                + BenchmarkLanguages.mainSDF3normModule[a_lang.ordinal()] + "-norm.aterm");

            ptg = new ParseTableGenerator(mainFile, null, null, null,
                Lists.newArrayList("normalizedGrammars/" + BenchmarkLanguages.languages[a_lang.ordinal()]));
        }
    }

    public static void main(String[] args) throws RunnerException {
        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(5) 
            .measurementIterations(5)
            .mode(Mode.AverageTime)
            .forks(1)
            .threads(1)
            .include(ParseTableGenerationBenchmark.class.getSimpleName())
            .timeUnit(TimeUnit.MILLISECONDS)
            .build();

     // @formatter:on
        new Runner(options).run();
    }

 // @formatter:off
    /*
     * for each language:
     * - measure parser generation time for regular contextual grammar (that duplicates productions)
     * - measure parser generation time for contextual grammar that does not duplicate productions (to be used by the data dependent parser)
     * - measure parser generation time for original grammar that does not solve deep priority conflicts
     * - measure parser generation time for regular contextual grammar using lazy parser generation (does not construct the states)
     * - measure parser generation time for data dependent contextual grammar using lazy parser generation (does not construct the states)
     */
    // @formatter:on

    @Benchmark
    public ParseTableGenerator parseTableGeneration(BenchmarkTableGenerationConfig btgc) throws Exception {
        btgc.ptg.createParseTable(b_isLazyGeneration, c_isDataDependent, d_solvesDeepConflicts);
        return btgc.ptg;
    }

}
