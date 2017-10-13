package org.metaborg;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.google.common.collect.Lists;

@State(Scope.Thread)
public class MyBenchmark {

    @Param({ "0", "1" })
    public int lang;

    @Param({ "0", "1", "2", "3", "4" })
    public int tableGenerationConfig;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        public final static String[] languages = { "OCaml", "Java" };
        public final static String[] mainSDF3normModule = { "OCaml", "java-front" };
    }

    @State(Scope.Benchmark)
    public static class BenchmarkTableGenerationConfig {
        public final static String[] config =
            { "regular", "dataDependent", "original", "lazyRegular", "lazyDataDependent" };
    }

    public static void main(String[] args) throws RunnerException {
        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(5) 
            .measurementIterations(5)
            .mode(Mode.AverageTime)
            .forks(1)
            .threads(5)
            .include(MyBenchmark.class.getSimpleName())
            .timeUnit(TimeUnit.MILLISECONDS)
            .build();

     // @formatter:on
        new Runner(options).run();
    }

    @Benchmark
    public void parseTableGeneration() throws Exception {
        File mainFile = new File("normalizedGrammars/" + BenchmarkLanguages.languages[lang] + "/normalized/"
            + BenchmarkLanguages.mainSDF3normModule[lang] + "-norm.aterm");

        ParseTableGenerator ptg = new ParseTableGenerator(mainFile, null, null, null,
            Lists.newArrayList("normalizedGrammars/" + BenchmarkLanguages.languages[lang]));

        switch(BenchmarkTableGenerationConfig.config[tableGenerationConfig]) {
            case "regular":
                ptg.createParseTable(false, false);
                break;
            case "dataDependent":
                ptg.createParseTable(false, true);
                break;
            case "original":
                ptg.createParseTable(false, false, false);
                break;
            case "lazyRegular":
                ptg.createParseTable(true, false);
                break;
            case "lazyDataDependent":
                ptg.createParseTable(true, true);
                break;
            default:
                break;
        }

    }

}
