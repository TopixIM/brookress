
(ns app.vm (:require [app.config :as config]))

(defn get-view-model [store local-store]
  {:local local-store, :site config/site, :store store})

(defn on-action [d! op param options view-model]
  (when config/dev? (println "Action" op param (pr-str options)))
  (case op
    :local/username (d! :local-mutate {:path [:username], :value (:value options)})
    :local/password (d! :local-mutate {:path [:password], :value (:value options)})
    :signup
      (let [local (:local view-model), pair [(:username local) (:password local)]]
        (d! :user/sign-up pair)
        (.setItem js/localStorage (:storage-key config/site) pair))
    :login
      (let [local (:local view-model), pair [(:username local) (:password local)]]
        (d! :user/log-in pair)
        (.setItem js/localStorage (:storage-key config/site) pair))
    :logout
      (do (d! :user/log-out nil) (.removeItem js/localStorage (:storage-key config/site)))
    :profile (d! :router/change {:name :profile})
    :home (d! :router/change {:name :home})
    (do (println "Unknown op:" op))))
