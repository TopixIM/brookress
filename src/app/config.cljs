
(ns app.config )

(def cdn?
  (cond
    (exists? js/window) false
    (exists? js/process) (= "true" js/process.env.cdn)
    :else false))

(def dev?
  (let [debug? (do ^boolean js/goog.DEBUG)]
    (if debug?
      (cond
        (exists? js/window) true
        (exists? js/process) (not= "true" js/process.env.release)
        :else true)
      false)))

(def site
  {:port 11017,
   :title "Brookress",
   :icon "http://cdn.tiye.me/logo/brookress.png",
   :dev-ui "http://localhost:8100/main-fonts.css",
   :release-ui "http://cdn.tiye.me/favored-fonts/main-fonts.css",
   :cdn-url "http://cdn.tiye.me/brookress/",
   :cdn-folder "tiye.me:cdn/brookress",
   :upload-folder "tiye.me:repo/TopixIM/brookress/",
   :server-folder "tiye.me:servers/brookress",
   :theme "#eeeeff",
   :storage-key "brookress",
   :storage-file "storage.edn"})
