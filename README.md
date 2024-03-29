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
                                 "/group/{id}" [#'delete-group #'update-group]}})
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

### Handlers based on description

In order to simplify error handling and reply with meaningful error message after, for example, `:malformed?` check, additional key has been introduced. Slightly modifying previous example by adding `:generate-handler` key to `::swagger/responses`:

```clojure
(defresource users [id db]
  {:swagger {:post {:tags                ["tag1"]
                    :summary             "Create new user"
                    ::swagger/parameters {:path (spec/keys :req-un [::id])
                                          :body ::user}
                    ::swagger/responses {200 {:schema ::user}
                                         404 {:generate-handler :handle-not-found
                                              :description "User not found!"}}
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [ctx] (POST-CODE)))
```

will cause additional handler to be generated, equivalent to appending at the end of the resource definition:
```clojure
  (...)
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [ctx] (POST-CODE)))
  ;; generated-handler:
  :handle-not-found (fn [ctx] (get {:post "User not found"} (get-in ctx [:request :method]))))
  ```

causing textual message to be returned from endpoint, depending on return code and request method.

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
