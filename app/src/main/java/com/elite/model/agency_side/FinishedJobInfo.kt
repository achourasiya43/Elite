package com.elite.model.agency_side

/**
 * Created by anil on 5/12/17.
 */
class FinishedJobInfo {

    var status: String? = null
    var message: String? = null
    var finishedJobListAgencySide: ArrayList<FinishedJobListAgencySideBean> = ArrayList()


    class FinishedJobListAgencySideBean {

        var name: String? = null
        var profileImage: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var userLocation: String? = null
        var id: String? = null
        var job_title: String? = null
        var salary: String? = null
        var need_from_date: String? = null
        var location: String? = null
        var crd: String? = null
        var upd: String? = null
        var thumbImage: String? = null
        var day: String? = null

        var deviceToken: String? = null
        var firebase_id: String? = null
        var category_id: String? = null

    }
}