package org.metaborg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

@State(Scope.Thread)
public class DataDependentParsingBenchmark {



    @Param({ "test/Java/no-project/disamb.java" })
    public static String filename;

    @State(Scope.Benchmark)
    public static class FileConfig {
        public File currentFile;
        public String input;

        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            currentFile = new File(filename);
            if(currentFile.exists()) {
                input = FileUtils.readFileToString(currentFile, Charsets.UTF_8);
            } else {
                input = "";
            }
        }

    }

    @Param({ "1" })
    public static int lang;

     @Param({ "0", "1", "2", "3" })
    public static int mode;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        @Setup(Level.Trial)
        public void doSetup() throws Exception {
            File mainFile = new File(
                "normalizedGrammars/" + languages[lang] + "/normalized/" + mainSDF3normModule[lang] + "-norm.aterm");

            ParseTableGenerator ptg = new ParseTableGenerator(mainFile, null, null, null,
                Lists.newArrayList("normalizedGrammars/" + languages[lang]));

            switch(mode) {
                case 0:
                    System.out.println("generating parse table for regular contextual grammar");
                    ptg.createParseTable(false, false);
                    pt = ptg.getParseTable();
                    System.out.println("parse table has " + pt.totalStates() + " states.");
                    parser = JSGLR2.standard(pt);
                    break;
                case 1:
                    System.out.println("generating parse table for data dependent contextual grammar");
                    ptg.createParseTable(false, true);
                    pt = ptg.getParseTable();
                    parser = JSGLR2.dataDependent(pt);
                    System.out.println("parse table has " + pt.totalStates() + " states.");
                    break;
                case 2:
                    System.out.println("generating parse table for lazy regular contextual grammar");
                    ptg.createParseTable(true, false);
                    pt = ptg.getParseTable();
                    parser = JSGLR2.standard(pt);
                    System.out.println("parse table has " + pt.totalStates() + " states.");
                    break;
                case 3:
                    System.out.println("generating parse table for lazy data dependent contextual grammar");
                    ptg.createParseTable(true, true);
                    pt = ptg.getParseTable();
                    parser = JSGLR2.dataDependent(pt);
                    System.out.println("parse table has " + pt.totalStates() + " states.");
                    break;
                default:
                    System.out.println("invalid option");
                    pt = null;
                    break;
            }


        }

        public final String[] languages = { "OCaml", "Java" };
        public final String[] mainSDF3normModule = { "OCaml", "java-front" };
        public final String[] parsingMode = { "regular", "dataDependent", "lazyGenRegular", "lazyGenDataDependent" };
        public ParseTable pt;
        public JSGLR2<?, ?, IStrategoTerm> parser;
    }

    public static void main(String[] args) throws RunnerException {

        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(10) 
            .measurementIterations(10)
            .mode(Mode.AverageTime)
            .forks(1)
            .threads(1)
            .include(DataDependentParsingBenchmark.class.getSimpleName())
            .timeUnit(TimeUnit.MILLISECONDS)
            .build();

     // @formatter:on
        new Runner(options).run();
    }

    // @formatter:off
    /*
     * for each file:
     * - measure parse time for regular contextual grammar (that duplicates productions)
     * - measure parse time for data dependent parser + contextual grammar that does not duplicate productions
     * - measure parse time for regular contextual grammar + lazy parser generation
     * - measure parse time for data dependent parser + lazy parse table generation using the contextual grammar that does not duplicate productions
     */
    // @formatter:on

    @Benchmark
    public IStrategoTerm parseFile(BenchmarkLanguages bl, FileConfig fc) throws IOException {
        return bl.parser.parse(fc.input);
    }
}

