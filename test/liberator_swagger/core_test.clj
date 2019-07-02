(ns liberator-swagger.core-test
  (:require [clojure.test :refer :all]
            [liberator-swagger.core :as core]
            [spec-tools.swagger.core :as swagger]))

(def test-metadata
  {:swagger {:post {:tags                ["resources"]
                    :summary             "Define new resource"
                    ::swagger/parameters {:body ::resource-definition}
                    ::swagger/responses  {201 {:description "Resource created"}
                                          400 {:generate-handler :handle-malformed
                                               :description      "Malformed resource definition"}}}
             :get  {:tags                ["resources"]
                    :summary             "Define new resource"
                    ::swagger/parameters {:body ::resource-definition}
                    ::swagger/responses  {404 {:generate-handler :handle-not-found
                                               :description      "Not found"
                                               }
                                          400 {:generate-handler :handle-malformed
                                               :description      "Malformed resource definition"}}}}})


(deftest extract-handlers-data-test
  (is (= {:handle-malformed {:post "Malformed resource definition", :get "Malformed resource definition"},
          :handle-not-found {:get "Not found"}}
         (#'core/extract-handlers-data test-metadata))))

(deftest generate-handlers-test
  (is (= '(:handle-not-found (fn [ctx] (get (get-in ctx [:request :method]) methods))
            :handle-malformed (fn [ctx] (get (get-in ctx [:request :method]) methods)))
         (#'core/generate-handlers test-metadata))))
