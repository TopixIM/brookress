
(ns app.schema )

(def note {:id "", :content "", :time 0, :at-page 0})

(def book
  {:id "", :name "", :total-pages 0, :progress 0, :notes (do note {}), :sort-key "", :time 0})

(def router {:name nil, :title nil, :data {}, :router nil})

(def session
  {:user-id nil,
   :id nil,
   :nickname nil,
   :router (do router {:name :home, :data nil, :router nil}),
   :messages {}})

(def user {:name nil, :id nil, :nickname nil, :avatar nil, :password nil})

(def database {:sessions (do session {}), :users (do user {}), :books (do book {})})
