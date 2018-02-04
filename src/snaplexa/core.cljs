(ns snaplexa.core
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint :refer [pprint]]
            [promesa.core :as p]
            ["alexa-app" :as alexa]
            ["express"]
            [snaplexa.feed :as feed]))

(nodejs/enable-util-print!)

(defonce server (atom nil))

(defonce user-data (atom {}))

(defn play-episode [msg error-msg response token]
  (fn [episode-url]
    (if episode-url
      (doto response
        (.say msg)
        (.audioPlayerPlayStream
         "REPLACE_ALL"
         #js {:url episode-url
              :token token
              :offsetInMilliseconds 0}))
      (.say response
            (str error-msg)))))

(defn configure-app
  "Configure alexa integrations. Returns alexa app."
  [alexa-app express-app]
  (doto alexa-app
    (.express
     #js {:expressApp express-app
          :endpoint "/"})
    (.launch
     (fn [request response]
       (-> (feed/get-episode :latest)
           (p/then
            (play-episode "Playing the latest episode of tech snap"
                          "Sorry. I could not play tech snap. Please visit tech snap dot systems for other ways to listen."
                          response
                          "latest")))))
    (.intent
     "play"
     (fn [request response]
       (let [episode (.slot request "episode")]
         (-> (feed/get-episode (js/parseInt episode))
             (p/then
              (play-episode (str "Playing tech snap episode " episode)
                            (str "Sorry. I could not find episode " episode ". Please visit tech snap dot systems for other ways to listen.")
                            response
                            (str "ep" episode)))))))
    (.intent
     "AMAZON.StopIntent"
     (fn [request response]
       (.audioPlayerStop response)))
    (.intent
     "AMAZON.PauseIntent"
     (fn [request response]
       (.audioPlayerStop response)))))

(defn start-app
  "Start express app post alexa configuration. Returns server instance."
  [express-app]
  (.listen express-app
           9999))

(defn -main
  "Configure and start express-based alexa app.
  Stores running server in global server atom."
  [& args]
  (let [express-app (express)
        alexa-app   (alexa/app. "TechSNAP")]
    (configure-app alexa-app
                   express-app)
    (reset! server
            (start-app express-app))))

(set! *main-cli-fn* -main)
(defn on-js-reload
  "close the previous server and spawn anew"
  []
  (.close @server)
  (-main))
