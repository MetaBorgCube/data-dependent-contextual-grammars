package org.metaborg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class CheckCorrectness {

    public static Language[] languages = { Language.TEST };
    public static String filesWithDeepConflicts = "files/withDeepConflicts/files.csv";
    public static String filesWithoutDeepConflicts = "files/withoutDeepConflicts/files.csv";


    public static void main(String[] args) throws Exception {
        JSGLR2<?, ?, IStrategoTerm> parser;
        JSGLR2<?, ?, IStrategoTerm> dataDependentParser;
        JSGLR2<?, ?, IStrategoTerm> contextualGrammarParser;
        ParseTable notSolveDeepConflictsPt;
        ParseTable dataDependentPt;
        ParseTable contextualPt;

        for(Language a_lang : languages) {

            String pathToParseTable = "resources/parseTables/" + a_lang.getLanguageName() + "/";

            // comparing only non-lazy tables
            pathToParseTable += "notLazy/";

            String pathToDataDependentParseTable = pathToParseTable + "dataDependent/";
            pathToParseTable += "notDataDependent/";

            String pathToContextualGrammarPt = pathToParseTable;
            // comparing ASTs of files without deep conflicts
            pathToParseTable += "notSolveDeepConflicts/";

            notSolveDeepConflictsPt = generateParseTable(a_lang, pathToParseTable, false, false, false);
            dataDependentPt = generateParseTable(a_lang, pathToDataDependentParseTable, false, true, true);
            contextualPt = generateParseTable(a_lang, pathToContextualGrammarPt, false, false, true);

            parser = JSGLR2.naive(notSolveDeepConflictsPt);
            dataDependentParser = JSGLR2.dataDependent(dataDependentPt);
            contextualGrammarParser = JSGLR2.naive(contextualPt);

            // compares the ASTs of files without deep conflicts using regular parser and data dependent one
            checkASTFiles(a_lang, parser, dataDependentParser, filesWithoutDeepConflicts,
                "correctness-withoutConflicts-" + a_lang);

            // compares the ASTs of files with deep conflicts using parser with regular contextual grammar and data
            // dependent parser + data dependent grammar
            checkASTFiles(a_lang, contextualGrammarParser, dataDependentParser, filesWithDeepConflicts,
                "correctness-withConflicts-" + a_lang);
        }
    }


    private static ParseTable generateParseTable(Language a_lang, String pathToParseTable, boolean dynamic,
        boolean dataDependent, boolean solvesDeepConflicts) throws FileSystemException, Exception {
        ParseTable pt;
        ParseTable pt2;
        FileSystemManager fsManager = VFS.getManager();
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

            File atermFile = new File(pathToParseTable + "sdf.tbl");

            ParseTableGenerator ptg = new ParseTableGenerator(mainFile, atermFile, persistedFile, null,
                Lists.newArrayList("normalizedGrammars/" + a_lang.getLanguageName()));

            ptg.outputTable(dynamic, dataDependent, solvesDeepConflicts);
            pt = ptg.getParseTable();
        }
        return pt;
    }


    private static void checkASTFiles(Language a_lang, JSGLR2<?, ?, IStrategoTerm> parser,
        JSGLR2<?, ?, IStrategoTerm> parser2, String pathToCsvFile, String outputFile) throws IOException {
        List<String> input = Lists.newArrayList();
        List<String> programs = Lists.newArrayList();
        BufferedReader br = null;
        String line = "";
        File csvFile = new File("resources/" + a_lang.getLanguageName() + "/" + pathToCsvFile);

        br = new BufferedReader(new FileReader(csvFile));
        while((line = br.readLine()) != null) {
            File currentFile = new File(line);
            if(currentFile.exists()) {
                input.add(FileUtils.readFileToString(currentFile, Charsets.UTF_8));
                programs.add(line);
            }
        }
        br.close();

        File file = new File("Results/" + outputFile + ".txt");
        final BufferedWriter output = new BufferedWriter(new FileWriter(file));

        try {
            input.stream().forEach(program -> {
                try {
                    IStrategoTerm astParser1 = parser.parse(program);
                    IStrategoTerm astParser2 = parser2.parse(program);
                    if(!astParser1.equals(astParser2)) {
                        output.write("ASTs of the different parsers are different for program "
                            + programs.get(input.indexOf(program)) + "\n");
                    } else {
                        output.write("Processed File: " + programs.get(input.indexOf(program)) + "\n");
                    }
                } catch(Exception e) {
                    try {
                        output.write("Could not parse file: " + programs.get(input.indexOf(program)) + "\n");
                    } catch(IOException e1) {
                        System.out.println("output is invalid");
                    }
                }
            });
        } finally {
            output.close();
        }
    }
}
