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

@State(Scope.Thread)
public class ParseTableGenerationBenchmark {
    
    public enum Language {
        OCAML, JAVA
    }

    @Param
    public static Language lang;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        public final static String[] languages = { "OCaml", "Java" };
        public final static String[] mainSDF3normModule = { "OCaml", "java-front" };
    }

    @Param({ "true", "false" })
    public static boolean isDynamic;

    @Param({  "true", "false" })
    public static boolean isDataDependent;

    @Param({  "true", "true" })
    public static boolean enableDeepConflictResolution;

    @State(Scope.Benchmark)
    public static class ParseTableGeneratorState {

        public ParseTableGenerator ptg;

        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            File mainFile;

            mainFile = new File("normalizedGrammars/" + BenchmarkLanguages.languages[lang.ordinal()] + "/normalized/"
                + BenchmarkLanguages.mainSDF3normModule[lang.ordinal()] + "-norm.aterm");

            ptg = new ParseTableGenerator(mainFile, null, null, null,
                Lists.newArrayList("normalizedGrammars/" + BenchmarkLanguages.languages[lang.ordinal()]));

            if(!enableDeepConflictResolution && (isDynamic || isDataDependent)) {
                throw new RuntimeException();
            }
        }
    }



    public static void main(String[] args) throws RunnerException {
        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(5) 
            .measurementIterations(5)
            .mode(Mode.AverageTime)
            .forks(1)
            .threads(5)
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
    public ParseTableGenerator parseTableGeneration(ParseTableGeneratorState s) throws Exception {
        s.ptg.createParseTable(isDynamic, isDataDependent, enableDeepConflictResolution);
        return s.ptg;
    }

}
