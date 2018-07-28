package com.elite.model.doctor_side

/**
 * Created by anil on 28/11/17.
 */
class DoctorDetailsInfo{

    var status: String? = null
    var message: String? = null
    var doctorDetails: List<DoctorDetailsBean>? = null


    class DoctorDetailsBean {

        var name: String? = null
        var email: String? = null
        var profileImage: String? = null
        var specialist: String? = null
        var contactNumber: String? = null
        var countryCode: String? = null
        var userLocation: String? = null
        var postId: String? = null
        var job_title: String? = null
        var user_id: String? = null
        var vacancy_number: String? = null
        var job_type: String? = null
        var qualification: String? = null
        var need_from_date: String? = null
        var job_timing: String? = null
        var salary: String? = null
        var description: String? = null
        var location: String? = null
        var is_interested: String? = null

        var firebase_id: String? = null
        var deviceToken: String? = null
        var category_id: String? = null


    }

}