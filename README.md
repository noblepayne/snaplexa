# snaplexa

Alexa skill to play the TechSNAP podcast.

## Overview

Experiment using alexa-app, express, and the new-ish npm-deps clojurescript compiler options. 

TODO:
  - Improve rss feed operations
  - Implement next, shuffle, pause/resume
  - Add specs/tests

## Setup

To get an interactive development environment run:

    lein figwheel

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2018 Wesley Payne

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
