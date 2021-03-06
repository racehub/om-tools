(ns om-tools.schema-test
  (:require-macros
   [om-tools.test-utils :refer [with-element]]
   [schema.macros :as sm])
  (:require
   [om-tools.schema :as schema]
   [om-tools.core :as ot :include-macros true :refer [defcomponentk]]
   [om-tools.dom :as dom :include-macros true]
   [schema.core :as s]
   [schema.test :as schema-test]
   [om.core :as om]
   [cemerick.cljs.test :as t :include-macros true :refer [deftest is use-fixtures]]))

(sm/defschema Item
  {:id s/Int :name s/Str})

(sm/defschema ItemScores
  {s/Int s/Num})

(sm/defschema App
  {:items [Item]
   :item-scores ItemScores})

(sm/defschema ItemList
  {:items (schema/cursor [Item])      ;; om.core/IndexedCursor
   :scores (schema/cursor ItemScores) ;; om.core/MapCursor
   :order s/Keyword})

(defcomponentk item-list [[:data items scores order] :- ItemList]
  (render [_]
    (let [comp-fn (if (= order :asc) compare (comp - compare))]
      (dom/ul
       (for [item (sort-by :name comp-fn items)]
         (dom/li
          (str (:name item) "-" (get scores (:id item)))))))))

(defcomponentk app [[:data items item-scores] :- (schema/cursor App)]
  (render [_]
    (dom/div
     (om/build item-list
               {:items items
                :scores item-scores
                :order :asc}))))

(deftest app-schema-test
  (with-element [e "div"]
    (om/root
     app
     {:items [{:id 1 :name "Common Lisp"}
              {:id 2 :name "Clojure"}
              {:id 3 :name "ClojureScript"}]
      :item-scores {1 1.0, 2 1.0, 3 1.0}}
     {:target e})
    (is (not (clojure.string/blank? (.-innerHTML e))))))

(use-fixtures :once schema-test/validate-schemas)
