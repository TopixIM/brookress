
(ns app.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> div span action-> cursor-> button]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [respo-message.comp.messages :refer [comp-messages]]
            [cumulo-reel.comp.reel :refer [comp-reel]]
            [app.config :refer [dev?]]
            [app.schema :as schema]
            [app.config :as config]
            [shadow.resource :refer [inline]]
            [cljs.reader :refer [read-string]]
            [cumulo-util.core :refer [id! unix-time!]]
            [composer.core :refer [render-markup extract-templates]]
            [app.vm :as vm]))

(defcomp
 comp-container
 (store states)
 (let [state (:data states)
       session (:session store)
       router (:router store)
       router-data (:data router)
       templates (extract-templates (read-string (inline "composer.edn")))
       view-model (vm/get-view-model store states)]
   (div
    {:style (merge ui/global ui/fullscreen ui/column)}
    (render-markup
     (get templates "container")
     {:data view-model,
      :templates templates,
      :level 1,
      :template-name "container",
      :state-path [],
      :states states,
      :state-fns (->> vm/states-manager
                      (map (fn [[alias manager]] [alias (:init manager)]))
                      (into {}))}
     (fn [d! op context options]
       (vm/on-action d! op (dissoc context :templates :state-fns) options view-model states)))
    (when dev? (comp-inspect "vm" view-model {:bottom 0, :left 0, :max-width "100%"}))
    (comp-messages
     (get-in store [:session :messages])
     {}
     (fn [info d! m!] (d! :session/remove-message info)))
    (when dev? (comp-reel (:reel-length store) {})))))
