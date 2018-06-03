(ns roll.views
  (:require [re-frame.core :as re-frame]
            [roll.events :as events]
            [roll.subs :as subs]
            ))

(defn http-link
  ([url] [:a {:href url} url])
  ([url title] [:a {:href url} title]))

(defn footer
  "Page Footer"
  []
  [:footer
   (str \u00A9 " 2018 deciduously - ")
   (http-link "https://github.com/deciduously/roll")])

(defn command-input
  "Command input"
  []
  [:div
   "Command: "
   [:input {:type "text"
            :id "field"
            :name "cmd"}] ;; do I need :name?
   [:input {:type "button"
            :value "Submit"
            :on-click #(re-frame/dispatch
                        [::events/submit-command (-> (.getElementById js/document "field") .-value)])}]])

(defn item
  "View a single item"
  [{:keys [title damage]} item]
  [:li.item
   (str title ": " damage)])

(defn all-items
  "View all items"
  [items]
  [:div.items
   (if (empty? items)
     [:span "Nada"]
     [:ul
      (for [i items]
        ^{:key (:id i)}
        [item i])])])

(defn add-item
  "Add an item"
  []
  [:div
   "New item: "
   [:input {:type "text" :id "item-name" :name "name"}]
   [:input {:type "text" :id "item-damage" :name "damage"}]
   [:input {:type "button" :value "Submit" :on-click #(re-frame/dispatch [::events/add-item
                                                                          {:title (-> (.getElementById js/document "item-name") .-value)
                                                                           :damage (-> (.getElementById js/document "item-damage") .-value)}])}]])

(defn outcome
"View a single outcome"
[{:keys [roll rolls]} outcome]
[:li.roll
 [:span.roll-string roll]
 [:div.roll-result
  (let [total (reduce + (map js/parseInt rolls))] ; maybe this should be a subscription?  get re-frame-y
    [:span.roll-total total])
  [:span.roll-vals (str ": " rolls)]]])

(defn outcomes
"View a list of outcomes"
[{:keys [time result]} outcomes]
[:div (.toLocaleTimeString time js/Date)
 [:ul.outcomes
  (for [o (:outcomes result)]
    [outcome o])]])

(defn roll-hx
"View full roll history"
[hx]
[:ul.hx
 (for [os (reverse hx)]
   ^{:key (:id os)}
   [:li [outcomes os]])])

(defn view-error
"Render a bad http result"
[{:keys [uri last-method debug-message]} error]
[:div.api-error
 [:p (str "Could not " last-method " " uri ": " debug-message)]])

(defn usage
"Usage instructions"
[]
[:div.usage
 [:p "Usage" [:br] "Enter commands in one of three formats:"]
 [:ul
  [:li "One or more rolls in XdX format: `1d6`, `1d4 2d20`, etc"]
  [:li "A multiplier followed by a roll: `6 2d6`"]
  [:li "An item to look up in the table, optionally preceeded by a multiplier: `ian`, `33 ian`"]]])

(defn main-panel []
(let [result (re-frame/subscribe [::subs/results])
      error (re-frame/subscribe [::subs/error])
      items (re-frame/subscribe [::subs/items])]
  [:div
   [:h1 "ROLL"]
   [usage]
   "Roll history:  " [roll-hx @result] [:br]
   [command-input] [:br]
   "Items: " [all-items @items] [:br]
   [add-item] [:br]
   "Last error:  " [view-error @error] [:hr]
   [footer]]))
