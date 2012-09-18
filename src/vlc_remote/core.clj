(ns vlc-remote.core
  (:require [vlc-remote.template :as template]
            [vlc-remote.commands :as commands]
            [vlc-remote.telnet :as telnet])
  (:use ring.util.response
        ring.middleware.params
        ring.middleware.resource
        ring.middleware.file-info
        [clojure.data.json :only [json-str read-json]]
        net.cgrand.enlive-html
        [net.cgrand.moustache :exclude [not-found]]
        [ring.adapter.jetty :only [run-jetty]]
        vlc-remote.util))

(def vlc-remote-handler
  (app
   wrap-params
   (wrap-resource "resources")
   (wrap-file-info)
   ["commands" &] commands/routes
   [""] #(snippet-to-response template/home %)
   [&] template/page-404-response))
   

(defonce server
  (run-jetty #'vlc-remote-handler {:port 8888 :join? false}))

(defn reload []
  (.stop server)
  (doall (map load ["template" "telnet" "commands" "core"]))
  (.start server))

