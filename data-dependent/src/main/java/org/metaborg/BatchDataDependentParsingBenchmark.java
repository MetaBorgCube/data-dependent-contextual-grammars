package org.metaborg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
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
public class BatchDataDependentParsingBenchmark {

    @Param({ "files/withDeepConflicts/files.csv", "files/withoutDeepConflicts/files.csv" })
    public static String b_filename;

    @State(Scope.Benchmark)
    public static class FileConfig {
        public File csvFile;
        public List<String> input;

        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            input = Lists.newArrayList();
            BufferedReader br = null;
            String line = "";
            csvFile = new File("resources/" + a_lang.getLanguageName() + "/" + b_filename);

            br = new BufferedReader(new FileReader(csvFile));
            while((line = br.readLine()) != null) {
                File currentFile = new File(line);
                if(currentFile.exists()) {
                    input.add(FileUtils.readFileToString(currentFile, Charsets.UTF_8));
                }
            }
            br.close();

        }
    }

    @Param({ "OCAML" })
    public static Language a_lang;

    @Param({ "false" })
    public static boolean c_isLazyGeneration;

    @Param({ "false" })
    public static boolean d_isDataDependent;

    @Param({ "false" })
    public static boolean e_solvesDeepConflicts;

    @State(Scope.Benchmark)
    public static class BenchmarkLanguages {
        public ParseTable pt;
        public JSGLR2<?, ?, IStrategoTerm> parser;

        @Setup(Level.Trial)
        public void doSetup() throws Exception {

            if(!e_solvesDeepConflicts && (c_isLazyGeneration || d_isDataDependent)) {
                throw new RuntimeException();
            }

            if(!e_solvesDeepConflicts && b_filename.equals("files/withDeepConflicts/files.csv")) {
                throw new RuntimeException();
            }

            FileSystemManager fsManager = VFS.getManager();
            String pathToParseTable = "resources/parseTables/" + a_lang.getLanguageName() + "/";
            if(c_isLazyGeneration) {
                pathToParseTable += "lazy/";
            } else {
                pathToParseTable += "notLazy/";
            }

            if(d_isDataDependent) {
                pathToParseTable += "dataDependent/";
            } else {
                pathToParseTable += "notDataDependent/";
            }

            if(!e_solvesDeepConflicts) {
                pathToParseTable += "notSolveDeepConflicts/";
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

                ptg.outputTable(c_isLazyGeneration, d_isDataDependent, e_solvesDeepConflicts);
                pt = ptg.getParseTable();
            }

            if(d_isDataDependent) {
                parser = JSGLR2.dataDependent(pt);
            } else {
                parser = JSGLR2.standard(pt);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {

        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(1) 
            .measurementIterations(5)
            .mode(Mode.AverageTime)
            .forks(1)
            .threads(1)
            .include(BatchDataDependentParsingBenchmark.class.getSimpleName())
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
        // int processedSize = 0;
        // int remainingSize = fc.input.size();

        for(String program : fc.input) {
            try {
                // System.out.println(String.format("%d processed, %d remaining files after %s", ++processedSize,
                // --remainingSize, "not available"));
                bh.consume(bl.parser.parse(program));
            } catch(Exception e) {
                System.out.println("could not parse file " + program);
            }
        }
    }
}

