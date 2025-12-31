(defproject org.openvoxproject/ssl-utils "3.5.6-SNAPSHOT"
  :url "http://www.github.com/openvoxproject/jvm-ssl-utils"
  :license {:name "Apache-2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.txt"}

  :description "SSL certificate management on the JVM."

  :min-lein-version "2.9.10"

  :parent-project {:coords [org.openvoxproject/clj-parent "7.5.1"]
                   :inherit [:managed-dependencies]}

  ;; Abort when version ranges or version conflicts are detected in
  ;; dependencies. Also supports :warn to simply emit warnings.
  :pedantic? :abort

  :dependencies [[org.clojure/clojure]
                 [org.clojure/tools.logging]
                 [commons-codec]
                 [clj-commons/fs]
                 [clj-time]
                 [org.openvoxproject/i18n]
                 [prismatic/schema]]

  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :jar-exclusions [#".*\.java$"]

  ;; By declaring a classifier here and a corresponding profile below we'll get an additional jar
  ;; during `lein jar` that has all the source code (including the java source). Downstream projects can then
  ;; depend on this source jar using a :classifier in their :dependencies.
  :classifiers [["sources" :sources-jar]]

  :profiles {:dev {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]
                   :resource-paths ["test-resources"]}

             ;; per https://github.com/technomancy/leiningen/issues/1907
             ;; the provided profile is necessary for lein jar / lein install
             :provided {:dependencies [[org.bouncycastle/bcpkix-jdk18on]]
                        :resource-paths ["test-resources"]}

             :fips {:dependencies [[org.bouncycastle/bctls-fips "2.0.19" :exclusions [org.bouncycastle/bcutil-fips]]
                                   [org.bouncycastle/bcpkix-fips "2.0.7" :exclusions [org.bouncycastle/bcutil-fips]]
                                   [org.bouncycastle/bcutil-fips "2.0.3"]
                                   [org.bouncycastle/bc-fips "2.0.0"]]
                    ;; this only ensures that we run with the proper profiles
                    ;; during testing. This JVM opt will be set in the puppet module
                    ;; that sets up the JVM classpaths during installation.
                    :jvm-opts ~(let [version (System/getProperty "java.specification.version")
                                     [major minor _] (clojure.string/split version #"\.")
                                     unsupported-ex (ex-info "Unsupported major Java version. Expects 17 or 21."
                                                      {:major major
                                                       :minor minor})]
                                 (condp = (java.lang.Integer/parseInt major)
                                   17 ["-Djava.security.properties==jdk17-fips-security"]
                                   21 ["-Djava.security.properties==jdk21-fips-security"]
                                   (throw unsupported-ex)))
                    :resource-paths ["test-resources"]}

             :sources-jar {:java-source-paths ^:replace []
                           :jar-exclusions ^:replace []
                           :source-paths ^:replace ["src/clojure" "src/java"]}}

  :plugins [[lein-parent "0.3.9"]
            [org.openvoxproject/i18n "0.9.4"]
            [jonase/eastwood "1.2.2" :exclusions [org.clojure/clojure]]]

  :eastwood {:exclude-linters [:no-ns-form-found :reflection]
             :continue-on-exception true}

  :lein-release {:scm         :git
                 :deploy-via  :lein-deploy}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD
                                     :sign-releases false}]])
