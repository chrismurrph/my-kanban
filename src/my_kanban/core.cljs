(ns my-kanban.core
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :as sab :include-macros true]
   [om.dom :as dom]
   [goog.dom :as gdom])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

(enable-console-print!)

(defcard first-card
  (sab/html [:div
             [:h1 "This is your first devcard!"]]))

(defui HelloWorld
       Object
       (render [this]
               (dom/div nil "Hello, world!")))

(def hello (om/factory HelloWorld))

(defn main []
  ;; conditionally start the app based on wether the #main-app-area
  ;; node is on the page
  (js/ReactDOM.render (hello) (gdom/getElement "main-app-area")))

(main)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

