
(ns app.vm (:require [app.config :as config] [respo.util.list :refer [map-val]]))

(defn add-percent [book]
  (assoc book :percent (str (.toFixed (* 100 (/ (:progress book) (:total-pages book)))) "%")))

(defn get-view-model [store states]
  {:site config/site,
   :store (if (nil? store)
     nil
     (update
      store
      :router
      (fn [router]
        (case (:name router)
          :home (update router :data (fn [data] (->> data (map-val add-percent) (into {}))))
          router)))),
   :dev? config/dev?,
   :states states})

(def state-book-card
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op
       :view-book (d! :router/change {:name :book, :data (:param options)})
       (println "Unhandled op" op)))})

(def state-book-form
  {:init (fn [props state]
     (println "book form props" props)
     (or state props {:name "", :total-pages 0, :progress 0})),
   :update (fn [d! op context options state mutate!]
     (case op
       :name (mutate! (assoc state :name (:value options)))
       :total-pages (mutate! (assoc state :total-pages (:value options)))
       :progress (mutate! (assoc state :progress (:value options)))
       :cancel (do (d! :router/change {:name :home}) (mutate! nil))
       :submit
         (let [book-data state]
           (if (:id book-data) (d! :book/merge book-data) (d! :book/add book-data))
           (mutate! nil)
           (d! :router/change {:name :home}))
       (println "Unhandled op" op)))})

(def state-book-overview
  {:init (fn [props state]
     (or state props {:show-remove? false, :show-editor? false, :progress 0})),
   :update (fn [d! op context options state mutate!]
     (case op
       :open-editor (d! :router/change {:name :edit-book, :data (:id (:param options))})
       :show-progress-editor
         (mutate! (assoc state :progress (:param options) :show-editor? true))
       :show-remove (mutate! (assoc state :show-remove? true))
       :progress (mutate! (assoc state :progress (:value options)))
       :submit-progress
         (do
          (d! :book/edit-progress {:id (:param options), :progress (:progress state)})
          (mutate! (assoc state :show-editor? false)))
       :confirm-remove
         (do
          (d! :book/remove (:param options))
          (mutate! (assoc state :show-remove? false))
          (d! :router/change {:name :home}))
       :cancel-remove (mutate! (assoc state :show-remove? false))
       :cancel-progress (mutate! (assoc state :show-editor? false))
       (println "Unhandled op" op)))})

(def state-header
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op
       :home (d! :router/change {:name :home})
       :profile (d! :router/change {:name :profile})
       (println "Unhandled op:" op)))})

(def state-login
  {:init (fn [props state] (or state {:username "", :password ""})),
   :update (fn [d! op context options state mutate!]
     (case op
       :username (mutate! (assoc state :username (:value options)))
       :password (mutate! (assoc state :password (:value options)))
       :signup
         (let [login-pair [(:username state) (:password state)]]
           (d! :user/sign-up login-pair)
           (.setItem js/localStorage (:storage-key config/site) login-pair))
       :login
         (let [login-pair [(:username state) (:password state)]]
           (d! :user/log-in login-pair)
           (.setItem js/localStorage (:storage-key config/site) login-pair))
       (println "Unhandled op:" op)))})

(def state-offline
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op :reconnect (d! :effect/connect nil) (println "not handled op" op)))})

(def state-profile
  {:init (fn [props state] state),
   :update (fn [d! op context options state m!]
     (case op
       :logout
         (do (d! :user/log-out nil) (.removeItem js/localStorage (:storage-key config/site)))
       (println "Unhandled op" op)))})

(def state-workspace
  {:init (fn [props state] state),
   :update (fn [d! op context options state mutate!]
     (case op
       :add-book
         (do (mutate! [:book-form] {}) (d! :router/change {:name :add-book, :data nil}))
       (println "Unhandled op" op)))})

(def states-manager
  {"book-card" state-book-card,
   "book-form" state-book-form,
   "workspace" state-workspace,
   "book-overview" state-book-overview,
   "offline" state-offline,
   "profile" state-profile,
   "header" state-header,
   "login" state-login})

(defn on-action [d! op context options view-model states]
  (when config/dev? (println "Action" op context options))
  (let [param (:param options)
        template-name (:template-name context)
        state-path (:state-path context)
        mutate! (fn [x] (d! :states [state-path x]))
        this-state (get-in states (conj state-path :data))]
    (if (contains? states-manager template-name)
      (let [action-handler (get-in states-manager [template-name :update])
            state-fn (get-in states-manager [template-name :init])
            state (if (fn? state-fn) (state-fn (:data context) this-state) this-state)]
        (action-handler d! op context options state mutate!))
      (println "Unhandled template:" template-name))))
