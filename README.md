# vlc-remote

Designed to be a web remote for vlc supporting dvd menus, accesible from a mobile phone
on local wifi

Uses clojure/enlive/ring/moustache for the web service, and connects to VLC through 
both the http/web interface callbacks, and java robot for commands that aren't supported
through http (such as dvd menus)

At the moment I'm only targetting linux, since wmctrl (the library for bringing windows
to the front to make sure they have input), only runs on X11.

## License

Copyright (C) 2012 Corey Williams

Distributed under the Eclipse Public License, the same as Clojure.
