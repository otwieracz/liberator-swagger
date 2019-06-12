(ns liberator-swagger.core
  (:require [liberator.core :as liberator]
            [spec-tools.swagger.core :as swagger]
            [compojure.api.swagger :as compojure-swagger]
            [compojure.core :refer [routes GET]]
            [clojure.data.json :as json]))

(defmacro defresource [name metadata params & body]
  "Wrapper on top of Liberator resource allowing to add metadata to defined function."
  `(defn ~name
     ~metadata
     ~params
     (liberator/resource
       ~@body)))

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

(defn- make-paths
  [paths]
  (apply merge
         (map (fn [[url function-symbol]]
                {url (:swagger (meta function-symbol))})
              paths)))

(defn make-swagger-object
  "Make ring-swagger map object"
  [{:keys [info tags paths]}]
  (-> {:swagger "2.0"}
      (assoc :info info)
      (assoc :tags tags)
      (assoc :paths (make-paths paths))))
