package com.elite.model.agency_side

/**
 * Created by anil on 27/11/17.
 */
class AllRequestInfo {

    var status: String? = null
    var message: String? = null
    var doctorRequestJobList: ArrayList<DoctorRequestJobListBean>? = ArrayList()


    class DoctorRequestJobListBean {
        var id: String? = null
        var post_id: String? = null
        var category_id: String? = null
        var job_title: String? = null
        var vacancy_number: String? = null
        var job_type: String? = null
        var qualification: String? = null
        var need_from_date: String? = null
        var job_timing: String? = null
        var salary: String? = null
        var description: String? = null
        var location: String? = null
        var latitude: String? = null
        var longitude: String? = null
        var finished_by: String? = null
        var crd: String? = null
        var upd: String? = null
        var doctorName: String? = null
        var profileImage: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var thumbImage: String? = null
        var day: String? = null
    }
}