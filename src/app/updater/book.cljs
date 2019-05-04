
(ns app.updater.book (:require [app.schema :as schema]))

(defn add-book [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (update-in
     db
     [:users user-id :books]
     (fn [books] (assoc books op-id (merge schema/book op-data {:id op-id, :time op-time}))))))

(defn edit-progress [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])
        book-id (:id op-data)
        progress (:progress op-data)]
    (update-in
     db
     [:users user-id :books book-id]
     (fn [book] (assoc book :progress progress :time op-time)))))

(defn merge-book [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id]), book-id (:id op-data)]
    (update-in
     db
     [:users user-id :books book-id]
     (fn [book] (merge book op-data {:time op-time})))))

(defn remove-book [db op-data sid op-id op-time]
  (let [user-id (get-in db [:sessions sid :user-id])]
    (update-in db [:users user-id :books] (fn [books] (dissoc books op-data)))))
