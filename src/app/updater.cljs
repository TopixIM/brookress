
(ns app.updater
  (:require [app.updater.session :as session]
            [app.updater.user :as user]
            [app.updater.router :as router]
            [app.updater.book :as book]
            [app.schema :as schema]
            [respo-message.updater :refer [update-messages]]))

(defn updater [db op op-data sid op-id op-time]
  (let [f (case op
            :session/connect session/connect
            :session/disconnect session/disconnect
            :session/remove-message session/remove-message
            :user/log-in user/log-in
            :user/sign-up user/sign-up
            :user/log-out user/log-out
            :router/change router/change
            :book/add book/add-book
            :book/merge book/merge-book
            :book/edit-progress book/edit-progress
            :book/remove book/remove-book
            (do (println "Unknown op:" op) identity))]
    (f db op-data sid op-id op-time)))
