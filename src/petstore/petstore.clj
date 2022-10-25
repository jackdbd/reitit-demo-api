(ns petstore.petstore
  "FIXME: my new org.corfield.new/scratch project.")

(defn exec
  "Invoke me with clojure -X petstore.petstore/exec"
  [opts]
  (println "exec with" opts))

(defn -main
  "Invoke me with clojure -M -m petstore.petstore"
  [& args]
  (println "-main with" args))
