(ns bsless.build.modulo
  (:require
   [clojure.pprint :as pprint]
   [clojure.tools.deps.alpha.util.dir :as dir]
   [clojure.tools.deps.alpha :as deps]
   [clojure.tools.build.api :as b]
   [clojure.java.io :as io]))

(defn -module-call
  [opts f]
  (let [{::keys [project-root]} opts]
    (binding [b/*project-root* project-root]
      (f opts))))

(defn paths [m]
  (letfn [(paths* [ps ks m]
            (reduce-kv
             (fn [ps k v]
               (if (map? v)
                 (paths* ps (conj ks k) v)
                 (conj ps (conj ks k))))
             ps
             m))]
    (paths* () [] m)))

(defn derive-deps
  [parent child]
  (reduce
   (fn [m p]
     (if (= :build.modulo/inherit (peek p))
       (let [p' (pop p)
             what (get-in parent p')
             base (get-in child p')
             scope (:build.modulo/inherit base)
             selected (if (= :all scope) what (select-keys what scope))]
         (assoc-in m p' (merge base selected)))
       m))
   child
   (paths child)))

(defn slurp-deps
  [f]
  (-> f io/file dir/canonicalize deps/slurp-deps))

(defn- -maybe-slurp-deps
  [o]
  (if (string? o) (slurp-deps o) o))

(defn with-derived-project
  [{::keys [parent project] :as opts}]
  (assoc opts :project (derive-deps parent project)))

(defn module-call
  [opts f]
  (-module-call
   opts
   (fn [opts]
     (-> opts
         (update ::parent -maybe-slurp-deps)
         (update ::project -maybe-slurp-deps)
         with-derived-project
         f))))

(defmacro modo
  "Do body where `sym` is bound to module options derived from `opts`."
  {:indent/style 1}
  [[sym opts] & body]
  `(module-call ~opts (fn [~sym] ~@body)))

(def ^:private this-ns (str *ns*))

(defn unqualify
  "Assoc unqualified keywords with the same value as a respective
  qualified keywords which satisfies `pred` in `m`.
  `pred` can be:
  - a function
  - a string: will be converted to a predicate where (= (namespace kw) pred)"
  ([m] (unqualify m this-ns))
  ([m pred]
   (let [pred (cond
                (fn? pred) pred
                (string? pred) #(= (namespace %) pred))]
     (reduce-kv
      (fn [m k v]
        (if (and (qualified-keyword? k)
                 (pred k))
          (let [k' (keyword (name k))]
            (if (contains? m k')
              m
              (assoc m k' v)))
          m))
      m
      m))))

(defn spit-project
  [opts]
  (with-open [w (io/writer (::project opts))]
    (pprint/pprint (:project opts) w))
  opts)

(comment
  (modo [opts {::parent "./deps.edn"
               ::project-root "."
               ::pom "../pom.xml"
               ::lib 'foo.bar/bazz
               ::project "./schmeps.edn"}]
        (-> opts
            unqualify
            (assoc :basis b/create-basis))))
