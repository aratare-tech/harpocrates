(ns shinsetsu.parser
  (:require
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]
    [taoensso.timbre :as log]
    [shinsetsu.resolvers]
    [shinsetsu.mutations]
    [puget.printer :refer [pprint]]))

(def resolvers [shinsetsu.resolvers/resolvers
                shinsetsu.mutations/mutations])

(defn process-error
  "Overriding the default Pathom error handler so we can get the attached data on the client side."
  [env err]
  (.getData err))

(def pathom-parser
  (p/parser {::p/env     {::p/reader                 [p/map-reader
                                                      pc/reader2
                                                      pc/open-ident-reader]
                          ;::p/process-error          process-error
                          ::pc/mutation-join-globals [:tempids]}
             ::p/mutate  pc/mutate
             ::p/plugins [(pc/connect-plugin {::pc/register resolvers})
                          (p/env-plugin {:hello "hello"})
                          (p/post-process-parser-plugin p/elide-not-found)
                          p/error-handler-plugin]}))

(defn api-parser [query]
  (log/info "Process" query)
  (pathom-parser {} query))