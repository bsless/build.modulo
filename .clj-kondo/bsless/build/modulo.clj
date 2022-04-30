(ns bsless.build.modulo)

(defmacro modo
  [[sym opts] & body]
  `(module-call ~opts (fn [~sym] ~@body)))
