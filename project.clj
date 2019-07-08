(defproject liberator-swagger "0.1.4"
  :description "Thin layer interfacing between Liberator and Swagger, for adding API documentation into exisitng Liberator projects with Spec."
  :url "https://github.com/otwieracz/liberator-swagger"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [liberator "0.15.3"]
                 [compojure "1.6.1"]
                 [metosin/spec-tools "0.9.2"]
                 [org.flatland/ordered "1.5.7"]             ;; enforce newer version for jdk11 compatibility
                 [com.rpl/specter "1.1.2"]
                 [metosin/compojure-api "1.1.12"]]
  :repl-options {:init-ns liberator-swagger.core})
