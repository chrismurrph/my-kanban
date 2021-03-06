(ns my-kanban.core
  (:require
   [om.next :as om :refer-macros [defui]]
   [sablono.core :as sab :include-macros true]
   [om.dom :as dom]
   [goog.dom :as gdom]
   [kanban.reconciler :refer [reconciler]]
   [kanban.parsing.boards :as boards]
   [kanban.components.boards-menu :refer [BoardMenuItem boards-menu]]
   [kanban.components.board :refer [Board board]]
   [kanban.components.board-dialog :refer [BoardDialog board-dialog]]
   [kanban.components.card :refer [Assignee Card]]
   [kanban.components.card-dialog :refer [CardDialog card-dialog]]
   [kanban.components.lane :refer [Lane]]
   [kanban.components.about :refer [about]]
   [kanban.utils :as u])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

;(enable-console-print!)
;
;(defcard first-card
;  (sab/html [:div
;             [:h1 "This is your first devcard!"]]))
;
;(defui HelloWorld
;       Object
;       (render [this]
;               (dom/div nil "Hello, world!")))
;
;(def hello (om/factory HelloWorld))

(defn computed [component some-map]
  (u/log "COMP: " component "\n")
  (om/computed component some-map))

(defui App
       static om/IQuery
       (query [this]
              [{:boards (om/get-query Board)}
               {:boards/active (om/get-query Board)}
               {:boards/editing (om/get-query BoardDialog)}
               {:lanes (om/get-query Lane)}
               {:cards (om/get-query Card)}
               :cards/dragged
               {:cards/editing (om/get-query CardDialog)}
               {:users (om/get-query Assignee)}])
       Object
       (board-activate [this ref]
                       (u/log "Activating " ref)
                       (om/transact! this `[(boards/activate {:ref ~ref})]))

       (board-create [this]
                     (om/transact! this `[(boards/create-board)]))

       (board-update [this board data]
                     (om/transact! this `[(boards/update {:board ~board :data ~data})]))

       (board-edit [this board]
                   (om/transact! this `[(boards/edit {:board ~board})]))

       (card-drag-start [this lane card]
                        (om/transact! this `[(cards/drag {:lane ~lane :card ~card})]))

       (card-drag-end [this lane card]
                      (om/transact! this `[(cards/drag nil)]))

       (card-drag-drop [this lane]
                       (if-let [source (-> this om/props :cards/dragged)]
                         (om/transact! this `[(lanes/move-card {:card ~(:card source)
                                                                :from ~(:lane source)
                                                                :to   ~lane})
                                              (cards/drag nil)])))

       (card-drag-delete [this]
                         (if-let [source (-> this om/props :cards/dragged)]
                           (om/transact! this `[(lanes/delete-card {:card ~(:card source)
                                                                    :lane ~(:lane source)})
                                                (cards/drag nil)])))

       (card-create [this lane]
                    (om/transact! this `[(lanes/create-card {:lane ~lane})]))

       (card-edit [this card]
                  (om/transact! this `[(cards/edit {:card ~card})]))

       (card-update [this card data]
                    (om/transact! this `[(cards/update {:card ~card :data ~data})]))

       (render [this]
               (dom/div #js {:className "app"}
                        (dom/header #js {:className "header"}
                                    (dom/h1 nil
                                            (dom/a #js {:onClick #(.board-activate this nil)}
                                                   "Om Next Kanban Demo"))
                                    (dom/nav nil
                                             (let [props (-> this om/props (select-keys [:boards]))]
                                               (boards-menu
                                                 (om/computed props
                                                              {:activate-fn #(.board-activate this %)
                                                               :create-fn #(.board-create this)})))))
                        (dom/main nil
                                  (let [active-board (get-in (om/props this) [:boards 0])]
                                    (board
                                      (computed active-board
                                                {:dragging (-> this om/props :cards/dragged)
                                                 :edit-fn #(.board-edit this %)
                                                 :card-create-fn #(.card-create this %)
                                                 :card-edit-fn #(.card-edit this %)
                                                 :card-drag-fns {:start #(.card-drag-start this %1 %2)
                                                                 :end #(.card-drag-end this %1 %2)
                                                                 :drop #(.card-drag-drop this %)
                                                                 :delete #(.card-drag-delete this)}})))
                                  (if-let [board (-> this om/props :boards/editing)]
                                    (board-dialog
                                      (om/computed board {:close-fn #(.board-edit this nil)
                                                          :update-fn #(.board-update this %1 %2)})))
                                  (if-let [card (-> this om/props :cards/editing)]
                                    (card-dialog
                                      (om/computed card {:users (-> this om/props :users)
                                                         :close-fn #(.card-edit this nil)
                                                         :update-fn #(.card-update this %1 %2)})))))))


(defn run []
  (om/add-root! reconciler
                App
                (.. js/document (getElementById "main-app-area"))))

;(defn main []
;  ;; conditionally start the app based on wether the #main-app-area
;  ;; node is on the page
;  (js/ReactDOM.render (hello) (gdom/getElement "main-app-area")))

(run)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

