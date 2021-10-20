(ns clj-holmes.rules.engine
  (:require [clj-holmes.rules.utils :as utils]
            [clojure.spec.alpha :as s]
            [shape-shifter.core :refer [*wildcards* pattern->spec]]))

(defn ^:private build-fn-to-find [function namespace ns-declaration check-required?]
  (if check-required?
    (->> function
         (utils/function-usage-possibilities ns-declaration namespace)
         (map (fn [element] `'~element))
         set)
    `#{~function}))

(defn ^:private build-spec [ns-declaration
                            {:keys [function namespace check-required? pattern]}]
  (let [fn-to-find (build-fn-to-find function namespace ns-declaration check-required?)]
    (binding [*wildcards* (merge *wildcards* {"$custom-function" fn-to-find})]
      (let [spec (pattern->spec pattern)]
        (fn check [form]
          (s/valid? spec form))))))

(defn check [{:keys [forms ns-declaration]} {:keys [definition patterns]}]
  (let [finder (comp
                (map #(build-spec ns-declaration %))
                (map #(utils/find-in-forms forms %)))
        findings (transduce finder concat patterns)]
    (when (seq findings)
      (assoc {} :findings (into [] findings)
             :id (:id definition)
             :definition (-> definition :shortDescription :text)))))