# liberator-swagger

A Clojure library designed to provide very thin and simple interface layer between standard Liberator applications and Swagger with Spec support (via `spec-tools`).

## Usage

1. Add require `[liberator-swagger.core :refer [defresource swagger-routes make-swagger-object]`
2. Add swagger routes to your compojure routes
```clojure
(defn app-routes
  [{db :db]
  (routes
    ;; Your standard routes
    (ANY "/users/:id" [] (users id db))
    (ANY "/groups" [] (users db))
    ;; Swagger routes
    (swagger-routes {:swagger-object 
                      (make-swagger-object
                        {:info  {:version     "0.0.1"
                                 :title       "Project Title"
                                 :description "Project Description"}
                         :tags  [{:name        "tag1"
                                  :description "First tag"}
                                 {:name        "tag 2"
                                  :description "Second tag"}]
                         :paths {"/users/{id}" #'users
                                 "/groups #'groups}})
                     :ui   "/swagger"
                     :spec "/swagger.json"})
    ;; Catch-all routes, etc
    (route/resources "/")
    (route/not-found "404")))

```
3. Add `:swagger` metadata to `defresource` like:
```clojure
(spec/def ::id uuid?)
(spec/def ::user some-user-definition)

(defresource users [id db]
  {:swagger {:post {:tags                ["tag1"]
                    :summary             "Create new user"
                    ::swagger/parameters {:path (spec/keys :req-un [::id])
                                          :body ::user}
                    ::swagger/responses {200 {:schema ::user}
             :put  {:tags                ["tag1" "tag2"]
                    :summary             "Update specific users"
                    ::swagger/parameters {:path (spec/keys :req-un [::id])
                                          :body ::user}
                    ::swagger/responses {200 {:schema ::user}}}}
  :allowed-methods [:post :put]
  :available-media-types ["application/json"]
  :post! (fn [ctx] (POST-CODE))
  :put! (fn [ctx] (PUT-CODE)))
```


## License

Copyright © 2019 Slawomir Gonet

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
