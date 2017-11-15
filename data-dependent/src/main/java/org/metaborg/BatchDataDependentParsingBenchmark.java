package org.metaborg;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@State(Scope.Benchmark)
public class BatchDataDependentParsingBenchmark {

    @Param({ "files/withDeepConflicts", "files/withoutDeepConflicts" })
    public static String b_filepath;

    @State(Scope.Benchmark)
    public static class FileConfig {
        public File csvFile;
        public List<String> input;

        @Setup(Level.Trial)
        public void doSetup() throws IOException {
            final Path filesThatDoNotParsePath =
                    Paths.get("resources/" + a_lang.getLanguageName() + "/" + b_filepath + "/" + "files-do-not-parse.csv");

            final Set<String> filesThatDoNotParse =
                    Files.readAllLines(filesThatDoNotParsePath).stream().collect(Collectors.toSet());

            input = Lists.newArrayList();
            BufferedReader br = null;
            String line = "";
            csvFile = new File("resources/" + a_lang.getLanguageName() + "/" + b_filepath + "/" + "files.csv");

            br = new BufferedReader(new FileReader(csvFile));
            while((line = br.readLine()) != null) {
                File currentFile = new File(line);
                if(currentFile.exists() && !filesThatDoNotParse.contains(line)) {
                    input.add(FileUtils.readFileToString(currentFile, Charsets.UTF_8));
                }
            }
            br.close();

            final String totalCharacterCountString = String.valueOf(characterCount());
            Files.write(
                    Paths.get("resources/" + a_lang.getLanguageName() + "/" + b_filepath + "/" + "files.csv.size.txt"),
                    totalCharacterCountString.getBytes());
        }

        public long characterCount() {
            if (input == null) {
                return 0L;
            } else {
                long characterCount = input.stream()
                        .mapToInt(String::length)
                        .sum();

                return characterCount;
            }
        }
    }

    @Param({ "JAVA" }) // "OCAML", "JAVA"
    public static Language a_lang;

    @Param({ "false" })
    public static boolean c_isLazyGeneration;

    @Param({ "true", "false" })
    public static boolean d_isDataDependent;

    @Param({ "true", "false" })
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

            if(!e_solvesDeepConflicts && b_filepath.equals("files/withDeepConflicts")) {
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
                parser = JSGLR2.naive(pt);
            }
        }
    }

    public static void main(String[] args) throws RunnerException {

        // @formatter:off
        Options options = new OptionsBuilder()
            .warmupIterations(10)
            .measurementIterations(15)
            .mode(Mode.Throughput)
                .param("a_lang", "JAVA")
                .param("a_lang", "OCAML")
                .param("b_filename", "files/withDeepConflicts")
                .param("b_filename", "files/withoutDeepConflicts")
                .param("c_isLazyGeneration", "true")
                .param("c_isLazyGeneration", "false")
                .param("d_isDataDependent", "true")
                .param("d_isDataDependent", "false")
                .param("e_solvesDeepConflicts", "true")
                .param("e_solvesDeepConflicts", "false")
            .forks(1)
            .threads(1)
            .shouldDoGC(true)
            .include(BatchDataDependentParsingBenchmark.class.getSimpleName() + ".parseFilesWithCharacterLimit")
            .timeUnit(TimeUnit.SECONDS)
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
        fc.input.stream().forEach(program -> {
            try {
                bh.consume(bl.parser.parseUnsafe(program));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Benchmark
    public void parseFilesWithFileLimit(Blackhole bh, BenchmarkLanguages bl, FileConfig fc) throws IOException {
        final int fileLimit = 1_000;

        fc.input.stream()
                .limit(fileLimit)
                .forEach(program -> {
                    try {
                        bh.consume(bl.parser.parseUnsafe(program));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
        });

//        long characterCount = fc.input.stream()
//                .limit(fileLimit)
//                .mapToInt(String::length)
//                .sum();
//
//        System.out.println(characterCount);
    }


    @Benchmark
    public void parseFilesWithCharacterLimit(Blackhole bh, BenchmarkLanguages bl, FileConfig fc) throws IOException {
        long characterCount = 0;
        long characterLimit = 1_000;

        final Iterator<String> inputIterator = fc.input.iterator();

        while (inputIterator.hasNext() && characterCount < characterLimit) {
            final String program = inputIterator.next();

            try {
                bh.consume(bl.parser.parseUnsafe(program));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            characterCount += program.length();
        }

        if (characterCount < characterLimit) {
            throw new RuntimeException(String.format("Processed less than %d characters.", characterCount));
        }

        // System.out.println(characterCount);
    }

}

