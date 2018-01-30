(ns snaplexa.feed
  (:require [promesa.core :as p]
            [tubax.core :as xml]
            ["request-promise" :as rp]))

(def feed-url "http://techsnap.systems/rss")

(defn- fetch-feed
  "Fetches feed from url and returns promise."
  [feed]
  (rp feed))

;; TODO convert to zippers
(defn- get-episode-title
  "Returns episode title."
  [ep]
  (-> ep
      :content
      first
      :content
      first))

(defn- get-episode-number
  "Returns episode number."
  [ep]
  (let [title (get-episode-title ep)
        match (re-matches #"Episode (\d+).*" title)
        number (js/parseInt (last match))]
    number))

(defn- get-episode-url
  "Returns episode url."
  [ep]
  (-> ep
      :content
      (nth 5) ;; hacky magic number
      :attributes
      :url))

(defn- episodes-from-feed
  "Returns episode map keyed on ep number from feed xml."
  [xmldata]
  (let [cljdata (xml/xml->clj xmldata)
        content (-> cljdata last second first :content)]
    (->> content
         (drop 14) ;; hacky magic number
         (reduce
          (fn [eps ep]
            (assoc eps
                   (get-episode-number ep) (get-episode-url ep)))
          {}))))

(defn- get-episode-url-by-number
  "Returns episode url (or nil) from episode number and episode map.
  If passed :latest, returns newest episode url."
  [n eps]
  (if (= :latest n)
    (let [newest-ep (apply max (keys eps))]
      (get eps newest-ep))
    (get eps n)))


(defn get-episode
  "Fetch TechSNAP episode url by episode number."
  ([] (get-episode :latest))
  ([n]
   (let [get-url (partial get-episode-url-by-number n)]
     (-> (fetch-feed feed-url)
         (p/then episodes-from-feed)
         (p/then get-url)
         (p/catch (fn [e] nil))))))
