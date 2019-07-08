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
                    :responses           {404 {:generate-handler :handle-not-found
                                               :description      "Not found"
                                               }
                                          400 {:generate-handler :handle-malformed
                                               :description      "Malformed resource definition"}}}}})

(deftest extract-handlers-data-test
  (is (= {:handle-malformed {:post "Malformed resource definition", :get "Malformed resource definition"},
          :handle-not-found {:get "Not found"}}
         (#'core/extract-handlers-data test-metadata))))

(deftest cleanup-swagger-structure-test
  (is (= {:post {:tags                ["resources"]
                 :summary             "Define new resource"
                 ::swagger/parameters {:body ::resource-definition}
                 ::swagger/responses  {201 {:description "Resource created"}
                                       400 {:description "Malformed resource definition"}}}
          :get  {:tags                ["resources"]
                 :summary             "Define new resource"
                 ::swagger/parameters {:body ::resource-definition}
                 :responses           {404 {:description "Not found"}
                                       400 {:description "Malformed resource definition"}}}}
         (#'core/cleanup-swagger-structure (:swagger test-metadata)))))
