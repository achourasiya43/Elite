package com.elite.model

/**
 * Created by anil on 9/11/17.
 */

class UserInfo {

    var status: String? = null
    var message: String? = null
    var userDetail: UserDetailBean? = null

    class UserDetailBean {

        var id: String? = null
        var name: String? = null
        var password: String? = null
        var email: String? = null
        var userType: String? = null
        var profileImage: String? = null
        var specialist: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var location: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var authToken: String? = null
        var deviceType: String? = null
        var deviceToken: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var firebase_id: String? = null
        var Is_notify: String? = null
        var categoryDetails: List<CategoryDetailsBean>? = null


        class CategoryDetailsBean {

            var id: String? = null
            var name: String? = null
            var ischecked:Boolean = false

        }
    }
}
