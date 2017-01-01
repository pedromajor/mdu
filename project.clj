(defproject mdu "0.1.0"
  :description "My cosmetic wrapper for *nix du"
  :url "https://github.com/pedromajor/mdu"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [jansi-clj "0.1.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :aot [du.core]
  :main du.core
  ;; for lein-bin plugin
  :bin {:name "mdu"})
