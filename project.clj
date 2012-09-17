(defproject vlc-remote "0.0.1"
  :description "VLC Web-based remote control."
  :source-path "src"
  :main vlc-remote.core
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [enlive "1.0.0"]
                 [net.cgrand/moustache "1.1.0"]
                 [org.clojure/data.json "0.1.3"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-devel "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]])
