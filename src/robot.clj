(ns robot
  (:import [java.awt Robot GraphicsEnvironment GraphicsDevice]
           [java.awt.event KeyEvent InputEvent]
           clojure.lang.Reflector))

(defn get-robot []
  (let [environ (GraphicsEnvironment/getLocalGraphicsEnvironment)
        devs (.getScreenDevices environ)]
    (if (not (zero? (count devs)))
      (Robot. (first devs))
      nil)))

(defn- mouse-demo []
  (let [rob (get-robot)
        init-coord 100]
    (dotimes [i 200]
      (doto rob
        (.mouseMove (+ init-coord i) (+ init-coord i))
        (.delay 10)))
    (doto rob
      ;;Note: button 2 is middle, button 3 is right
      (.mousePress InputEvent/BUTTON3_MASK)
      (.mouseRelease InputEvent/BUTTON3_MASK))))

(defn- key-demo []
  (let [rob (get-robot)]
    (.delay rob 2000)
    (dotimes [_ 10]
      (doto rob
        (.keyPress KeyEvent/VK_UP)
        (.keyRelease KeyEvent/VK_UP)
        (.delay 200)))))

;(defmacro get-vkey-new
  ;[key]
  ;(let [sym (symbol (str "VK_" (.toUpperCase key)))]
  ;`(. java.awt.event.KeyEvent ~sym)))

(defn get-vkey-new
  [key]
  (Reflector/getStaticField "java.awt.event.KeyEvent" (str "VK_" (.toUpperCase key))))

(defn- get-vkey
  "Returns the KeyEvent/VK_<key> for use with robot"
  [key]
  (eval
   (symbol
    (str "KeyEvent/VK_" (.toUpperCase key)))))

(defn key-press
  "Performs a key press, using strings of VK_... codes
ex: \"Control\" \"d\" does ctrl-d"
  [& keys]
  (let [keycodes (map #(get-vkey-new %) keys)
        rob (get-robot)]
    (doseq [code keycodes]
      (doto rob
        (.keyPress code)))
    (doseq [code keycodes]
      (doto rob
        (.keyRelease code)))))

(comment
  (wmctrl/bring-window-to-front "vlc")
  (key-press "control" "d")
  (key-press "alt" "p")
  (key-press "enter")
  )
