
(ns app.vm (:require [app.config :as config] [respo.util.list :refer [map-val]]))

(defn add-percent [book]
  (assoc book :percent (str (.toFixed (* 100 (/ (:progress book) (:total-pages book)))) "%")))

(defn get-view-model [store local-store]
  {:local local-store,
   :site config/site,
   :store (update
           store
           :router
           (fn [router]
             (case (:name router)
               :home
                 (update router :data (fn [data] (->> data (map-val add-percent) (into {}))))
               router))),
   :dev? config/dev?})

(defn on-action [d! op param options view-model]
  (when config/dev? (println "Action" op param (pr-str options)))
  (let [local (:local view-model)
        login-pair [(get-in local [:login :username]) (get-in local [:login :password])]
        mutate! (fn [path value] (d! :local-mutate [path value]))]
    (case op
      :local/username (mutate! [:login :username] (:value options))
      :local/password (mutate! [:login :password] (:value options))
      :local/book-name (mutate! [:book-form :name] (:value options))
      :local/book-total-pages (mutate! [:book-form :total-pages] (:value options))
      :local/book-progress (mutate! [:book-form :progress] (:value options))
      :book/cancel (d! :router/change {:name :home})
      :local/book-progress-value (mutate! [:book :progress] (:value options))
      :book/submit
        (let [book-data (get-in view-model [:local :book-form])]
          (if (:id book-data) (d! :book/merge book-data) (d! :book/add book-data))
          (d! :router/change {:name :home}))
      :book/edit (do (mutate! [:book-form] param) (d! :router/change {:name :add-book}))
      :book/remove (do (mutate! [:book :show-remove?] true))
      :book/edit-progress (do (mutate! [:book] {:progress param, :show-editor? true}))
      :book/submit-progress
        (do
         (d! :book/edit-progress {:id param, :progress (get-in local [:book :progress])})
         (mutate! [:book :show-editor?] false))
      :book/confirm-remove
        (do
         (d! :book/remove param)
         (mutate! [:book :show-remove?] false)
         (d! :router/change {:name :home}))
      :router/add-book
        (do (mutate! [:book-form] {}) (d! :router/change {:name :add-book, :data nil}))
      :router/view-book (d! :router/change {:name :book, :data param})
      :signup
        (do
         (d! :user/sign-up login-pair)
         (.setItem js/localStorage (:storage-key config/site) login-pair))
      :login
        (do
         (d! :user/log-in login-pair)
         (.setItem js/localStorage (:storage-key config/site) login-pair))
      :logout
        (do (d! :user/log-out nil) (.removeItem js/localStorage (:storage-key config/site)))
      :profile (d! :router/change {:name :profile})
      :home (d! :router/change {:name :home})
      (do (println "Unknown op:" op)))))
