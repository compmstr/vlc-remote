(ns vlc-remote.commands
  (:use [net.cgrand.moustache :exclude [not-found]]
        net.cgrand.enlive-html
        ring.util.response
        [clojure.data.json :only [json-str read-json]])
  (:require wmctrl robot
            [clojure.string :as string])
  (:import [java.io BufferedReader InputStreamReader]))

(def json-url "http://localhost:8080/requests/status.json")
(def xml-url "http://localhost:4212/requests/status.xml")

(defn encode-get-args
  "Expects a list of key value pairs, turns into a get query"
  [args]
  (if (zero? (count args))
    ""
    (str "?"
         (string/join "&"
                      (map
                       (fn [[key val]] (str (name key) "=" val))
                       (apply hash-map args))))))
 

(defn fetch-json
  "opts is a list of keywords and values, keywords get turned into GET vars, values are the values
ex: :command \"pl_pause\" gets turned into ?command=pl_pause"
  [& opts]
  (with-open [stream (.openStream (java.net.URL. (str json-url (encode-get-args opts))))]
    (let [buf (java.io.BufferedReader.
               (java.io.InputStreamReader. stream))]
      (read-json (apply str (line-seq buf))))))

(defn fetch-xml
  ([] (fetch-xml xml-url))
  ([url] (try
           (html-resource (java.net.URL. url))
           (catch Exception e
             ;(println "Error fetching url: " (.getMessage e))
             nil))))

(defn status-xml-attr
  [xml & attrs]
  (first
   (:content (first
              (select xml (concat [:root]
                                  (if (vector? attrs)
                                    attrs
                                    (apply vector attrs))))))))

(defn status-xml-attrs
  [xml & attrs]
  (apply hash-map
         (reduce concat
                 (map #(if (vector? %)
                         (vector (last %) ((fn [sel] (apply (partial status-xml-attr xml) sel))  %))
                         (vector % (status-xml-attr xml %)))
                      attrs))))

(defn vlc-status
  "Fetches xml (as enlive structure), and uses it for the status
If the xml is passed in, just uses that"
  ([]
     (vlc-status (fetch-xml)))
  ([xml]
     (if (nil? xml)
       {}
       (status-xml-attrs xml
                         :volume :time :length
                         :state :fullscreen :random :loop
                         :repeat [:meta-information :title]))))

(defn vlc-one-stream-info
  [stream]
  {(keyword (.toLowerCase (string/replace (:name (:attrs stream)) #"\s+" "")))
   (reduce merge
           (map #(hash-map (:name (:attrs %))
                           (apply str (:content %)))
                (select stream [:info])))})

(defn vlc-stream-info
  []
  (let [xml (fetch-xml)]
    (reduce merge
            (map vlc-one-stream-info (select xml
                                             [:information
                                              [:category (attr-starts "Stream")]])))))

;;If value is a map, used as url params to the vlc web interface
;;If it's a function, it's executed to run the command
;;  Function versions should return extra info to pass back to the client or nil
(def command-handlers
  {"status" {}
   "fullscreen" {:command "fullscreen"}
   "play" {:command "pl_play"}
   "pause" {:command "pl_pause"}
   "stop" {:command "pl_stop"}
   "seek" {:command "seek"}
   "next" {:command "pl_next"}
   "prev" {:command "pl_previous"}})

(defn map-to-query
  "Takes a map of keyword value pairs, and uses the keyword names and values to generate
a GET query string"
  [params]
  (str "?" (string/join "&"
                        (map #(str (name (first %)) "=" (second %)) params))))

(defn run-web-cmd
  "It seems that all vlc http commands take the form of command=<cmd>[&val=<val>]"
  [params val]
  (let* [extra-params (if (empty? val)
                        {}
                        {:val (first val)})
         query (if (zero? (count params))
                ""
                (map-to-query (merge params extra-params)))]
    (vlc-status (fetch-xml (str xml-url query)))))

(defn command-response
  [req]
  (let* [split-cmd (rest (string/split (:path-info req) #"/"))
         cmd-string (first split-cmd)
         cmd-args (rest split-cmd)
         handler (command-handlers cmd-string)
         handler-func? (fn? handler) ;;if the cmd is a func, we're not calling the web server
         handler-response (if handler-func?
                    (handler cmd-args)
                    (run-web-cmd handler cmd-args))
         status (if handler-func?
                  (vlc-status)
                  handler-response)]
        (-> (response
             (json-str {:status status
                        :command cmd-string}))
            (content-type "application/json"))))

(def routes
  (app
   [&] command-response))

(defn telnet-handler
  [input])

