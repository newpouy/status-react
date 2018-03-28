(ns status-im.ui.screens.qr-scanner.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.camera :as camera]
            [status-im.utils.handlers :as u :refer [register-handler]]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(defn set-current-identifier [db [_ identifier handler]]
  (assoc-in db [:qr-codes identifier] handler))

(defn navigate-to-scanner
  [_ [_ identifier]]
  (re-frame/dispatch [:request-permissions
                      [:camera]
                      #(re-frame/dispatch [:navigate-to :qr-scanner {:current-qr-context identifier}])
                      #(utils/show-popup (i18n/label :t/error)
                                         (i18n/label :t/camera-access-error))]))

(register-handler :scan-qr-code
  (re-frame/after navigate-to-scanner)
  set-current-identifier)

(register-handler :clear-qr-code
  (fn [db [_ identifier]]
    (update db :qr-codes dissoc identifier)))

(defn- handle-qr-request
  [db [_ context data]]
  (when-let [handler (get-in db [:qr-codes context])]
    (re-frame/dispatch [handler context data])))

(defn clear-qr-request [db [_ context]]
  (-> db
      (update :qr-codes dissoc context)
      (dissoc :current-qr-context)))

(register-handler :set-qr-code
  (u/handlers->
    handle-qr-request
    clear-qr-request))
