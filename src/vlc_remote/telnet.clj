(ns vlc-remote.telnet
  (:import (java.net Socket)
           (java.io PrintWriter BufferedReader InputStreamReader)))

(declare start-conn)
(defonce conn (ref nil))

(defn connect-in-5-seconds [read-handle]
  (.start (Thread. (fn []
                     (Thread/sleep 5000)
                     (start-conn read-handle)))))

(defn connect
  []
  (let [socket (Socket. "localhost" 4212)
        out (PrintWriter. (.getOutputStream socket))
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    {:socket socket :out out :in in}))

(defn disconnect
  []
  (dosync
   (alter
    conn merge {:exit true})))

(defn write [msg]
  (if (not (nil? (:out @conn)))
    (doto (:out @conn)
      (.println (str msg "\r"))
      (.flush))))

(defn telnet-handler
  []
  (while (nil? (:exit @conn))
    (if (or (nil? @conn)
            (not (.isConnected (:socket @conn))))
      (do ;if we're not connected
        (println "Connection closed, reconnecting")
        (dosync
         (ref-set conn (connect))))
      (do ;we're connected
        (if (.ready (:in @conn))
          (let [msg (.readLine (:in @conn))]
            (println "message:" msg)))))
    (Thread/sleep 100))
  (.close (:out @conn))
  (.close (:socket @conn))
  (dosync
   (ref-set conn nil)))

(defn start-conn [read-handle]
  (try
    (dosync
     (ref-set conn (assoc (connect) :read-handle read-handle)))
    (.start (Thread. telnet-handler))
    (catch Exception e
      (connect-in-5-seconds read-handle))))