(ns bsless.build.modulo.specs
  (:require
   [clojure.tools.build.api.specs :as tbas]
   [clojure.tools.deps.alpha.specs :as tdas]
   [clojure.spec.alpha :as s]))

(s/def :bsless.build.modulo/parent
  (s/or :path string?
        :deps-map ::tdas/deps-map))

(s/def :bsless.build.modulo/project
  (s/or :path string?
        :deps-map map?))

(s/def :bsless.build.modulo/project-root string?)
