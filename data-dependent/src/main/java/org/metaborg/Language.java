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
    };
    
    public abstract String getLanguageName();
    public abstract String getMainSDF3Module();
}
