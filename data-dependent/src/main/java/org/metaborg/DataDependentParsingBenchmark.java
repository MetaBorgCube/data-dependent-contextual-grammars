package org.metaborg;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

@State(Scope.Benchmark)
public class DataDependentParsingBenchmark {

    @Param({ "test/Java/aurora-imui/Android/chatinput/src/androidTest/java/imui/jiguang/cn/imuikit/ExampleInstrumentedTest.java" })
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

    @Param({ "JAVA" })
    public static Language a_lang;

    @Param({ "true", "false" })
    public static boolean b_isLazyGeneration;

    @Param({ "true", "false" })
    public static boolean c_isDataDependent;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        public ParseTable pt;
        public JSGLR2<?, ?, IStrategoTerm> parser;

        @Setup(Level.Trial)
        public void doSetup() throws Exception {

            FileSystemManager fsManager = VFS.getManager();
            String pathToParseTable = "resources/parseTables/" + a_lang.getLanguageName() + "/";
            if(b_isLazyGeneration) {
                pathToParseTable += "lazy/";
            } else {
                pathToParseTable += "notLazy/";
            }
            
            if(c_isDataDependent) {
                pathToParseTable += "dataDependent/";
            } else {
                pathToParseTable += "notDataDependent/";
            }
            
            File PTpath = new File(pathToParseTable); 
            if(!(PTpath.exists())) {
                System.out.println("dirs did not exist, creating them");
                PTpath.mkdirs();
            }

            File persistedFile = new File(pathToParseTable + "parseTable.bin");
            if(persistedFile.exists()) {
                System.out.println("parse table already exists, importing it");
                FileObject parseTable = fsManager.resolveFile(PTpath, "parseTable.bin");
                ParseTableGenerator ptg = new ParseTableGenerator(parseTable);
                pt = ptg.getParseTable();
            } else {
                System.out.println("parse table does not exist, creating it");
                File mainFile = new File("normalizedGrammars/" + a_lang.getLanguageName() + "/normalized/"
                    + a_lang.getMainSDF3Module() + "-norm.aterm");

                ParseTableGenerator ptg = new ParseTableGenerator(mainFile, null, persistedFile, null,
                    Lists.newArrayList("normalizedGrammars/" + a_lang.getLanguageName()));

                ptg.outputTable(b_isLazyGeneration, c_isDataDependent);
                pt = ptg.getParseTable();
            }
            
            if(c_isDataDependent) {
                parser = JSGLR2.dataDependent(pt);
            } else {
                parser = JSGLR2.standard(pt);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {

        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(10) 
            .measurementIterations(25)
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
    public void parseFile(Blackhole bh, BenchmarkLanguages bl, FileConfig fc) throws IOException {
        bh.consume(bl.parser.parse(fc.input));
    }
}

