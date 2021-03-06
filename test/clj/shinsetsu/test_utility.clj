(ns shinsetsu.test-utility
  (:require [clojure.test :refer :all]
            [clj-test-containers.core :as tc]
            [shinsetsu.db.ext]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [migratus.core :as migratus]
            [taoensso.timbre :as log]
            [cognitect.transit :as transit])
  (:import [com.zaxxer.hikari HikariDataSource]
           [java.io ByteArrayInputStream ByteArrayOutputStream]))

(def username "shinsetsu-test")
(def password "shinsetsu-test")

(defn get-container
  [db-name]
  (-> (tc/create {:image-name    "postgres:12.1"
                  :exposed-ports [5432]
                  :env-vars      {"POSTGRES_DB"       db-name
                                  "POSTGRES_USER"     username
                                  "POSTGRES_PASSWORD" password}})
      (tc/bind-filesystem! {:host-path      "/tmp"
                            :container-path "/opt"
                            :mode           :read-only})
      (tc/start!)))

(defn get-db-map
  [db-name host port]
  {:classname "com.postgresql.Driver"
   :dbtype    "postgresql"
   :dbname    db-name
   :host      host
   :port      port
   :user      username
   :username  username
   :password  password})

(defn get-migratus-config
  [db-name host port]
  {:store         :database
   :migration-dir "migrations/"
   :db            (get-db-map db-name host port)})

(defn get-db
  [db-name host port]
  (jdbc/with-options
    (connection/->pool HikariDataSource (get-db-map db-name host port))
    jdbc/snake-kebab-opts))

(defn get-db-fixture
  [db-name]
  (let [container       (get-container db-name)
        host            (:host container)
        port            (get-in container [:mapped-ports 5432])
        migratus-config (get-migratus-config db-name host port)
        db              (get-db db-name host port)]
    {:db      db
     :fixture {:once (fn [f]
                       (migratus/migrate migratus-config)
                       (f)
                       (tc/stop! container))
               :each (fn [f]
                       (f)
                       (migratus/reset migratus-config))}}))

(defn ->transit
  [v]
  (with-open [out (ByteArrayOutputStream. 4096)]
    (let [writer (transit/writer out :json)]
      (transit/write writer v)
      (.toString out))))