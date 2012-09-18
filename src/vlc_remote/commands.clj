(ns vlc-remote.commands
  (:use [net.cgrand.moustache :exclude [not-found]]
        [clojure.data.json :only [json-str read-json]])
  (:require wmctrl robot
            [clojure.string :as string])
  (:import [java.io BufferedReader InputStreamReader]))

;;/usr/share/vlc/lua/http/requests
;; describes commands to send to status.json

(def json-url "http://localhost:8080/requests/status.json")

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

(defn vlc-status []
  )

(def routes
  (app
   :post (app
          ["status"] vlc-status)))

(defn telnet-handler
  [input])

;;status.xml or status.json
;;===========


;;< Get VLC status information, current item info and meta.
;;< Get VLC version, and http api version

;;> add <uri> to playlist and start playback:
;;  ?command=in_play&input=<uri>&option=<option>
;;  the option field is optional, and can have the values:
;;    noaudio
;;    novideo

;;> add <uri> to playlist:
;;  ?command=in_enqueue&input=<uri>

;;> add subtitle to currently playing file
;;  ?command=addsubtitle&val=<uri>

;;> play playlist item <id>. If <id> is omitted, play last active item:
;;  ?command=pl_play&id=<id>

;;> toggle pause. If current state was 'stop', play item <id>, if no <id> specified, play current item. If no current item, play 1st item in the playlist:
;;  ?command=pl_pause&id=<id>

;;> resume playback if paused, else do nothing
;;  ?command=pl_forceresume

;;> pause playback, do nothing if already paused
;;  ?command=pl_forcepause

;;> stop playback:
;;  ?command=pl_stop

;;> jump to next item:
;;  ?command=pl_next

;;> jump to previous item:
;;  ?command=pl_previous

;;> delete item <id> from playlist:
;;  ?command=pl_delete&id=<id>
;;  NOTA BENE: pl_delete is completly UNSUPPORTED

;;> empty playlist:
;;  ?command=pl_empty

;;> set audio delay
;;  ?command=audiodelay&val=<delayinseconds>

;;> set subtitle delay
;;  ?command=subdelay&val=<delayinseconds>

;;> set playback rate. must be > 0
;;  ?command=rate&val=<newplaybackrate>

;;> set aspect ratio. Must be one of the following values. Any other value will reset aspect ratio to default
;;  ?command=aspectratio&val=<newratio>
;;  Valid aspect ratio values: 1:1 , 4:3 , 5:4 , 16:9 , 16:10 , 221:100 , 235:100 , 239:100

;;> sort playlist using sort mode <val> and order <id>:
;;  ?command=pl_sort&id=<id>&val=<val>
;;  If id=0 then items will be sorted in normal order, if id=1 they will be
;;  sorted in reverse order
;;  A non exhaustive list of sort modes:
;;    0 Id
;;    1 Name
;;    3 Author
;;    5 Random
;;    7 Track number

;;> toggle random playback:
;;  ?command=pl_random

;;> toggle loop:
;;  ?command=pl_loop

;;> toggle repeat:
;;  ?command=pl_repeat

;;> toggle enable service discovery module <val>:
;;  ?command=pl_sd&val=<val>
;;  Typical values are:
;;    sap
;;    shoutcast
;;    podcast
;;    hal

;;> toggle fullscreen:
;;  ?command=fullscreen

;;> set volume level to <val> (can be absolute integer, percent or +/- relative value):
;;  ?command=volume&val=<val>
;;  Allowed values are of the form:
;;    +<int>, -<int>, <int> or <int>%

;;> seek to <val>:
;;  ?command=seek&val=<val>
;;  Allowed values are of the form:
;;    [+ or -][<int><H or h>:][<int><M or m or '>:][<int><nothing or S or s or ">]
;;    or [+ or -]<int>%
;;    (value between [ ] are optional, value between < > are mandatory)
;;  examples:
;;    1000 -> seek to the 1000th second
;;    +1H:2M -> seek 1 hour and 2 minutes forward
;;    -10% -> seek 10% back

;;>command=preamp&val=<val in dB>
;; sets the preamp value, must be >=-20 and <=20

;;>command=equalizer&band=<band>&val=<gain in dB, must be >=-20 and <=20)
;; set the gain for a specific band

;;>command=enableeq&val=<0 or 1>
;; 0 --  disables the equalizer
;; 1 --  enables the equalizer

;;>command=setpreset&val=<presetid>
;; set the equalizer preset as per the id specified

;;<Displays the equalizer band gains.
;;Band 0: 60 Hz, 1: 170 Hz, 2: 310 Hz, 3: 600 Hz, 4: 1 kHz,
;;5: 3 kHz, 6: 6 kHz, 7: 12 kHz , 8: 14 kHz , 9: 16 kHz

;;<Display the list of presets available for the equalizer

;;---
;;Commands available from API version 2
;;---

;;> select the title
;;  ?command=title&val=<val>

;;> select the chapter
;;  ?command=title&val=<val>

;;> select the audio track (use the number from the stream)
;;  ?command=audio_track&val=<val>

;;> select the video track (use the number from the stream)
;;  ?command=video_track&val=<val>

;;> select the sibtitle track (use the number from the stream)
;;  ?command=subtitle_track&val=<val>

;;playlist.xml or playlist.json:
;;=============
;;< get the full playlist tree

;;NB: playlist_jstree.xml is used for the internal web client. It should not be relied upon by external remotes.
;;It may be removed without notice.

;;browse.xml or browse.json:
;;===========

;;< ?dir=<uri>
;;> get file list from uri. At the moment, only local file uris are supported

;;NB: uri is the preferred parameter. Dir is deprecated and may be removed in a future release.
;;< ?dir=<dir>
;;> get <dir>'s filelist
