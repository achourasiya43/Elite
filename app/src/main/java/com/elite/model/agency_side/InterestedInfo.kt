package com.elite.model.agency_side

import java.util.ArrayList

/**
 * Created by anil on 5/12/17.
 */
class InterestedInfo {

    var status: String? = null
    var message: String? = null
    var doctorListInterestedByAgency: ArrayList<DoctorListInterestedByAgencyBean> = ArrayList()


    class DoctorListInterestedByAgencyBean {

        var id: String? = null
        var post_id: String? = null
        var name: String? = null
        var email: String? = null
        var profileImage: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var location: String? = null
        var job_title: String? = null
        var vacancy_number: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var crd: String? = null
        var upd: String? = null
        var thumbImage: String? = null
        var day: String? = null

        var firebase_id: String? = null
        var category_id: String? = null
        var deviceToken: String? = null



    }
}