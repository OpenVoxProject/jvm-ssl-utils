(def i18n-version "1.0.3")

(defproject org.openvoxproject/ssl-utils "3.6.3-SNAPSHOT"
  :url "http://www.github.com/openvoxproject/jvm-ssl-utils"
  :license {:name "Apache-2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.txt"}

  :description "SSL certificate management on the JVM."

  :min-lein-version "2.9.10"

  ;; Abort when version ranges or version conflicts are detected in
  ;; dependencies. Also supports :warn to simply emit warnings.
  :pedantic? :abort

  ;; Generally, try to keep version pins in :managed-dependencies and the libraries
  ;; this project actually uses in :dependencies, inheriting the version from
  ;; :managed-dependencies. This prevents endless version conflicts due to deps of deps.
  ;; Renovate should keep the versions largely in sync between projects.
  :managed-dependencies [[org.clojure/clojure "1.12.4"]
                         [org.clojure/tools.logging "1.3.1"]
                         [clj-commons/fs "1.6.312"]
                         [clj-time "0.15.2"]
                         [commons-codec "1.20.0"]
                         [commons-io "2.21.0"]
                         [org.bouncycastle/bcpkix-jdk18on "1.83"]
                         [org.bouncycastle/bcpkix-fips "1.0.8"]
                         [org.bouncycastle/bc-fips "1.0.2.6"]
                         [org.bouncycastle/bctls-fips "1.0.19"]
                         [org.openvoxproject/i18n ~i18n-version]
                         [prismatic/schema "1.4.1"]]

  :dependencies [[org.clojure/clojure]
                 [org.clojure/tools.logging]
                 [clj-commons/fs]
                 [clj-time]
                 [commons-codec]
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

             :fips {:dependencies [[org.bouncycastle/bctls-fips]
                                   [org.bouncycastle/bcpkix-fips]
                                   [org.bouncycastle/bc-fips]]
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

  :plugins [[org.openvoxproject/i18n ~i18n-version]
            [jonase/eastwood "1.4.3" :exclusions [org.clojure/clojure]]]

  :eastwood {:exclude-linters [:no-ns-form-found :reflection]
             :continue-on-exception true}

  :lein-release {:scm         :git
                 :deploy-via  :lein-deploy}
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :username :env/CLOJARS_USERNAME
                                     :password :env/CLOJARS_PASSWORD
                                     :sign-releases false}]])
