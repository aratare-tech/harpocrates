(ns shinsetsu.db.user-test
  (:require [clojure.test :refer :all]
            [shinsetsu.db.user :refer :all]
            [shinsetsu.db.core :refer [db]]
            [shinsetsu.config :refer [env]]
            [clojure.test.check.generators :as check-gen]
            [expectations.clojure.test :refer [defexpect expect]]
            [schema.core :as s]
            [next.jdbc :as nj]
            [clojure.java.jdbc :as jdbc]
            [schema-generators.generators :as g]
            [shinsetsu.schemas :refer :all]
            [user :refer [reset-db migrate]]
            [taoensso.timbre :as log]
            [mount.core :as mount]))

(defn migrate-db-fixture
  [f]
  (log/info "Migrating db")
  (mount/start #'env #'db)
  (migrate)
  (f))

(defn reset-db-fixture
  [f]
  (f)
  (log/info "Resetting db")
  (reset-db))

(use-fixtures :once migrate-db-fixture)
(use-fixtures :each reset-db-fixture)

(defexpect user-db
  (let [user     (g/generate User default-leaf-generator)
        user-id  {:user/id (:user/id user)}
        diff     (dissoc (g/generate User default-leaf-generator) :user/id)
        new-user (merge user diff)]
    (expect nil? (read-user user-id))
    (create-user user)
    (expect user (read-user user-id))
    (update-user new-user)
    (expect new-user (read-user user-id))
    (expect new-user (delete-user user-id))
    (expect nil? (read-user user-id))))

(comment
  (-> "HELLO WORLD!" .getBytes)
  (let [byte (g/generate Bytes {Bytes check-gen/bytes})]
    (println (= byte byte)))
  (bytes? (g/generate Bytes {Bytes check-gen/bytes}))
  (require '[eftest.runner :as runner])
  (create-user (g/generate User {Bytes check-gen/bytes}))
  (runner/run-tests [#'shinsetsu.db.user-test/user-db]))

