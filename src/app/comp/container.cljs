
(ns app.comp.container
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp <> div span action-> cursor-> button]]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.comp.space :refer [=<]]
            [app.comp.navigation :refer [comp-navigation]]
            [app.comp.profile :refer [comp-profile]]
            [app.comp.login :refer [comp-login]]
            [respo-message.comp.messages :refer [comp-messages]]
            [cumulo-reel.comp.reel :refer [comp-reel]]
            [app.config :refer [dev?]]
            [app.schema :as schema]
            [app.config :as config]
            [shadow.resource :refer [inline]]
            [cljs.reader :refer [read-string]]
            [cumulo-util.core :refer [id! unix-time!]]
            [composer.core :refer [render-markup extract-templates]]))

(defcomp
 comp-offline
 ()
 (div
  {:style (merge
           ui/global
           ui/fullscreen
           ui/column-dispersive
           {:background-color (:theme config/site)})}
  (div {:style {:height 0}})
  (div
   {:style {:background-image (str "url(" (:icon config/site) ")"),
            :width 128,
            :height 128,
            :background-size :contain}})
  (div
   {:style {:cursor :pointer, :line-height "32px"},
    :on-click (action-> :effect/connect nil)}
   (<> "No connection..." {:font-family ui/font-fancy, :font-size 24}))))

(defcomp
 comp-container
 (states store view-model on-action)
 (let [state (:data states)
       session (:session store)
       router (:router store)
       router-data (:data router)
       templates (extract-templates (read-string (inline "composer.edn")))]
   (if (nil? store)
     (comp-offline)
     (div
      {:style (merge ui/global ui/fullscreen ui/column)}
      (render-markup
       (get templates "container")
       {:data view-model, :templates templates, :level 1}
       (fn [d! op param options] (on-action d! op param options view-model)))
      (when dev? (comp-inspect "vm" view-model {:bottom 0, :left 0, :max-width "100%"}))
      (comp-messages
       (get-in store [:session :messages])
       {}
       (fn [info d! m!] (d! :session/remove-message info)))
      (when dev? (comp-reel (:reel-length store) {}))))))

(defcomp
 comp-status-color
 (color)
 (div
  {:style (let [size 24]
     {:width size,
      :height size,
      :position :absolute,
      :bottom 60,
      :left 8,
      :background-color color,
      :border-radius "50%",
      :opacity 0.6,
      :pointer-events :none})}))

(def style-body {:padding "8px 16px"})
