(ns vlc-remote.commands
  (:use [net.cgrand.moustache :exclude [not-found]])
  (:require wmctrl robot
            [vlc-remote.telnet :as telnet]))

(defn vlc-status [req])

(def routes
  (app
   :post (app
          ["status"] vlc-status)))