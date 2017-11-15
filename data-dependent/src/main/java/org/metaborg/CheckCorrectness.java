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
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.metaborg.sdf2table.io.ParseTableGenerator;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.JSGLR2;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class CheckCorrectness {

    public static Language[] languages = { Language.JAVA, Language.OCAML };
    public static String files = "files/withDeepConflicts/files.csv";


    public static void main(String[] args) throws Exception {
        JSGLR2<?, ?, IStrategoTerm> parser;
        JSGLR2<?, ?, IStrategoTerm> dataDependentParser;
        ParseTable pt;
        ParseTable dataDependentPt;

        for(Language a_lang : languages) {
            FileSystemManager fsManager = VFS.getManager();
            String pathToParseTable = "resources/parseTables/" + a_lang.getLanguageName() + "/";

            // comparing only non-lazy tables
            pathToParseTable += "notLazy/";

            String pathToDataDependentParseTable = pathToParseTable + "dataDependent/";
            pathToParseTable += "notDataDependent/";

            // comparing ASTs of files without deep conflicts
            pathToParseTable += "notSolveDeepConflicts/";


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

                ptg.outputTable(false, false, false);
                pt = ptg.getParseTable();
            }

            File dataDependentPersistedFile = new File(pathToDataDependentParseTable + "parseTable.bin");
            if(dataDependentPersistedFile.exists()) {
                System.out.println("parse table already exists, importing it");
                FileObject parseTable = fsManager.resolveFile(PTpath, "parseTable.bin");
                ParseTableGenerator ptg = new ParseTableGenerator(parseTable);
                dataDependentPt = ptg.getParseTable();
            } else {
                System.out.println("parse table does not exist, creating it");
                File mainFile = new File("normalizedGrammars/" + a_lang.getLanguageName() + "/normalized/"
                    + a_lang.getMainSDF3Module() + "-norm.aterm");

                ParseTableGenerator ptg = new ParseTableGenerator(mainFile, null, dataDependentPersistedFile, null,
                    Lists.newArrayList("normalizedGrammars/" + a_lang.getLanguageName()));

                ptg.outputTable(false, true, true);
                dataDependentPt = ptg.getParseTable();

            }

            dataDependentParser = JSGLR2.dataDependent(dataDependentPt);
            parser = JSGLR2.naive(pt);

            checkASTFiles(a_lang, parser, dataDependentParser, files);

        }
    }


    private static void checkASTFiles(Language a_lang, JSGLR2<?, ?, IStrategoTerm> parser,
        JSGLR2<?, ?, IStrategoTerm> dataDependentParser, String pathToCsvFile) throws IOException {
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

        File file = new File("Results/correctness-" + a_lang + ".txt");
        final BufferedWriter output = new BufferedWriter(new FileWriter(file));
        
        try {
            input.stream().forEach(program -> {
                try {
                    IStrategoTerm astRegular = parser.parse(program);
                    IStrategoTerm astDataDependent = dataDependentParser.parse(program);
                    if(!astRegular.equals(astDataDependent)) {
                        output.write("AST of data-dependent parser and regular parser is different for program "
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
