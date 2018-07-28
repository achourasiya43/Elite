package com.elite.model

/**
 * Created by anil on 19/12/17.
 */
class NotificationInfo {

    var status: String? = null
    var message: String? = null
    var notificationList: List<NotificationListBean>? = null


    class NotificationListBean {

        var name: String? = null
        var profileImage: String? = null
        var notification_message: NotificationMessageBean? = null
        var notification_type: String? = null
        var crd: String? = null
        var thumbImage: String? = null
        var day: String? = null


        class NotificationMessageBean {

            var title: String? = null
            var body: String? = null
            var type: String? = null
            var reference_id: String? = null
            var click_action: String? = null

        }
    }
}