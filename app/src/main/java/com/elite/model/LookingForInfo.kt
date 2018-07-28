package com.elite.model

/**
 * Created by anil on 22/11/17.
 */
class LookingForInfo {


    var status: String? = null
    var message: String? = null
    var userDetail: UserDetailBean? = null
    var userStatus: String? = null


    class UserDetailBean {

        var id: String? = null
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
        var latitude: String? = null
        var longitude: String? = null
        var status: String? = null
        var crd: String? = null
        var upd: String? = null
        var catId: String? = null

    }
}