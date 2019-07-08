(ns liberator-swagger.core
  (:require [liberator.core :as liberator]
            [spec-tools.swagger.core :as swagger]
            [compojure.api.swagger :as compojure-swagger]
            [compojure.core :refer [routes GET]]
            [clojure.data.json :as json])
  (:use [com.rpl.specter]))

;; https://dnaeon.github.io/clojure-map-ks-paths/
(defn- keys-in
  "Returns a sequence of all key paths in a given map using DFS walk."
  [m]
  (letfn [(children [node]
            (let [v (get-in m node)]
              (if (map? v)
                (map (fn [x] (conj node x)) (keys v))
                [])))
          (branch? [node] (-> (children node) seq boolean))]
    (->> (keys m)
         (map vector)
         (mapcat #(tree-seq branch? children %)))))

(defn- extract-handlers-data
  [metadata]
  (reduce (fn [acc [method desc handler]] (update acc handler merge {method desc}))
          {}
          (concat
            (select [ALL (collect-one FIRST)                ;; collect every HTTP method
                     ALL ::swagger/responses ALL            ;; navigate through every swagger response code
                     (nthpath 1) (collect-one :description) (must :generate-handler)] ;; navigate only if first element in [code {:description .. :generate-handler ...} contains :generate-handler
                    (:swagger metadata))
            (select [ALL (collect-one FIRST)                ;; collect every HTTP method
                     ALL :responses ALL                     ;; navigate through every response code
                     (nthpath 1) (collect-one :description) (must :generate-handler)] ;; navigate only if first element in [code {:description .. :generate-handler ...} contains :generate-handler
                    (:swagger metadata)))))

(defn- generate-handlers
  "Generate handlers from :swagger metadata for each   "
  [metadata]
  (reduce-kv (fn [acc handler-name methods]
               (concat (list handler-name `(fn [ctx#] (get ~, methods (get-in ctx# [:request :request-method]))))
                       acc))
             '()
             (extract-handlers-data metadata)))

(defmacro defresource [name metadata params & body]
  "Wrapper on top of Liberator resource allowing to add metadata to defined function."
  `(defn ~name
     ~metadata
     ~params
     (liberator/resource
       ~@(concat body
                 (generate-handlers metadata)))))

(defn swagger-routes
  "Define Compojure routes for Swagger documentation.
  - `swagger-object` is Swagger object map in ring-swagger format with clojure.spec support.
  - `ui` is Swagger Browser UI URL (defaults to /swagger)
  - `spec` is swagger.json URL (default to /swagger.json)
  "
  [{:keys [swagger-object ui spec]}]
  (routes
    (GET spec _ (json/write-str (swagger/swagger-spec swagger-object)))
    (compojure-swagger/swagger-routes {:ui   (or ui "/swagger")
                                       :spec (or spec "/swagger.json")})))

(defn- cleanup-swagger-structure
  "Cleanup swagger structure from additional fields like `generate-handler`"
  [map]
  (reduce (fn [acc key]
            (update-in acc (butlast key) dissoc (last key)))
          map
          (filter #(= (last %) :generate-handler) (keys-in map))))

(defn- make-paths
  [paths]
  (->>
    (map (fn [[url function-symbol]]
           {url (if (coll? function-symbol)
                  (reduce merge (map #(get (meta %) :swagger) function-symbol))
                  (:swagger (meta function-symbol)))})
         paths)
    (apply merge)
    (cleanup-swagger-structure)))

(defn make-swagger-object
  "Make ring-swagger map object"
  [{:keys [info tags paths]}]
  (-> {:swagger "2.0"}
      (assoc :info info)
      (assoc :tags tags)
      (assoc :paths (make-paths paths))))
