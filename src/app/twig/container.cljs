
(ns app.twig.container
  (:require [recollect.twig :refer [deftwig]]
            [app.twig.user :refer [twig-user]]
            ["randomcolor" :as color]))

(deftwig
 twig-members
 (sessions users)
 (->> sessions
      (map (fn [[k session]] [k (get-in users [(:user-id session) :name])]))
      (into {})))

(deftwig
 twig-container
 (db session records)
 (let [logged-in? (some? (:user-id session))
       router (:router session)
       base-data {:logged-in? logged-in?, :session session, :reel-length (count records)}
       user (get-in db [:users (:user-id session)])]
   (merge
    base-data
    (if logged-in?
      {:user (twig-user user),
       :router (assoc
                router
                :data
                (case (:name router)
                  :home (:books user)
                  :profile (twig-members (:sessions db) (:users db))
                  :book (get-in user [:books (:data router)])
                  {})),
       :count (count (:sessions db)),
       :color (color/randomColor)}
      nil))))
