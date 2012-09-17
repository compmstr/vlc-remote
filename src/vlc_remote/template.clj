(ns vlc-remote.template
  (:use net.cgrand.enlive-html
        vlc-remote.util))

(defsnippet base-snip "templates/base.html" [:html] [req])

(defsnippet* home
  (base-snip req)
  [:html]
  [req])

(deftemplate page-404 "templates/404.html" [])
(defn page-404-response [_]
  {:status 404 :headers {"Content-type" "text/html"} :body (page-404)})