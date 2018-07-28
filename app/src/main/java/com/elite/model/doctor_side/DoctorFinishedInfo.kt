package com.elite.model.doctor_side

/**
 * Created by anil on 7/12/17.
 */
class DoctorFinishedInfo {


    var status: String? = ""
    var message: String? = ""
    var doctorFinishedJobList: ArrayList<DoctorFinishedJobListBean> = ArrayList()


    class DoctorFinishedJobListBean {

        var name: String? = ""
        var profileImage: String? = ""
        var contactNumber: String? = ""
        var countryCode: String? = ""
        var userLocation: String? = ""
        var id: String? = ""
        var job_title: String? = ""
        var salary: String? = ""
        var need_from_date: String? = ""
        var finished_by: String? = ""
        var location: String? = ""
        var crd: String? = ""
        var upd: String? = ""

        var qualification: String? = ""
        var vacancy_number: String? = ""

    }
}