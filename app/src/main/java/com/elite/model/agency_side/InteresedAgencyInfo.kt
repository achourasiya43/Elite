package com.elite.model.agency_side

/**
 * Created by anil on 28/11/17.
 */
class InteresedAgencyInfo {

    var status: String? = null
    var message: String? = null
    var InterestedAgencyList: List<InterestedAgencyListBean> = ArrayList()


    class InterestedAgencyListBean {

        var id: String? = null
        var name: String? = null
        var email: String? = null
        var profileImage: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var location: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var crd: String? = null
        var upd: String? = null
        var day: String? = null
        var job_title: String? = null

        var firebase_id: String? = null
        var category_id: String? = null
        var deviceToken: String? = null

    }
}