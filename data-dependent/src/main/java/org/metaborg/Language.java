package org.metaborg;

public enum Language {
    OCAML {
        @Override
        public String getLanguageName() {
            return "OCaml";
        }

        @Override
        public String getMainSDF3Module() {
            return "OCaml";
        }
    }, JAVA {
        @Override
        public String getLanguageName() {
            return "Java";
        }

        @Override
        public String getMainSDF3Module() {
            return "java-front";
        }
    }, TEST {

        @Override
        public String getLanguageName() {
            return "Test";
        }

        @Override
        public String getMainSDF3Module() {
            return "Test";
        }
        
    };
    
    public abstract String getLanguageName();
    public abstract String getMainSDF3Module();
}
