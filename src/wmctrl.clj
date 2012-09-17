(ns wmctrl
  (:require [clojure.java.shell :as sh]
              [clojure.string :as string :only [split]]))

(defn shell-demo []
  ;;sh -- Gives args to Runtime.exec to run a process
  ;;  ends up as [cmd & opts]
  ;;  returns a map of :exit (exit code) :out (stdout) :err (stderr)
  ;;  options:
  ;;    :in <string to pass as stdin>
  ;;    :out followed by :bytes or a string for encoding, defaults to "UTF-8"
  ;;    :env override process env with a map
  ;;    :dir override process dir with a string or java.io.File
  ;;  You can bind :env or :dir for multiple calls using with-sh-env and with-sh-dir
  (sh/sh "ls")
  (comment
    (println (sh "ls" "-l"))
    (println (sh "ls" "-l" "/no-such-thing"))
    (println (sh "sed" "s/[aeiou]/oo/g" :in "hello there\n"))
    (println (sh "cat" :in "x\u25bax\n"))
    (println (sh "echo" "x\u25bax"))
    (println (sh "echo" "x\u25bax" :out "ISO-8859-1")) ; reads 4 single-byte chars
    (println (sh "cat" "myimage.png" :out :bytes)) ; reads binary file into bytes[]
    ))

(defn- wmctrl-list []
  (let* [lst-raw (sh/sh "wmctrl" "-l")
         lst-entries (string/split (:out lst-raw) #"\n")]
        lst-entries))

(defn find-windows [title]
  (filter (complement nil?)
          (map #(re-find 
                 (re-pattern (str "(?i)" ".*" title ".*")) ;;The (?i) is for case insensitive
                 %)
               (wmctrl-list))))

(defn- find-window-ids [title]
  (map #(first (string/split % #"\s+"))
       (find-windows title)))

(defn bring-window-to-front
  "Bring window with title containing param to front
returns nil if window not found"
  [title]
  (if-let [win-id (first (find-window-ids title))]
    (sh/sh "wmctrl" "-i" "-R" win-id)))
